package com.tiketing.api.reservation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.enums.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	
	Optional<Reservation> findBySeat_SeatIdAndUserIdAndStatus(
			Long seatId,
			Long userId,
			ReservationStatus status
	);
}
