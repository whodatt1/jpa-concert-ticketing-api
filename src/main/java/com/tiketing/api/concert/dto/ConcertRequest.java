package com.tiketing.api.concert.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
		String concertName,
		List<Long> categoryIds,
		List<String> regions,
		Integer daysLeft,
		ConcertRating rating
	) {}
	
	public record Create(
			@NotBlank(message = "콘서트 이름은 필수입니다.")
			String concertName,
			
			String concertDescription,
			
			@NotNull(message = "관람 등급을 필수입니다.")
			ConcertRating rating,
			
			@NotNull(message = "시작 일시는 필수입니다.")
			LocalDateTime startedAt,
			
			@NotNull(message = "종료 일시는 필수입니다.")
			LocalDateTime endedAt,
			
			@NotNull(message = "공연장 ID는 필수입니다.")
			Long venueId,
			
			@NotEmpty(message = "최소 1개 이상의 카테고리를 선택해야 합니다.")
			List<Long> categoryIds,
			
			@Valid
			@NotEmpty(message = "최소 1개 이상의 스케쥴(회차) 정보가 필요합니다.")
			List<ScheduleDto> schedules,
			
			@Valid
			@NotEmpty(message = "최소 1개 이상의 가격정보가 필요합니다.")
			List<PriceDto> prices
	) {}
	
	// =======================================================
	// Create DTO 내부에서 조립 부품으로 쓰일 하위 DTO들
	// =======================================================
	public record ScheduleDto(
					@NotNull(message = "공연 날짜는 필수입니다.")
					LocalDate scheduleDate,
					
					@NotNull(message = "공연 시간은 필수입니다.")
					@Schema(type = "string", example = "19:00:00", description = "공연 시간 (HH:mm:ss)")
					LocalTime scheduleTime
	) {}
	
	public record PriceDto(
					@NotBlank(message = "좌석 등급은 필수입니다.")
					String seatRating,
					
					@NotNull(message = "좌석 가격은 필수입니다.")
					@Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
					Long seatPrice,
					
					@NotNull(message = "좌석 수는 필수입니다.")
					@Min(value = 1, message = "좌석 수는 최소 1개 이상이어야 합니다.")
					Integer seatCount
	) {}
}
