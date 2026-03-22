package com.tiketing.api.concert.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tiketing.api.concert.dto.ConcertRequest;
import com.tiketing.api.concert.dto.ConcertResponse;
import com.tiketing.api.concert.dto.SeatRequest;
import com.tiketing.api.concert.dto.SeatResponse;
import com.tiketing.api.reservation.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {
	
	private final SeatRepository seatRepository;
	
	// 특정 회차 좌석 맵 조회
	public List<SeatResponse.Summary> getSeats(Long concertId, Long scheduleId, SeatRequest.SearchCondition searchCondition) {
		
		return seatRepository.getSeats(concertId, scheduleId, searchCondition)
				.stream()
				.map(SeatResponse.Summary::new)
				.toList();
	}
}
