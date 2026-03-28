           package com.tiketing.api.reservation.dto;

import com.tiketing.api.reservation.enums.SeatStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public class SeatRequest {
	
	@Schema(name = "SeatSearchCondition", description = "좌석 목록 조회 시 필터링 조건")
	public record SearchCondition(
		@Schema(description = "조회할 좌석 상태 (AVAILABLE: 예매 가능, RESERVED: 예매 완료 등)", example = "AVAILABLE")
		SeatStatus status,
		@Schema(description = "특정 좌석 등급만 필터링해서 볼 경우 (예: VIP, R, S)", example = "VIP")
		String seatRating
	) {}
}
