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
	
	// 예매
	public void reserve() {
		if (this.getStatus() != SeatStatus.AVAILABLE) {
			throw new IllegalStateException("예매할 수 없는 좌석입니다.");
		}
		this.status = SeatStatus.LOCKED;
	}
	
	// 결제 후 확정
	public void confirm() {
		if (this.getStatus() != SeatStatus.LOCKED) {
        	throw new IllegalStateException("결제할 수 없는 상태의 좌석입니다.");
        }
		this.status = SeatStatus.SOLD;
	}
	
	// 락 해제 및 초기 상태로 원복
	public void release() {
		this.status = SeatStatus.AVAILABLE;
	}
}
