package com.tiketing.api.concert.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tiketing.api.concert.dto.ConcertRequest;
import com.tiketing.api.concert.entity.Concert;

public interface ConcertRepositoryCustom {
	Slice<Concert> searchConcerts(ConcertRequest.SearchCondition condition, Pageable pageable);
}
