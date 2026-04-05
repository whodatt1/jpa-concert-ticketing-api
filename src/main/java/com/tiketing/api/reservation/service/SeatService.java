package com.tiketing.api.reservation.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.global.config.RedissonConfig;
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
	//private final StringRedisTemplate redisTemplate;
	private final RedissonClient redissonClient;
	
	// 특정 회차 좌석 맵 조회
	public List<SeatResponse.Summary> getSeats(Long concertId, Long scheduleId, SeatRequest.SearchCondition searchCondition) {
		
		return seatRepository.getSeats(concertId, scheduleId, searchCondition)
				.stream()
				.map(SeatResponse.Summary::new)
				.toList();
	}
	
	// 5분 좌석 선점
	// 이제 Redis는 찰나의 동시성만 막고 빠짐
	// 락 유지기간이 3초, DB트랜잭션은 무조건 2초 안에 끝내거나 롤백
	@Transactional(timeout = 2)
	public void reserveSeat(Long seatId, Long userId) {
		
		// Redis에 사용할 Key 이름 만들기 (ex: "seat:lock:15")
		String lockKey = "seat:lock:" + seatId;
		// Redisson 락 객체 가져오기
		// 자물쇠를 잠그려면 먼저 RLock 객체를 가져와야 함
		RLock lock = redissonClient.getLock(lockKey);
		
		try {
			// 자물쇠 잠그기 시도 (스핀락 없이 즉시 실패, 락 유지 최대 3초)
			// - 0초: 남이 이미 잠갔으면 대기하지 않고 즉시 포기함 (스핀락 방지)
	        // - 3초: 내가 잠그는 데 성공하면, 내 작업이 3초를 넘기면 강제로 뺏김 (워치독 끄기)
			boolean isLocked = lock.tryLock(0, 3, TimeUnit.SECONDS);
			
			// 이미 잠겨있는 경우
			if (!isLocked) {
				throw new BusinessException(ErrorCode.SEAT_ALREADY_LOCKED);
			}
			
			Seat seat = seatRepository.findById(seatId)
					.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
	
			seat.reserve(); // 엔티티에게 검증 및 상태 변경을 위임
			
			// PENDING 상태 이력 저장
			reservationRepository.save(Reservation.builder()
					.userId(userId)
					.seat(seat)
					.build()); // 상태는 PENDING
			
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
		
//		// Redis에 SETNX 명령 날리기 + 5분 타이머(TTL) 설정
//		// lockKey가 비어있다면 userId를 저장 후 5분 뒤 만료 성공한다면 true 반환 중복된 키가 있다면 false
//		Boolean isLockAcquired = redisTemplate.opsForValue()
//						.setIfAbsent(lockKey, String.valueOf(userId), 5, TimeUnit.MINUTES);
//		
//		// 누군가 먼저 클릭해서 이미 열쇠가 있다면
//		if (Boolean.FALSE.equals(isLockAcquired)) {
//			throw new BusinessException(ErrorCode.SEAT_ALREADY_LOCKED);
//		}
//		
//		// 1등으로 들어온 경우 DB 물리적 상태도 변경
//		Seat seat = seatRepository.findById(seatId)
//						.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
//		
//		try {
//			seat.reserve(); // 엔티티에게 검증 및 상태 변경을 위임
//			
//			// PENDING 상태 이력 저장
//			reservationRepository.save(Reservation.builder()
//											      .userId(userId)
//											      .seat(seat)
//											      .build());
//		} catch (BusinessException e) {
//			// 엔티티가 AVAILABLE 예외를 던지면 Redis락을 제거 후 에러 전달
//			redisTemplate.delete(lockKey);
//			throw e;
//		}
	}
	
	// 예매 취소 로직도 짧게 락을 걸고 DB를 원복
	@Transactional
	public void unlockSeat(Long seatId, Long userId) {
		
		String lockKey = "seat:lock:" + seatId;
		// Redisson 락 객체 가져오기
		RLock lock = redissonClient.getLock(lockKey);
		
		try {
			// 3초 대기, 3초 유지
			boolean isLocked = lock.tryLock(3, 3, TimeUnit.SECONDS);
			// 3초 뒤에도 락을 못얻었다면 취소 실패처리
			if (!isLocked) return;
			
			Reservation reservation = reservationRepository.findBySeat_SeatIdAndUserIdAndStatus(seatId, userId, ReservationStatus.PENDING)
					.orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

			Seat seat = seatRepository.findById(seatId)
					.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
			
			seat.release();
			reservation.cancel();
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			if (lock.isLocked() && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
		
//		// Redis에서 현재 락을 가진 유저 확인
//		String lockedUserId = redisTemplate.opsForValue().get(lockKey);
//		
//		// 락이 없거나 권한이 없는 유저인 경우 예외 처리
//		if (lockedUserId == null) {
//			// 5분이 지나 만료됐거나 락이 없는 경우
//			return;
//		}
//		
//		if (!lockedUserId.equals(String.valueOf(userId))) {
//			throw new BusinessException(ErrorCode.LOCK_UNAUTHORIZED);
//		}
//		
//		// 권한 확인 후 Redis에서 락 제거
//		redisTemplate.delete(lockKey);
//		
//		// DB 상태 원복
//		Seat seat = seatRepository.findById(seatId)
//				.orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
//		
//		seat.release();
//		
//		// 취소할 예매 내역이 있다면 취소 상태로 업데이트
//		reservationRepository.findBySeat_SeatIdAndUserIdAndStatus(seatId, userId, ReservationStatus.PENDING)
//						.ifPresent(Reservation::cancel);
	}
}
