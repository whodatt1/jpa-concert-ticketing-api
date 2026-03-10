package com.tiketing.api.concert.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.concert.dto.ConcertRequest;
import com.tiketing.api.concert.dto.ConcertResponse;
import com.tiketing.api.concert.entity.Category;
import com.tiketing.api.concert.entity.Concert;
import com.tiketing.api.concert.entity.ConcertCategory;
import com.tiketing.api.concert.entity.ConcertPrice;
import com.tiketing.api.concert.entity.ConcertSchedule;
import com.tiketing.api.concert.entity.Venue;
import com.tiketing.api.concert.repository.CategoryRepository;
import com.tiketing.api.concert.repository.ConcertRepository;
import com.tiketing.api.concert.repository.VenueRepository;
import com.tiketing.api.reservation.repository.SeatJdbcRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {
	
	private final ConcertRepository concertRepository;
	private final CategoryRepository categoryRepository;
	private final VenueRepository venueRepository;
	private final SeatJdbcRepository seatJdbcRepository;
	
	@Transactional
	public Long createConcert(ConcertRequest.Create request) {
		
		// 공연장 검증
		Venue venue = venueRepository.findById(request.venueId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공연장입니다."));
		
		// 좌석 요청값
		int totalRequestedSeats = request.prices().stream()
				.mapToInt(ConcertRequest.PriceDto::seatCount)
				.sum();
		
		// 수용인원 유효성 체크
		if (venue.getCapacity() != null && totalRequestedSeats > venue.getCapacity()) {
			throw new IllegalArgumentException("총 좌석 수(" + totalRequestedSeats + ")가 공연장 수용 인원(" + venue.getCapacity() + ")을 초과합니다.");
		}
		
		Concert concert = Concert.builder()
				.concertName(request.concertName())
				.concertDescription(request.concertDescription())
				.venue(venue)
				.rating(request.rating())
				.startedAt(request.startedAt())
				.endedAt(request.endedAt())
				.build();
		
		// 카테고리 조립
		List<Category> categories = categoryRepository.findAllById(request.categoryIds());
		if (categories.size() != request.categoryIds().size()) {
			throw new IllegalArgumentException("유효하지 않은 카테고리 ID가 포함되어 있습니다.");
		}
		for (Category category : categories) {
			ConcertCategory concertCategory = ConcertCategory.builder()
					.concert(concert)
					.category(category)
					.build();
			concert.addConcertCategory(concertCategory);
		}
		
		for (ConcertRequest.ScheduleDto scheduleDto : request.schedules()) {
			ConcertSchedule schedule = ConcertSchedule.builder()
					.scheduleDate(scheduleDto.scheduleDate())
					.scheduleTime(scheduleDto.scheduleTime())
					.build();
			concert.addSchedule(schedule);
		}
		
		for (ConcertRequest.PriceDto priceDto : request.prices()) {
			ConcertPrice price = ConcertPrice.builder()
					.seatRating(priceDto.seatRating())
					.seatPrice(priceDto.seatPrice())
					.seatCount(priceDto.seatCount())
					.build();
			concert.addConcertPrice(price);
		}
		
		// JPA를 통한 1차 저장 (Cascade)
        // 이 순간 Concert, ConcertCategory, ConcertSchedule, ConcertPrice 테이블에 INSERT 쿼리가 날아가고, 각각의 PK(ID)가 발급됩니다.
		Concert savedConcert = concertRepository.save(concert);
		
		// 발급된 스케줄 ID를 활용하여 좌석(Seat) DTO 대량 조립
        // Seat 엔티티를 생성하지 않고, DB에 전달할 Dto 사용
        List<SeatJdbcRepository.SeatBatchDto> seatBatchList = new ArrayList<>();
        
        for (ConcertSchedule schedule : savedConcert.getSchedules()) {
        	for (ConcertPrice price : savedConcert.getConcertPrices()) {
        		for (int i = 1; i <= price.getSeatCount(); i++) {
        			
        			String seatName = price.getSeatRating() + "-" + i;
        			
        			seatBatchList.add(new SeatJdbcRepository.SeatBatchDto(
        					schedule.getConcertScheduleId(),
        					seatName,
        					price.getSeatRating(),
        					price.getSeatPrice()
        			));
        		}
        	}
        }
        
        if (!seatBatchList.isEmpty()) {
        	seatJdbcRepository.batchInsertSeats(seatBatchList);
        }
        
        return savedConcert.getConcertId();
	}
	
	// 콘서트 목록 조회 (전체 리스트)
	public Slice<ConcertResponse.Summary> getConcerts(ConcertRequest.SearchCondition searchCondition, Pageable pageable) {
		return concertRepository.searchConcerts(searchCondition, pageable)
				.map(ConcertResponse.Summary::new);
	}
}
