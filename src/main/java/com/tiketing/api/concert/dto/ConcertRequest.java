package com.tiketing.api.concert.dto;

import java.util.List;

import com.tiketing.api.concert.enums.ConcertRating;

public class ConcertRequest {
	
	// 콘서트 목록 조회 조건
	public record SearchCondition(
		String concertName,
		List<Long> categoryIds,
		List<String> regions,
		Integer daysLeft,
		ConcertRating rating
	) {}
}
