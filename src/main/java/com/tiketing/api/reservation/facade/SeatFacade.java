package com.tiketing.api.reservation.facade;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.tiketing.api.global.exception.BusinessException;
import com.tiketing.api.global.exception.ErrorCode;
import com.tiketing.api.reservation.service.SeatService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SeatFacade {
	
	private final SeatService seatService;
	private final RedissonClient redissonClient;
	
	// 예매시 0초 대기 분산 락 적용
	public void reserveSeat(Long seatId, Long userId) {
		// Redis에 사용할 Key 이름 만들기 (ex: "seat:lock:15")
		String lockKey = "seat:lock:" + seatId;
		// Redisson 락 객체 가져오기
		// 자물쇠를 잠그려면 먼저 RLock 객체를 가져와야 함
		RLock lock = redissonClient.getLock(lockKey);
		
		try {
			// 자물쇠 잠그기 시도 (스핀락 없이 즉시 실패, 락 유지 최대 3초)
			// - 0초: 남이 이미 잠갔으면 대기하지 않고 즉시 포기함 (재시도 방지)
	        // - 3초: 내가 잠그는 데 성공하면, 내 작업이 3초를 넘기면 강제로 뺏김 (워치독 끄기)
			boolean isLocked = lock.tryLock(0, 3, TimeUnit.SECONDS);
			
			// 이미 잠겨있는 경우
			if (!isLocked) {
				throw new BusinessException(ErrorCode.SEAT_ALREADY_LOCKED);
			}
			
			// 락 획득 설공시에만 실제 DB 트랜잭션 진입
			seatService.reserveSeat(seatId, userId);
			
		} catch (InterruptedException e) { // 스레드가 락을 얻으려고 기다리고 있는데 갑자기 서버가 종료되거나 다른 스레드가 강제로 방해(Interrupt)할 때 발생하는 에러
			// 현재 스레드에게 작업 취소 상태 인지시킴
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
		} finally {
			// 작업이 끝났으니 원자성을 보장하며 락 해제
			// 자물쇠가 잠겨있고 자물쇠 주인이 본인인 경우
			if (lock.isLocked() && lock.isHeldByCurrentThread()) {
				lock.unlock(); // 내부적으로 안전한 Lua 스크립트 실행
			}
		}
	}
}
