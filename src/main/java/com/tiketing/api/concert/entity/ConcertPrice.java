package com.tiketing.api.concert.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "concert_price")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertPrice extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concert_price_id")
	private Long concertPriceId;
	
	@Column(name = "seat_rating", nullable = false)
	private String seatRating;
	
	@Column(name = "seat_price", nullable = false)
	private Long seatPrice;
	
	@Column(name = "seat_count", nullable = false)
	private Integer seatCount;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_id")
	private Concert concert;
	
	@Builder
	public ConcertPrice(String seatRating, Long seatPrice, Integer seatCount) {
		this.seatRating = seatRating;
		this.seatPrice = seatPrice;
		this.seatCount = seatCount;
	}
	
	public void setConcert(Concert concert) {
		this.concert = concert;
	}

}
