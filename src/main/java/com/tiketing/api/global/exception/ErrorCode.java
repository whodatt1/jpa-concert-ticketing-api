package com.tiketing.api.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	
	// Concert & Venue
	CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 콘서트입니다."),
	VENUE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공연장입니다."),
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 카테고리가 포함되어 있습니다."),
	CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "공연장 수용 인원을 초과했습니다."),
	SCHEDULE_OVERLAPPED(HttpStatus.CONFLICT, "해당 기간에 이미 대관 일정이 존재합니다."),
	
	// Seat
	SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 좌석입니다."),
	SEAT_ALREADY_LOCKED(HttpStatus.CONFLICT, "이미 선점되었거나 결제 진행 중인 좌석입니다."),
	INVALID_SEAT_STATUS(HttpStatus.BAD_REQUEST, "결제할 수 없는 상태의 좌석입니다."),
	LOCK_UNAUTHORIZED(HttpStatus.FORBIDDEN, "해당 좌석의 락을 해제할 권한이 없습니다."),
	
	// Payment
	PAYMENT_TIMEOUT(HttpStatus.BAD_REQUEST, "결제 가능 시간이 초과되어 취소되었습니다.");
	
	private final HttpStatus httpStatus;
	private final String message;
}
