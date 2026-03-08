package com.tiketing.api.concert.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;

import com.tiketing.api.concert.enums.ConcertRating;
import com.tiketing.api.global.entity.Address;
import com.tiketing.api.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "concert",
		indexes = {
  					@Index(name = "idx_concert_search_all", columnList = "show_yn, del_yn, created_at")
			}
		)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// JPA delete 호출 시 del_yn를 Y처리하여 Soft Delete 자동화
@SQLDelete(sql = "UPDATE concert SET del_yn = 'Y' WHERE concert_id = ?")
public class Concert extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concert_id")
	private Long concertId;
	
	@Column(name = "concert_name", nullable = false)
	private String concertName;
	
	@Column(name = "concert_description")
	private String concertDescription;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "venue_id", nullable = false)
	private Venue venue;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "rating", nullable = false)
	private ConcertRating rating;
	
	@Column(name = "del_yn", nullable = false)
	private String delYn = "N"; // 기본 값을 N으로 설정
	
	@Column(name = "started_at", nullable = false)
	private LocalDateTime startedAt;
	
	@Column(name = "ended_at", nullable = false)
	private LocalDateTime endedAt;
	
	// mappedBy => 상대방 엔티티에 선언된 객체 변수명을 가리킨다. 해당 컬럼을 통해 조인
	// 명시가 없을 경우 JPA가 임의로 매핑테이블을 새로 생성
	@OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConcertCategory> concertCategories = new ArrayList<>();
	
	@OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConcertPrice> concertPrices = new ArrayList<>();
	
	@OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConcertSchedule> schedules = new ArrayList<>();
	
	public void addConcertCategory(ConcertCategory concertCategory) {
		this.concertCategories.add(concertCategory);
		concertCategory.setConcert(this);
	}
	
	public void addConcertPrice(ConcertPrice concertPrice) {
		this.concertPrices.add(concertPrice);
		concertPrice.setConcert(this);
	}
	
	public void addSchedule(ConcertSchedule concertSchedule) {
		this.schedules.add(concertSchedule);
		concertSchedule.setConcert(this);
	}
	
	@Builder
	public Concert(String concertName, String concertDescription, Venue venue, ConcertRating rating,
			LocalDateTime startedAt, LocalDateTime endedAt) {
		this.concertName = concertName;
		this.concertDescription = concertDescription;
		this.venue = venue;
		this.rating = rating;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
	}
}
