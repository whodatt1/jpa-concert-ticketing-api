package com.tiketing.api.concert.repository;

import static com.tiketing.api.concert.entity.QConcert.concert;
import static com.tiketing.api.concert.entity.QConcertCategory.concertCategory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tiketing.api.concert.dto.ConcertRequest.SearchCondition;
import com.tiketing.api.concert.entity.Concert;
import com.tiketing.api.concert.enums.ConcertRating;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepositoryCustom {
	
	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<Concert> searchConcerts(SearchCondition condition, Pageable pageable) {
		
		// Querydsl을 이용한 콘서트 리스트 조회
		List<Concert> contents = queryFactory
				.selectFrom(concert)
				// 콘서트 객체가 가진 카테고리 리스트를 펼쳐서 조인하고, 이름표(Alias)를 concertCategory로 달아두겠다는 선언
				.leftJoin(concert.concertCategories, concertCategory)
				.where(
						concertNameContains(condition.concertName()),
						categoryIdsIn(condition.categoryIds()),
						regionsIn(condition.regions()),
						daysLeftLoe(condition.daysLeft()),
						ratingEq(condition.rating()),
						concert.showYn.eq("Y"),
						concert.delYn.eq("N")
				)
				.orderBy(concert.createdAt.desc()) // 최신순 정렬
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize() + 1) // Slice 처리를 위해 요청한 사이즈보다 1개 더 조회
				.fetch();
		
		// 다음 페이지 존재 여부 확인 로직 (Slice는 추가 데이터로 다음 페이지 여부를 판단)
		boolean hasNext = false;
		if (contents.size() > pageable.getPageSize()) {
			contents.remove(pageable.getPageSize());
			hasNext = true; // 1개 더 가져와졌다는 건 다음 페이지가 있음을 의미
		}
		
		return new SliceImpl<>(contents, pageable, hasNext);
	}
	
	// =======================================================
	// 동적 쿼리를 위한 BooleanExpression 메서드 모음
	// =======================================================
	
	// Null일 경우 where 조건절에서 빠지게 된다. SQL WHERE 절의 조건식 조각(블록)을 의미하는 전용 객체
	private BooleanExpression concertNameContains(String concertName) {
		return StringUtils.hasText(concertName) ? concert.concertName.contains(concertName) : null;
	}
	
	private BooleanExpression categoryIdsIn(List<Long> categoryIds) {
		return (categoryIds != null && !categoryIds.isEmpty()) ?
					concertCategory.category.categoryId.in(categoryIds) : null;
	}
	
	private BooleanExpression regionsIn(List<String> regions) {
		// Address 객체 안의 city 필드로 검색
		return (regions != null && !regions.isEmpty()) ?
					concert.address.city.in(regions) : null;
	}
	
	private BooleanExpression daysLeftLoe(Integer dayLeft) {
		if (dayLeft == null) return null;
		// 현재 시간 기준으로 daysLeft 일 후의 계산
		LocalDateTime targetDate =  LocalDateTime.now().plusDays(dayLeft);
		// 공연 종료일이  targetDate보다 작거나 같은(loe) 것만 조회
		return concert.endedAt.loe(targetDate);
	}
	
	private BooleanExpression ratingEq(ConcertRating rating) {
		return rating != null ? concert.rating.eq(rating) : null;
	}
}
