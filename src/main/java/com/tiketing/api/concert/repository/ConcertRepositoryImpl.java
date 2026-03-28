package com.tiketing.api.concert.repository;

import static com.tiketing.api.concert.entity.QConcert.concert;
import static com.tiketing.api.concert.entity.QConcertCategory.concertCategory;
import static com.tiketing.api.concert.entity.QConcertSchedule.concertSchedule;
import static com.tiketing.api.concert.entity.QVenue.venue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tiketing.api.concert.dto.ConcertRequest.SearchCondition;
import com.tiketing.api.concert.entity.Concert;
import com.tiketing.api.concert.enums.ConcertRating;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepositoryCustom {
	
	private final JPAQueryFactory queryFactory;
	
	@Override
	public boolean existsOverlappingConcert(Long venueId, LocalDateTime startedAt, LocalDateTime endedAt) {
		
		Integer fetchOne = queryFactory
				.selectOne()
				.from(concert)
				.where(
						concert.venue.venueId.eq(venueId),
						concert.delYn.eq("N"),
						
						concert.startedAt.loe(endedAt),
						concert.endedAt.goe(startedAt)
				)
				.fetchFirst();
		
		return fetchOne != null;
	}

	@Override
	public Slice<Concert> searchConcerts(SearchCondition condition, Pageable pageable) {
		
		List<Concert> contents = queryFactory
				.selectFrom(concert)
				// N:1 관계이므로 데이터 뻥튀기가 발생하지 않는 안전한 JOIN
				.join(concert.venue, venue)
				.where(
						concert.delYn.eq("N"),
						ratingEq(condition.rating()),
						categoryIdsIn(condition.categoryIds()),
						concertNameContains(condition.concertName()),
						regionsIn(condition.regions()),
						daysLeftLoe(condition.daysLeft())
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
	
	@Override
	public Concert getConcert(Long concertId) {
		
		Concert content = queryFactory
				.selectFrom(concert)
				.leftJoin(concert.schedules, concertSchedule).fetchJoin()
				// 가격의 경우 batch size에게 맡기기
				.where(
						concertIdEq(concertId)
				)
				.fetchOne();
		
		return content;
	}
	
	// =======================================================
	// 동적 쿼리를 위한 BooleanExpression 메서드 모음
	// ======================================================

	private BooleanExpression concertIdEq(Long concertId) {
		if (concertId == null || concertId <= 0) {
			return null;
		}
		
		return concert.concertId.eq(concertId);
	}
	
	// Null일 경우 where 조건절에서 빠지게 된다. SQL WHERE 절의 조건식 조각(블록)을 의미하는 전용 객체
	private BooleanExpression concertNameContains(String concertName) {
		return StringUtils.hasText(concertName) ? concert.concertName.contains(concertName) : null;
	}

	// JOIN 대신 EXISTS 서브쿼리 도입!
	private BooleanExpression categoryIdsIn(Set<Long> categoryIds) {
		if (categoryIds == null || categoryIds.isEmpty()) {
			return null;
		}
		
		// "현재 메인 쿼리에서 검사 중인 콘서트와 매핑되어 있고, 요청받은 카테고리 ID를 가진 ConcertCategory가 1개라도 존재하는가?"
		return JPAExpressions
						.selectOne()
						.from(concertCategory)
						.where(
								concertCategory.concert.eq(concert), // 메인 쿼리의 concert와 연결
								concertCategory.category.categoryId.in(categoryIds)
						)
						.exists();
	}
	
	private BooleanExpression regionsIn(List<String> regions) {
		// Address 객체 안의 city 필드로 검색
		return (regions != null && !regions.isEmpty()) ?
					venue.address.city.in(regions) : null;
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
