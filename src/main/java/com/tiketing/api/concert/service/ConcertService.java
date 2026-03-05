package com.tiketing.api.concert.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.concert.dto.ConcertRequest;
import com.tiketing.api.concert.dto.ConcertResponse;
import com.tiketing.api.concert.repository.ConcertRepository;
import com.tiketing.api.concert.repository.ConcertScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {
	
	private final ConcertRepository concertRepository;
	private final ConcertScheduleRepository concertScheduleRepository;
	
	// 콘서트 목록 조회 (전체 리스트)
	public Slice<ConcertResponse.Summary> getConcerts(ConcertRequest.SearchCondition searchCondition, Pageable pageable) {
		return concertRepository.searchConcerts(searchCondition, pageable)
				.map(ConcertResponse.Summary::new);
	}
}
