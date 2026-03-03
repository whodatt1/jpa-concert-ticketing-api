package com.tiketing.api.concert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiketing.api.concert.entity.ConcertSchedule;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, Long> {
}
