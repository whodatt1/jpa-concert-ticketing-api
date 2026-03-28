package com.tiketing.api.global.init;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tiketing.api.concert.dto.ConcertRequest;
import com.tiketing.api.concert.entity.Category;
import com.tiketing.api.concert.entity.Venue;
import com.tiketing.api.concert.enums.ConcertRating;
import com.tiketing.api.concert.repository.CategoryRepository;
import com.tiketing.api.concert.repository.ConcertRepository;
import com.tiketing.api.concert.repository.VenueRepository;
import com.tiketing.api.concert.service.ConcertService;
import com.tiketing.api.global.entity.Address;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
	
	private final ConcertRepository concertRepository;
	private final VenueRepository venueRepository;
	private final CategoryRepository categoryRepository;
	private final ConcertService concertService; // 🌟 완벽하게 만들어진 Service 주입!
	
	@Override
	public void run(String... args) throws Exception {
		
		if (concertRepository.count() > 0) {
			return;
		}
		
		System.out.println("데이터 초기화: 카테고리, 공연장, 콘서트 및 좌석 데이터 생성 중...");
		
		// 1. 공연장 마스터 데이터
		Venue seoulVenue = venueRepository.save(Venue.builder().venueName("서울 올림픽 체조경기장").capacity(15000).address(new Address("서울", "올림픽로 424", "05540")).build());
		Venue gyeonggiVenue = venueRepository.save(Venue.builder().venueName("일산 킨텍스").capacity(20000).address(new Address("경기", "킨텍스로 217-60", "10390")).build());
		Venue busanVenue = venueRepository.save(Venue.builder().venueName("부산 벡스코").capacity(10000).address(new Address("부산", "APEC로 55", "48060")).build());
		
		// 2. 카테고리 마스터 데이터
		Category cat1 = categoryRepository.save(Category.builder().categoryName("발라드").build());
		Category cat2 = categoryRepository.save(Category.builder().categoryName("힙합").build());
		Category cat3 = categoryRepository.save(Category.builder().categoryName("EDM").build());
		Category cat4 = categoryRepository.save(Category.builder().categoryName("락").build());
		Category[] categories = {cat1, cat2, cat3, cat4};
		
		// 3. 콘서트 + 스케줄 + 가격 + 좌석(Batch Insert) 일괄 생성
		for (int i = 1; i <= 30; i++) {
			Venue targetVenue = (i % 3 == 0) ? busanVenue : (i % 2 == 0) ? gyeonggiVenue : seoulVenue;
			String city = targetVenue.getAddress().getCity();
			
			// 날짜 겹침 방지를 위해 5일 간격으로 배정
			LocalDateTime startedAt = LocalDateTime.now().plusDays(i * 5L);
			LocalDateTime endedAt = startedAt.plusDays(2);
			
			ConcertRating rating = (i % 3 == 0) ? ConcertRating.ALL : (i % 2 == 0) ? ConcertRating.AGE_12 : ConcertRating.AGE_15;
			
			// 1. 스케줄 DTO 조립
			List<ConcertRequest.ScheduleDto> schedules = List.of(
			        new ConcertRequest.ScheduleDto(startedAt.toLocalDate(), LocalTime.of(19, 0))
			);

			// 2. 가격/좌석 DTO 조립
			List<ConcertRequest.PriceDto> prices = List.of(
			        new ConcertRequest.PriceDto("VIP", 150000L, 50),
			        new ConcertRequest.PriceDto("R", 120000L, 100)
			);

			// 3. Request DTO 조립
			ConcertRequest.Create request = new ConcertRequest.Create(
			        "콘서트 - " + city + i,       // 1. concertName
			        "더미 콘서트 설명입니다.",      // 2. concertDescription
			        rating,                       // 3. rating
			        startedAt,                    // 4. startedAt
			        endedAt,                      // 5. endedAt
			        targetVenue.getVenueId(),     // 6. venueId
			        List.of(categories[i % 4].getCategoryId(), categories[(i + 1) % 4].getCategoryId()), // 7. categoryIds
			        schedules,                    // 8. schedules
			        prices                        // 9. prices
			);

			concertService.createConcert(request);
		}
		
		System.out.println("더미 데이터 생성 완료!");
	}
}