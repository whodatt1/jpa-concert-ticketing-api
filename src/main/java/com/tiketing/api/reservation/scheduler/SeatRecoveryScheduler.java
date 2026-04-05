package com.tiketing.api.reservation.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.ReservationStatus;
import com.tiketing.api.reservation.enums.SeatStatus;
import com.tiketing.api.reservation.repository.ReservationRepository;
import com.tiketing.api.reservation.repository.SeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatRecoveryScheduler {
	
	private final ReservationRepository reservationRepository;
	
	// 1분마다 주기적으로 실행
	@Scheduled(cron = "0 * * * * *")
	@Transactional
	public void recoverExpiredReservations() {
		
		// 기준 시간 설정: 현재 시간으로부터 5분 전
		LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
		
		// 5분이 지났는데도 여전히 PENDING 상태인 예매 내역들 조회
		List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(ReservationStatus.PENDING, fiveMinutesAgo);
		
		// 만료된 건이 없으면 종료
		if (expiredReservations.isEmpty()) {
			return;
		}
		
		log.info("[Scheduler] 결제 시간(5분)이 초과된 예매 {}건 발견. 예매 취소 및 좌석 복구를 진행합니다.", expiredReservations.size());
		
		// 일괄 취소 및 좌석 원복 처리
		for (Reservation reservation : expiredReservations) {
			
			// 예매 상태를 CANCELED로 변경
			reservation.cancel();
			
			// 연관된 좌석 상태를 AVAILABLE로 변경
			reservation.getSeat().release();
			
			log.info(" - 예약 ID: {} (좌석 ID: {}) 만료 취소 완료", reservation.getReservationId(), reservation.getSeat().getSeatId());
		}
	}
}
