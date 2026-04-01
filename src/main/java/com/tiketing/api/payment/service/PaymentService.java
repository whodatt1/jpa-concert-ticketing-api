package com.tiketing.api.payment.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.global.exception.BusinessException;
import com.tiketing.api.global.exception.ErrorCode;
import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.ReservationStatus;
import com.tiketing.api.reservation.enums.SeatStatus;
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
	private final StringRedisTemplate redisTemplate;
	
	// 결제를 마치고 승인을 기다리는 로직이라고 가정한다.
	@Transactional
	public void confirmPayment(Long seatId, Long userId) {
		
		String lockKey = "seat:lock:" + seatId;
		
		// 해당 유저가 락을 쥐고있는지 확인
		String lockedUserId = redisTemplate.opsForValue().get(lockKey);
		
		if (lockedUserId == null || !lockedUserId.equals(String.valueOf(userId))) {
			
			log.info("시간 초과로 인한 락 소유권 상실");
			
			throw new BusinessException(ErrorCode.PAYMENT_TIMEOUT);
		}
		
		// 검즘 완료 후 자리 확정 작업 진행
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
        
        seat.confirm();
        
        Reservation reservation = reservationRepository.findBySeat_SeatIdAndUserIdAndStatus(seatId, userId, ReservationStatus.PENDING)
        						.orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        
        reservation.complete();
        
        redisTemplate.delete(lockKey);
        
        log.info("결제 및 좌석 예매 확정 완료! (seatId: {})", seatId);
	}
}
