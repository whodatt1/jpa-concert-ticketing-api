package com.tiketing.api.concert.dto;

import com.tiketing.api.reservation.enums.SeatStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public class SeatRequest {
	
	@Schema(name = "SeatSearchCondition")
	public record SearchCondition(
		SeatStatus status,
		String seatRating
	) {}
}
