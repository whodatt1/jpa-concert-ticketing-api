package com.tiketing.api.concert.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.tiketing.api.concert.entity.Concert;
import com.tiketing.api.concert.entity.ConcertPrice;
import com.tiketing.api.concert.entity.ConcertSchedule;
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
	
	// 콘서트 디테일 조회 (단일)
	public record Detail(
		Long concertId,
		String concertName,
		String concertDescription,
		LocalDateTime startedAt,
		LocalDateTime endedAt,
		ConcertRating rating,
		String venueName,
		List<PriceInfo> prices,
		List<ScheduleInfo> schedules
	) {
		public static Detail from(Concert concert) {
			return new Detail(
					concert.getConcertId(),
					concert.getConcertName(),
					concert.getConcertDescription(),
					concert.getStartedAt(),
					concert.getEndedAt(),
					concert.getRating(),
					concert.getVenue() != null ? concert.getVenue().getVenueName() : "미정",
					concert.getConcertPrices().stream()
						   .map(PriceInfo::from)
						   .toList(),
					concert.getSchedules().stream()
					       .map(ScheduleInfo::from)
					       .toList()
			);
		}
	}
	
	public record PriceInfo(
		Long concertPriceId,
		String seatRating,
		Long seatPrice
	) {
		public static PriceInfo from(ConcertPrice price) {
			return new PriceInfo(
					price.getConcertPriceId(),
					price.getSeatRating(),
					price.getSeatPrice()
			);
		}
	}
	
	public record ScheduleInfo(
		Long concertScheduleId,
		LocalDate scheduleDate,
		LocalTime scheduleTime
	) {
		public static ScheduleInfo from(ConcertSchedule schedule) {
			return new ScheduleInfo(
					schedule.getConcertScheduleId(),
					schedule.getScheduleDate(),
					schedule.getScheduleTime()
			);
		}
	}
}
