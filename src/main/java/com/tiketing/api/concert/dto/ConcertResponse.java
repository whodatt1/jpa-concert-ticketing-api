package com.tiketing.api.concert.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tiketing.api.concert.entity.Concert;
import com.tiketing.api.concert.enums.ConcertRating;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.SeatStatus;

public class ConcertResponse {
	
	// 콘서트 목록 조회 (전체 리스트)
	public record Summary(
		Long concertId,
		String concertName,
		ConcertRating rating,
		LocalDateTime startedAt,
		LocalDateTime endedAt
	) {
		public Summary(Concert concert) {
			this(
					concert.getConcertId(),
					concert.getConcertName(),
					concert.getRating(),
					concert.getStartedAt(),
					concert.getEndedAt()
				);
		}
	}
}
