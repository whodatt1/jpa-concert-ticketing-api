package com.tiketing.api.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.global.exception.BusinessException;
import com.tiketing.api.global.exception.ErrorCode;
import com.tiketing.api.reservation.dto.SeatRequest;
import com.tiketing.api.reservation.dto.SeatResponse;
import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.ReservationStatus;
import com.tiketing.api.reservation.repository.ReservationRepository;
import com.tiketing.api.reservation.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {
	
	private final SeatRepository seatRepository;
	private final ReservationRepository reservationRepository;
	
	// 특정 회차 좌석 맵 조회
	public List<SeatResponse.Summary> getSeats(Long concertId, Long scheduleId, SeatRequest.SearchCondition searchCondition) {
		
		return seatRepository.getSeats(concertId, scheduleId, searchCondition)
				.stream()
				.map(SeatResponse.Summary::new)
				.toList();
	}
	
	// 레디스에서 락을 잡는 과정에도 DB 쓰레드풀 소모하여 facade로 분리
	@Transactional
	public void reserveSeat(Long seatId, Long userId) {
		
		Seat seat = seatRepository.findById(seatId)
				.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

		seat.reserve(); // 엔티티에게 검증 및 상태 변경을 위임
		
		// PENDING 상태 이력 저장
		reservationRepository.save(Reservation.builder()
				.userId(userId)
				.seat(seat)
				.build()); // 상태는 PENDING
		
	}
	
	// 락이 필요하지 않다고 생각하여 제거
	@Transactional
	public void unlockSeat(Long seatId, Long userId) {
			
		Reservation reservation = reservationRepository.findBySeat_SeatIdAndUserIdAndStatus(seatId, userId, ReservationStatus.PENDING)
				.orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

		Seat seat = seatRepository.findById(seatId)
				.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
		
		seat.release();
		reservation.cancel();
	}
}
