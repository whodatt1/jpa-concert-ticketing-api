package com.tiketing.api.concert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiketing.api.concert.entity.Concert;

public interface ConcertRepository extends JpaRepository<Concert, Long>, ConcertRepositoryCustom {
}
