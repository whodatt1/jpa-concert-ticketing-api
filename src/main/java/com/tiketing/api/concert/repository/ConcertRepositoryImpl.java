package com.tiketing.api.concert.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tiketing.api.concert.dto.ConcertRequest.SearchCondition;
import com.tiketing.api.concert.entity.Concert;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepositoryCustom {
	
	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<Concert> searchConcerts(SearchCondition condition, Pageable pageable) {
		
		return null;
	}
	
}
