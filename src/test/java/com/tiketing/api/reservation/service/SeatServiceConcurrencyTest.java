package com.tiketing.api.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; // 상단 import 필요
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations; // 상단 import 필요
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.tiketing.api.global.exception.BusinessException;
import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.repository.ReservationRepository;
import com.tiketing.api.reservation.repository.SeatRepository;

@SpringBootTest // 통합 테스트
public class SeatServiceConcurrencyTest {
	
	@Autowired
	private SeatService seatService; // 객체 주입
	
    @MockitoSpyBean
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
        
        // 예약건 초기화
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
					System.out.println("런타임 에러 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
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
	
	@Test
	@Disabled("Redisson 마이그레이션으로 인해 더 이상 유효하지 않은 테스트 (Lettuce 결함 증명용)")
	@DisplayName("REDIS SETNX 결함: 락 소유권 확인(GET) 후 찰나의 지연이 발생하여 TTL이 만료되면, 그 틈에 새로 진입한 유저의 락을 오삭제하는 버그가 발생한다.")
	void unlockSeat_LettuceLock_OwnershipDefect() throws InterruptedException {
		// given
		Long targetSeatId = 1L;
		String lockKey = "seat:lock:" + targetSeatId;
		Long user1 = 1L;
		Long user2 = 2L;

		// 유저 1이 먼저 락을 획득해둔 상황
		seatService.reserveSeat(targetSeatId, user1);
		
		// 유저 1이 주인 확인을 마쳤음을 알리는 신호
		CountDownLatch user1CheckedLatch = new CountDownLatch(1);
		// 유저 1을 다시 움직이게 하는 신호
		CountDownLatch user1ResumeLatch = new CountDownLatch(1);
		CountDownLatch testDoneLatch = new CountDownLatch(1);
		
		/*
			redisTemplate.opsForValue()는 스파이 객체(@MockitoSpyBean)가 아니라 내부에 있는 일반 객체를 반환
			일반 객체에 대고 .get()을 가로채려(when)고 하니 Mockito가 에러 반환
			그래서 중간 징검다리인 ValueOperations까지 스파이로 만들어서 연결
		*/
		
		// opsForValue()가 반환하는 진짜 객체를 꺼내서 스파이로 설정
		ValueOperations<String, String> valueOpsSpy = Mockito.spy(redisTemplate.opsForValue());

		// redisTemplate에게 opsForValue() 호출시 위의 스파이를 반환해달라고 지시
		Mockito.doReturn(valueOpsSpy).when(redisTemplate).opsForValue();

		// Redis Spy 설정: 유저 1이 락 주인을 확인하는 시점을 가로챔
		doAnswer(invocation -> {
			String result = (String) invocation.callRealMethod();
			
			// 유저 1이 본인의 락인 것을 확인했다면?
			if (String.valueOf(user1).equals(result)) {
				user1CheckedLatch.countDown(); // 결제 실패
				user1ResumeLatch.await();      // 찰나의 순간 멈춤
			}
			return result;
		}).when(valueOpsSpy).get(lockKey);

		// when
		// 유저 1이 예매 취소(락 해제)를 시도
		new Thread(() -> {
			try {
				seatService.unlockSeat(targetSeatId, user1);
			} finally {
				testDoneLatch.countDown();
			}
		}).start();

		// 장애 상황 연출
		user1CheckedLatch.await(); // 유저 1이 본인의 락 확인

		// 1. 유저 1의 락이 갑자기 만료되어 증발함 (TTL 만료)
		redisTemplate.delete(lockKey);
		System.out.println("유저 1의 락 만료");

		// 2. 그 빈자리에 유저 2가 번개같이 나타나서 자신의 이름으로 락을 획득함
		redisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(user2));
		System.out.println("유저 2가 새로운 락 획득 (주인 바뀜)");

		// 3. 멈춰있던 유저 1 비스니스 로직 재개
		user1ResumeLatch.countDown(); 
		testDoneLatch.await();

		// then
		// 유저 1의 작업이 끝난 후 Redis를 확인
		String currentLockOwner = redisTemplate.opsForValue().get(lockKey);

		System.out.println("====== 장애 재현 결과 ======");
		System.out.println("현재 Redis 락의 주인: " + currentLockOwner);
		System.out.println("=========================");

		// 결함 증명
		// 유저 2가 잡고 있던 락이 유저 1에 의해 지워짐
		// 원래라면 유저 2의 이름이 남아있어야 하지만 Lettuce의 비원자성 때문에 null이 됨
		assertThat(currentLockOwner).isNull();
	}
}
