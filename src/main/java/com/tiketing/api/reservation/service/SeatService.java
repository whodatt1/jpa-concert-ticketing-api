package com.tiketing.api.reservation.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.global.exception.BusinessException;
import com.tiketing.api.global.exception.ErrorCode;
import com.tiketing.api.reservation.dto.SeatRequest;
import com.tiketing.api.reservation.dto.SeatResponse;
import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.ReservationStatus;
import com.tiketing.api.reservation.enums.SeatStatus;
import com.tiketing.api.reservation.repository.ReservationRepository;
import com.tiketing.api.reservation.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {
	
	private final SeatRepository seatRepository;
	private final ReservationRepository reservationRepository;
	private final StringRedisTemplate redisTemplate;
	
	// 특정 회차 좌석 맵 조회
	public List<SeatResponse.Summary> getSeats(Long concertId, Long scheduleId, SeatRequest.SearchCondition searchCondition) {
		
		return seatRepository.getSeats(concertId, scheduleId, searchCondition)
				.stream()
				.map(SeatResponse.Summary::new)
				.toList();
	}
	
	// 5분 좌석 선점
	@Transactional
	public void reserveSeat(Long seatId, Long userId) {
		
		// Redis에 사용할 Key 이름 만들기 (ex: "seat:lock:15")
		String lockKey = "seat:lock:" + seatId;
		
		// Redis에 SETNX 명령 날리기 + 5분 타이머(TTL) 설정
		// lockKey가 비어있다면 userId를 저장 후 5분 뒤 만료 성공한다면 true 반환 중복된 키가 있다면 false
		Boolean isLockAcquired = redisTemplate.opsForValue()
						.setIfAbsent(lockKey, String.valueOf(userId), 5, TimeUnit.MINUTES);
		
		// 누군가 먼저 클릭해서 이미 열쇠가 있다면
		if (Boolean.FALSE.equals(isLockAcquired)) {
			throw new BusinessException(ErrorCode.SEAT_ALREADY_LOCKED);
		}
		
		// 1등으로 들어온 경우 DB 물리적 상태도 변경
		Seat seat = seatRepository.findById(seatId)
						.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
		
		try {
			seat.reserve(); // 엔티티에게 검증 및 상태 변경을 위임
			
			// PENDING 상태 이력 저장
			reservationRepository.save(Reservation.builder()
											      .userId(userId)
											      .seat(seat)
											      .build());
		} catch (BusinessException e) {
			// 엔티티가 AVAILABLE 예외를 던지면 Redis락을 제거 후 에러 전달
			redisTemplate.delete(lockKey);
			throw e;
		}
	}
	
	@Transactional
	public void unlockSeat(Long seatId, Long userId) {
		
		String lockKey = "seat:lock:" + seatId;
		
		// Redis에서 현재 락을 가진 유저 확인
		String lockedUserId = redisTemplate.opsForValue().get(lockKey);
		
		// 락이 없거나 권한이 없는 유저인 경우 예외 처리
		if (lockedUserId == null) {
			// 5분이 지나 만료됐거나 락이 없는 경우
			return;
		}
		
		if (!lockedUserId.equals(String.valueOf(userId))) {
			throw new BusinessException(ErrorCode.LOCK_UNAUTHORIZED);
		}
		
		// 권한 확인 후 Redis에서 락 제거
		redisTemplate.delete(lockKey);
		
		// DB 상태 원복
		Seat seat = seatRepository.findById(seatId)
				.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
		
		seat.release();
		
		// 취소할 예매 내역이 있다면 취소 상태로 업데이트
		reservationRepository.findBySeat_SeatIdAndUserIdAndStatus(seatId, userId, ReservationStatus.PENDING)
						.ifPresent(Reservation::cancel);
	}
}
