package com.tiketing.api.concert.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tiketing.api.concert.dto.ConcertRequest;
import com.tiketing.api.concert.dto.ConcertResponse;
import com.tiketing.api.concert.service.ConcertService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Concert API", description = "콘서트 조회 및 관리 API") // 컨트롤러 이름표
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/concerts")
public class ConcertController {
	
	private final ConcertService concertService;
	
	@Operation(summary = "콘서트 목록 조회", description = "다양한 조건(지역, 이름, 마감일 등)으로 콘서트 목록을 조회합니다.") // API 설명
	@GetMapping
	public ResponseEntity<Slice<ConcertResponse.Summary>> getConcerts(
				@ModelAttribute ConcertRequest.SearchCondition searchCondition,
				@PageableDefault(size = 10) Pageable pageable
			) {
		return ResponseEntity.ok(concertService.getConcerts(searchCondition, pageable));
	}
	
	@Operation(summary = "콘서트 대량 생성", description = "새로운 콘서트와 관련 카테고리, 스케줄, 가격, 그리고 수만 개의 좌석 정보를 한 번에 생성합니다.")
	@PostMapping
	public ResponseEntity<Long> createConcert(@Valid @RequestBody ConcertRequest.Create request) {
		
		// Service 로직 호출
		Long concertId = concertService.createConcert(request);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(concertId);
	}
}
