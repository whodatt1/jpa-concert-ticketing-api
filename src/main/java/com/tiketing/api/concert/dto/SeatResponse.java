package com.tiketing.api.concert.dto;

import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.SeatStatus;

public class SeatResponse {
	
	public record Summary (
		Long seatId,
		String seatRating,
		String seatName,
		Long seatPrice,
		SeatStatus status
	) {
		public Summary(Seat seat) {
			this(
					seat.getSeatId(),
					seat.getSeatRating(),
					seat.getSeatName(),
					seat.getSeatPrice(),
					seat.getStatus()
				);
		}
	}
}
