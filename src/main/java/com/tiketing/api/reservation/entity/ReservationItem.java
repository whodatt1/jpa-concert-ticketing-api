package com.tiketing.api.reservation.entity;

import com.tiketing.api.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "reservation_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationItem extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reservation_item_id")
	private Long reservationItemId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id", nullable = false)
	private Reservation reservation;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;
	
	@Column(name = "seat_rating", nullable = false)
	private String seatRating;
	
	@Column(name = "seat_name", nullable = false)
	private String seatName;
	
	@Column(name = "seat_price", nullable = false)
	private Long seatPrice;
	
	public void setReservation(Reservation reservation) {
		this.reservation = reservation;
	}
}
