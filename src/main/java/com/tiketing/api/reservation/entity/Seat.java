package com.tiketing.api.reservation.entity;

import com.tiketing.api.concert.entity.ConcertSchedule;
import com.tiketing.api.global.entity.BaseEntity;
import com.tiketing.api.reservation.enums.SeatStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "seat_id")
	private Long seatId;
	
	@Column(name = "seat_rating", nullable = false)
	private String seatRating;
	
	@Column(name = "seat_name", nullable = false)
	private String seatName;
	
	@Column(name = "seat_price", nullable = false)
	private Long seatPrice;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "seat_status", nullable = false)
	private SeatStatus status = SeatStatus.AVAILABLE;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_schedule_id", nullable = false)
	private ConcertSchedule concertSchedule;
}
