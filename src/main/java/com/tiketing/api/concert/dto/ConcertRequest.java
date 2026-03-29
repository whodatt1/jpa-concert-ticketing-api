package com.tiketing.api.concert.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import com.tiketing.api.concert.enums.ConcertRating;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ConcertRequest {
	
	// 콘서트 목록 조회 조건
	@Schema(name = "ConcertSearchCondition")
	public record SearchCondition(
		@Schema(description = "검색할 콘서트 이름 (부분 일치 검색 지원)", example = "콘서트 - 서울")
		String concertName,
		@Schema(description = "검색할 카테고리 ID 목록 (1: 발라드, 2: 힙합, 3: EDM, 4: 락)", example = "[1, 3]")
		Set<Long> categoryIds,
		@Schema(description = "검색할 지역 목록 (예: 서울, 경기, 부산)", example = "[\"서울\", \"부산\"]")
		List<String> regions,
		@Schema(description = "마감 임박 기준 일수 (예: 7 입력 시 오늘부터 7일 이내에 종료되는 공연 검색)", example = "7")
		Integer daysLeft,
		@Schema(description = "관람 등급 필터 (예: ALL, AGE_12, AGE_15, AGE_19)", example = "AGE_15")
		ConcertRating rating
	) {}
	
	public record Create(
			@Schema(description = "콘서트 공식 명칭", example = "2026 월드 투어 콘서트 - 서울")
			@NotBlank(message = "콘서트 이름은 필수입니다.")
			String concertName,
			
			@Schema(description = "콘서트 상세 설명", example = "초대형 스케일의 무대와 함께하는 감동의 시간")
			String concertDescription,
			 
			@Schema(description = "관람 등급 (ALL, AGE_12, AGE_15, AGE_19)", example = "ALL")
			@NotNull(message = "관람 등급을 필수입니다.")
			ConcertRating rating,
			
			@Schema(description = "콘서트 시작 일시 (예매 오픈 또는 공연 시작 기준)", type = "string", format = "date-time", example = "2026-05-01T19:00:00")
			@NotNull(message = "시작 일시는 필수입니다.")
			LocalDateTime startedAt,
			
			@Schema(description = "콘서트 종료 일시", type = "string", format = "date-time", example = "2026-05-03T22:00:00")
			@NotNull(message = "종료 일시는 필수입니다.")
			LocalDateTime endedAt,
			
			@Schema(description = "공연장 ID (마스터 데이터 - 1: 서울 올림픽 체조경기장, 2: 일산 킨텍스, 3: 부산 벡스코)", example = "1")
			@NotNull(message = "공연장 ID는 필수입니다.")
			Long venueId,
			
			@Schema(description = "카테고리 ID 목록 (마스터 데이터 - 1: 발라드, 2: 힙합, 3: EDM, 4: 락)", example = "[1, 2]")
			@NotEmpty(message = "최소 1개 이상의 카테고리를 선택해야 합니다.")
			List<Long> categoryIds,
			
			@Schema(description = "콘서트 회차(스케줄) 목록")
			@Valid
			@NotEmpty(message = "최소 1개 이상의 스케쥴(회차) 정보가 필요합니다.")
			List<ScheduleDto> schedules,
			
			@Schema(description = "좌석 등급 및 가격 정보 목록")
			@Valid
			@NotEmpty(message = "최소 1개 이상의 가격정보가 필요합니다.")
			List<PriceDto> prices
	) {}
	
	// =======================================================
	// Create DTO 내부에서 조립 부품으로 쓰일 하위 DTO들
	// =======================================================
	public record ScheduleDto(
					@Schema(description = "공연 날짜", type = "string", format = "date", example = "2026-05-01")
					@NotNull(message = "공연 날짜는 필수입니다.")
					LocalDate scheduleDate,
					
					@Schema(description = "공연 시간 (HH:mm:ss)", type = "string", format = "time", example = "19:00:00")
					@NotNull(message = "공연 시간은 필수입니다.")
					LocalTime scheduleTime
	) {}
	
	public record PriceDto(
					@Schema(description = "좌석 등급 명칭 (예: VIP, R, S)", example = "VIP")
					@NotBlank(message = "좌석 등급은 필수입니다.")
					String seatRating,
					
					@Schema(description = "해당 등급의 1석당 가격", example = "150000")
					@NotNull(message = "좌석 가격은 필수입니다.")
					@Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
					Long seatPrice,
					
					@Schema(description = "해당 등급으로 발행할 총 좌석 수 (Batch Insert 대상)", example = "500")
					@NotNull(message = "좌석 수는 필수입니다.")
					@Min(value = 1, message = "좌석 수는 최소 1개 이상이어야 합니다.")
					Integer seatCount
	) {}
}
