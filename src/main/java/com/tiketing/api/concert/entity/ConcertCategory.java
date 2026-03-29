package com.tiketing.api.concert.entity;

import com.tiketing.api.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "concert_category",
		indexes = {
					@Index(name = "idx_concert_category", columnList = "concert_id, category_id", unique = true)
			}
		)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertCategory extends BaseEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concert_category_id")
	private Long concertCategoryId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_id", nullable = false)
	private Concert concert;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
	
	public void setConcert(Concert concert) {
		this.concert = concert;
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}
	
	// DataLoader를 위한 빌더
	@Builder
	public ConcertCategory(Concert concert, Category category) {
		this.concert = concert;
		this.category = category;
	}
}
