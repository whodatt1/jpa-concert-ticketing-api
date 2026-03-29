package com.tiketing.api.global.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass // 엔티티가 아니며 자식에게 컬럼을 물려주는 부모 역할
@EntityListeners(AuditingEntityListener.class) // 스프링 데이터 JPA가 시간에 맞춰서 자동으로 값을 넣어주도록 감시(Listen)함
public abstract class BaseEntity {
	
	@CreatedDate
	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt; // 생성 일자
	
	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	
}
