package com.tiketing.api.concert.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.tiketing.api.concert.enums.ScheduleStatus;
import com.tiketing.api.reservation.entity.Seat;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "concert_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConcertSchedule {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concert_schedule_id")
	private Long concertScheduleId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_id", nullable = false)
	private Concert concert;
	
	@Column(name = "schedule_date", nullable = false)
	private LocalDate scheduleDate;
	
	@Column(name = "schedule_time", nullable = false)
	private LocalTime scheduleTime;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "schedule_status", nullable = false)
	private ScheduleStatus status = ScheduleStatus.OPEN;
	
	@OneToMany(mappedBy = "concert_schedule", cascade = CascadeType.ALL)
	private List<Seat> seats = new ArrayList<>();
	
	public void setConcert(Concert concert) {
		this.concert = concert;
	}
	
	public void addSeat(Seat seat) {
		this.seats.add(seat);
		seat.setConcertSchedule(this);
	}
}
