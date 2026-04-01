package com.tiketing.api.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.tiketing.api.global.exception.BusinessException;
import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.repository.ReservationRepository;
import com.tiketing.api.reservation.repository.SeatRepository;

@SpringBootTest // 통합 테스트
public class SeatServiceConcurrencyTest {
	
	@Autowired
	private SeatService seatService; // 객체 주입
	
	// Redis 제어를 위한 템플릿 주입
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
	private SeatRepository seatRepository;
    
 	@Autowired
 	private ReservationRepository reservationRepository;

    // 매 테스트가 실행되기 직전에 Redis를 깔끔하게 비워주기
    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
    
    // 테스트가 종료된 직후에도 Redis를 비워주어 다른 테스트에 영향을 주지 않도록 격리
    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        
        reservationRepository.deleteAllInBatch();
        
        // 1번 좌석 상태 원래대로 복구
 		Seat seat = seatRepository.findById(1L).orElseThrow();
 		seat.release(); // AVAILABLE로 원복
 		seatRepository.save(seat);
    }
	
	@Test
    @DisplayName("동시성 제어: 100명의 사용자가 동일한 좌석(ID: 1) 예매 시도 시, 단 1명만 성공해야 한다.")
	void reserveSeat_Concurrency_100Users() throws InterruptedException {
		// given (테스트 환경 세팅)
		int threadCount = 100; // 100명의 유저로 가정
		Long targetSeatId = 1L; // DataLoader가 생성해준 1번 좌석
		
		// 쓰레드 풀 생성
		// 32개의 쓰레드가 100개의 작업을 나눠서 처리하도록
		ExecutorService executorService = Executors.newFixedThreadPool(32);
		
		// 쓰레드 대기 장치
		// 100개의 작업이 끝날 때까지 Main 쓰레드가 대기하도록 하는 자물쇠
		CountDownLatch latch = new CountDownLatch(threadCount);
		
		// 쓰레드 세이프한 Integer타입
		// AtomicInteger는 멀티 스레드 환경에서도 안전하게 숫자를 카운트할 수 있도록 보장하는 클래스
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);
		
		// when (100번의 예매 요청 동시 실행)
		for (int i = 0; i < threadCount; i++) {
			final Long userId = (long) i + 1; // 1번 유저부터 100번 유저까지 ID 부여
			
			// 쓰레드 풀에게 작업을 하나씩 전달 (비동기 실행)
			executorService.submit(() -> {
				try {
					// 실제 서비스 호출
					seatService.reserveSeat(targetSeatId, userId);
					
					// 문제 없이 수행된 경우 예매 성공
					successCount.incrementAndGet();
				} catch (BusinessException e) {
					// 예매에 실패
					failCount.incrementAndGet();
				} catch (Exception e) {
					// 분산 락을 획득하지 못하고 타임아웃이 났거나, 다른 런타임 에러가 난 경우에도 실패 처리
                    failCount.incrementAndGet();
				} finally {
					// 스레드가 성공하든 실패하든 1씩 감소
					latch.countDown();
				}
			});
		}
		
		// 메인 스레드는 여기서 대기 latch 숫자가 0이 될때까지
		latch.await();
		
		// then (결과 검증)
		System.out.println("====== 테스트 결과 ======");
        System.out.println("성공 횟수: " + successCount.get()); // 출력 예상: 1
        System.out.println("실패 횟수: " + failCount.get()); // 출력 예상: 99
        System.out.println("=========================");
        
        // 최종 (Assert)
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
        
        // 예매 엔티티 추가로 인한 추가 검증 로직
        List<Reservation> reservations = reservationRepository.findAll();
        
        // 예매 내역이 정확히 1개 생성되었는지 검증
        assertThat(reservations).hasSize(1);
        
        // 그 1개의 예매 내역이 대상 좌석에 대한 것인지 검증
        assertThat(reservations.get(0).getSeat().getSeatId()).isEqualTo(targetSeatId);
	}
}
