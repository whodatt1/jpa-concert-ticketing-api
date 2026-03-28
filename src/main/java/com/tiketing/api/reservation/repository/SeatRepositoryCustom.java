package com.tiketing.api.reservation.repository;

import java.util.List;

import com.tiketing.api.reservation.dto.SeatRequest;
import com.tiketing.api.reservation.entity.Seat;

public interface SeatRepositoryCustom {
	List<Seat> getSeats(Long concertId, Long scheduleId, SeatRequest.SearchCondition searchCondition);
}
