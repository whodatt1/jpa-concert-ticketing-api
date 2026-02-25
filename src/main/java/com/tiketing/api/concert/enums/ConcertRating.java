package com.tiketing.api.concert.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConcertRating {
	
	ALL("전체 관람가", 0),
	AGE_12("12세 이상 관람가", 12),
	AGE_15("15세 이상 관람가", 15),
	AGE_19("청소년 관람 불가", 12);
	
	private final String description;
	private final int minAge;
}
