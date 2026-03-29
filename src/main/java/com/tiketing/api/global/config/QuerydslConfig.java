package com.tiketing.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Configuration
public class QuerydslConfig {
	
	// 스프링이 현재 트랜잭션에 맞는 EntityManager를 프록시형태로 주입
	@PersistenceContext 
	private EntityManager entityManager;
	
	// Querydsl의 심장 select, from, where 같은 자바 메서드를 체이닝해서 부르면 이 객체가 JPQL로 번역
	// 자바코드로 쿼리를 만들어 entityManager에게 전달
	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(entityManager);
	}
}
