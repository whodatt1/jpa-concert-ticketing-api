package com.tiketing.api.reservation.repository;

import java.util.List;

import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import static com.tiketing.api.concert.entity.QConcert.concert;
import static com.tiketing.api.concert.entity.QConcertSchedule.concertSchedule;
import static com.tiketing.api.reservation.entity.QSeat.seat;

import com.tiketing.api.reservation.dto.SeatRequest;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.SeatStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepositoryCustom {
	
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Seat> getSeats(Long concertId, Long scheduleId, SeatRequest.SearchCondition searchCondition) {
		
		return queryFactory
				.selectFrom(seat)
				.join(seat.concertSchedule, concertSchedule)
				.join(concertSchedule.concert, concert)
				.where(
						concertScheduleIdEq(scheduleId),
						concertIdEq(concertId),
						
						seatStatusEq(searchCondition.status()),
						seatRatingEq(searchCondition.seatRating())
				)
				.orderBy(seat.seatId.asc())
				.fetch();
	}
	
	private BooleanExpression seatRatingEq(String seatRating) {
		return StringUtils.hasText(seatRating) ? seat.seatRating.eq(seatRating) : null;
	}
	
	private BooleanExpression seatStatusEq(SeatStatus status) {
		if (status == null) {
			return null;
		}
		
		return seat.status.eq(status);
	}
	
	private BooleanExpression concertScheduleIdEq(Long scheduleId) {
		if (scheduleId == null || scheduleId <= 0) {
			return null;
		}
		
		return concertSchedule.concertScheduleId.eq(scheduleId);
	}
	
	private BooleanExpression concertIdEq(Long concertId) {
		if (concertId == null || concertId <= 0) {
			return null;
		}
		
		return concert.concertId.eq(concertId);
	}
}
