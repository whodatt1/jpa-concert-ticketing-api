package com.tiketing.api.reservation.scheduler;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.SeatStatus;
import com.tiketing.api.reservation.repository.SeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatRecoveryScheduler {
	
	private final SeatRepository seatRepository;
	private final StringRedisTemplate redisTemplate;
	
	// 1분마다 돌아가며 만료된 Redis락을 제거
	@Scheduled(fixedDelay = 60000)
	@Transactional
	public void recoverExpiredSeats() {
		// 현재 DB에서 LOCK이 걸린 모든 좌석 조회
		List<Seat> lockedSeats = seatRepository.findAllByStatus(SeatStatus.LOCKED);
		
		// 잠긴 좌석이 없으면 바로 리턴
		if (lockedSeats.isEmpty()) {
			return;
		}
		
		// DB에는 잠겨있지만 Redis에 Key가 없는건들 조회
		List<Seat> expiredSeats = lockedSeats.stream()
				.filter(seat -> {
					String lockKey = "seat:lock:" + seat.getSeatId();
					// Redis에 해당 키가 없는 경우 필터링 통과
					return Boolean.FALSE.equals(redisTemplate.hasKey(lockKey));
				})
				.toList();
		
		if (!expiredSeats.isEmpty()) {
			log.info("[Scheduler] Redis 락이 만료된 좌석 {}개 발견. AVAILABLE 상태로 복구합니다.", expiredSeats.size());
			
			for (Seat seat : expiredSeats) {
				seat.release();
			}
		}
	}
}
