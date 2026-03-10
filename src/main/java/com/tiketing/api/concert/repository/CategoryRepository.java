package com.tiketing.api.concert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tiketing.api.concert.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
