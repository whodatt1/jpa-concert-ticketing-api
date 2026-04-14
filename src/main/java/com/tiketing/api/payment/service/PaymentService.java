package com.tiketing.api.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.global.exception.BusinessException;
import com.tiketing.api.global.exception.ErrorCode;
import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.ReservationStatus;
import com.tiketing.api.reservation.repository.ReservationRepository;
import com.tiketing.api.reservation.repository.SeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
	
	private final SeatRepository seatRepository;
	private final ReservationRepository reservationRepository;
	//private final StringRedisTemplate redisTemplate;
	
	// 결제를 마치고 확정되는 로직이라고 가정한다.
	@Transactional
	public void confirmPayment(Long seatId, Long userId) {
		
		// 내 예약이 아직 PENDING 상태로 살아있는지 DB에서 확인 (락 소유권 검증)
		// (만약 5분이 지나서 스케줄러가 CANCELED로 바꿨다면 여기서 Exception이 터짐)
		Reservation reservation = reservationRepository.findBySeat_SeatIdAndUserIdAndStatus(seatId, userId, ReservationStatus.PENDING)
				.orElseThrow(() -> {
					log.info("시간 초과 또는 권한 없음으로 인한 결제 실패");
					return new BusinessException(ErrorCode.PAYMENT_TIMEOUT);
				});
		
		Seat seat = seatRepository.findById(seatId)
						.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
		
		// 검증 완료 후 확정
		seat.confirm();
		reservation.complete();
		
		log.info("결제 및 좌석 예매 확정 완료! (seatId: {})", seatId);
	}
}
