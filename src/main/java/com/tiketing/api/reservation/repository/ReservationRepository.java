package com.tiketing.api.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tiketing.api.reservation.entity.Reservation;
import com.tiketing.api.reservation.enums.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	
	Optional<Reservation> findBySeat_SeatIdAndUserIdAndStatus(
			Long seatId,
			Long userId,
			ReservationStatus status
	);
	
	@Query("SELECT r FROM Reservation r JOIN FETCH r.seat " +
	       "WHERE r.status = :status AND r.createdAt <= :timeLimit")
	List<Reservation> findExpiredReservations(@Param("status") ReservationStatus status, @Param("timeLimit") LocalDateTime timeLimit);
}
