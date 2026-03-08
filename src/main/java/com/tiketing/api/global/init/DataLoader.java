package com.tiketing.api.global.init;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tiketing.api.concert.entity.Category;
import com.tiketing.api.concert.entity.Concert;
import com.tiketing.api.concert.entity.ConcertCategory;
import com.tiketing.api.concert.entity.Venue;
import com.tiketing.api.concert.enums.ConcertRating;
import com.tiketing.api.concert.repository.ConcertRepository;
import com.tiketing.api.global.entity.Address;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
	
	private final ConcertRepository concertRepository;
	private final EntityManager em; // Repository를 안 만들고 직접 영속화하기 위해 주입!
	
	@Override
	@Transactional // EntityManager를 통한 쓰기 작업이 있으므로 추가
	public void run(String... args) throws Exception {
		
		// 데이터가 이미 존재할 경우 return
		if (concertRepository.count() > 0) {
			return;
		}
		
		System.out.println("데이터 초기화: 카테고리 및 콘서트 데이터 생성 중...");
		
		// ==========================================
		// 1. 공연장(Venue) 마스터 데이터 3개 생성
		// ==========================================
		Venue seoulVenue = Venue.builder().venueName("서울 올림픽 체조경기장").capacity(15000).address(new Address("서울", "올림픽로 424", "05540")).build();
		Venue gyeonggiVenue = Venue.builder().venueName("일산 킨텍스").capacity(20000).address(new Address("경기", "킨텍스로 217-60", "10390")).build();
		Venue busanVenue = Venue.builder().venueName("부산 벡스코").capacity(10000).address(new Address("부산", "APEC로 55", "48060")).build();
		
		em.persist(seoulVenue);
		em.persist(gyeonggiVenue);
		em.persist(busanVenue);
		
		// ==========================================
        // 1. 카테고리(Category) 더미 데이터 생성
        // ==========================================
		
		Category cat1 = Category.builder().categoryName("발라드").build();
		Category cat2 = Category.builder().categoryName("힙합").build();
		Category cat3 = Category.builder().categoryName("EDM").build();
		Category cat4 = Category.builder().categoryName("락").build();
		
		em.persist(cat1);
		em.persist(cat2);
		em.persist(cat3);
		em.persist(cat4);
		
		Category[] categories = {cat1, cat2, cat3, cat4};
		
		// ==========================================
        // 2. 콘서트(Concert) 더미 데이터 30개 생성
        // ==========================================
		for (int i = 1; i <= 30; i++) {
			// 순서대로 서울, 경기, 부산 공연장 매핑
			Venue targetVenue = (i % 3 == 0) ? busanVenue : (i % 2 == 0) ? gyeonggiVenue : seoulVenue;
			String city = targetVenue.getAddress().getCity();
			
			LocalDateTime startedAt = LocalDateTime.now().plusDays(i - 10);
			LocalDateTime endedAt = startedAt.plusDays(2);
			
			Concert concert = Concert.builder()
					.concertName("콘서트 - " + city + i)
					.venue(targetVenue)
					.rating((i % 3 == 0) ? ConcertRating.ALL : (i % 2 == 0) ? ConcertRating.AGE_12 : ConcertRating.AGE_15)
					.startedAt(startedAt)
					.endedAt(endedAt)
					.build();
			
			concertRepository.save(concert);
			
			// ==========================================
            // 3. 콘서트-카테고리 매핑 (ConcertCategory) 연결
            // ==========================================
			Category category1 = categories[i % 4]; // 카테고리 하나씩 순회 배정
			Category category2 = categories[(i + 1) % 4];
			
			ConcertCategory concertCategory1 = ConcertCategory.builder()
					.concert(concert)
					.category(category1)
					.build();
			
			ConcertCategory concertCategory2 = ConcertCategory.builder()
					.concert(concert)
					.category(category2)
					.build();
			
			em.persist(concertCategory1);
			em.persist(concertCategory2);
		}
		
		System.out.println("더미 데이터 생성 완료!");
		
	}
}
