package com.tiketing.api.reservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tiketing.api.reservation.dto.SeatRequest;
import com.tiketing.api.reservation.entity.Seat;
import com.tiketing.api.reservation.enums.SeatStatus;

public interface SeatRepository extends JpaRepository<Seat, Long>, SeatRepositoryCustom {
	
	@Query("SELECT s FROM Seat s " +
	         "JOIN s.concertSchedule cs " +
	         "JOIN cs.concert c " +
	        "WHERE (:scheduleId IS NULL OR cs.concertScheduleId = :scheduleId) " +
	          "AND (:concertId IS NULL OR c.concertId = :concertId) " +
	          "AND (:#{#condition.status()} IS NULL OR s.status = :#{#condition.status()}) " +
	          "AND (:#{#condition.seatRating()} IS NULL OR s.seatRating = :#{#condition.seatRating()}) " +
	     "ORDER BY s.seatId ASC")
    List<Seat> findSeatsByCondition(
            @Param("concertId") Long concertId,
            @Param("scheduleId") Long scheduleId,
            @Param("condition") SeatRequest.SearchCondition condition
    );

	List<Seat> findAllByStatus(SeatStatus status);
}
