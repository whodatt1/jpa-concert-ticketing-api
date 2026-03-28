package com.tiketing.api.reservation.entity;

import com.tiketing.api.concert.entity.ConcertSchedule;
import com.tiketing.api.global.entity.BaseEntity;
import com.tiketing.api.global.exception.BusinessException;
import com.tiketing.api.global.exception.ErrorCode;
import com.tiketing.api.reservation.enums.SeatStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "seat",
		indexes = {
					@Index(name = "idx_seat_search", columnList = "concert_schedule_id")
			}
	    )
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
			throw new BusinessException(ErrorCode.SEAT_ALREADY_LOCKED);
		}
		this.status = SeatStatus.LOCKED;
	}
	
	// 결제 후 확정
	public void confirm() {
		if (this.status != SeatStatus.LOCKED) {
			throw new BusinessException(ErrorCode.INVALID_SEAT_STATUS); 
		}
		this.status = SeatStatus.SOLD;
	}
	
	// 락 해제 및 초기 상태로 원복
	public void release() {
		this.status = SeatStatus.AVAILABLE;
	}
}
