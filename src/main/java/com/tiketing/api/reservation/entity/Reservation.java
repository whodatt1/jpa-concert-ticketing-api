package com.tiketing.api.reservation.entity;

import java.util.ArrayList;
import java.util.List;

import com.tiketing.api.global.entity.BaseEntity;
import com.tiketing.api.reservation.enums.ReservationStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reservation_id")
	private Long reservationId;
	
	@Column(name = "user_id", nullable = false)
	private Long userId;
	
	@Column(name = "concert_id", nullable = false)
	private Long concertId;
	
	@Column(name = "total_amount", nullable = false)
	private Long totalAmount;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "reservation_status", nullable = false)
	private ReservationStatus status = ReservationStatus.PENDING; // 결제 대기
	
	@OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
	private List<ReservationItem> items = new ArrayList<>();
	
	public void addItem(ReservationItem reservationItem) {
		this.items.add(reservationItem);
		reservationItem.setReservation(this);
	}
}
