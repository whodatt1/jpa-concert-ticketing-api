package com.tiketing.api.concert.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tiketing.api.concert.dto.ConcertRequest;
import com.tiketing.api.concert.entity.Concert;

public interface ConcertRepositoryCustom {
	boolean existsOverlappingConcert(Long venueId, LocalDateTime startedAt, LocalDateTime endedAt);
	Slice<Concert> searchConcerts(ConcertRequest.SearchCondition condition, Pageable pageable);
	Optional<Concert> getConcert(Long concertId);
}
