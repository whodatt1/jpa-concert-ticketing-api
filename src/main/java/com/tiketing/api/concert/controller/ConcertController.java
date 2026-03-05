package com.tiketing.api.concert.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiketing.api.concert.dto.ConcertRequest;
import com.tiketing.api.concert.dto.ConcertResponse;
import com.tiketing.api.concert.service.ConcertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/concerts")
public class ConcertController {
	
	private final ConcertService concertService;
	
	@GetMapping
	public ResponseEntity<Slice<ConcertResponse.Summary>> getConcerts(
				@ModelAttribute ConcertRequest.SearchCondition searchCondition,
				@PageableDefault(size = 10) Pageable pageable
			) {
		return ResponseEntity.ok(concertService.getConcerts(searchCondition, pageable));
	}
}
