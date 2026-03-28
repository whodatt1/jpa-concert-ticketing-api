package com.tiketing.api.concert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiketing.api.concert.entity.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
