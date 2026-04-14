package com.tiketing.api.conert.repository;

import static com.tiketing.api.concert.entity.QConcert.concert;
import static com.tiketing.api.concert.entity.QConcertSchedule.concertSchedule;
import static com.tiketing.api.concert.entity.QConcertPrice.concertPrice;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hibernate.loader.MultipleBagFetchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

@SpringBootTest
@Transactional // 테스트 끝난 후에 롤백 보장
public class MultipleBagFetchExceptionTest {
	
	@Autowired
	private JPAQueryFactory queryFactory;
	
	@Test
	@DisplayName("JPA N + 1 예외 재현: 2개 이상의 List 컬렉션을 동시에 FetchJoin시 MultipleBagFetchException 발생")
	void multipleBagFetchException_Simulation() {
		// given
		Long targetConcertId = 1L;
		
		// when & then
		assertThatThrownBy(() -> {
			queryFactory
				.selectFrom(concert)
				.leftJoin(concert.schedules, concertSchedule).fetchJoin()
				.leftJoin(concert.concertPrices, concertPrice).fetchJoin()
				.where(concert.concertId.eq(targetConcertId))
				.fetch();
		})
		.hasRootCauseInstanceOf(MultipleBagFetchException.class);
		
		System.out.println("====== 테스트 결과 ======");
        System.out.println("MultipleBagFetchException 발생");
        System.out.println("=========================");
	}
}
