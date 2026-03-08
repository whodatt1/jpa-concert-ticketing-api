package com.tiketing.api.concert.entity;

import com.tiketing.api.global.entity.Address;
import com.tiketing.api.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "venue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Venue extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "venue_id")
	private Long venueId;
	
	@Column(name = "venue_name", nullable = false)
	private String venueName;
	
	@Column(name = "capacity")
	private Integer capacity; // 수용 인원
	
	@Embedded
	private Address address;
	
	@Builder
	public Venue(String venueName, Integer capacity, Address address) {
		this.venueName = venueName;
        this.capacity = capacity;
        this.address = address;
	}
}
