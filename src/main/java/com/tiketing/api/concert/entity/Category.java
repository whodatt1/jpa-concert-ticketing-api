package com.tiketing.api.concert.entity;

import java.util.ArrayList;
import java.util.List;

import com.tiketing.api.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {
	
	@Id
	@GeneratedValue
	@Column(name = "category_id")
	private Long categoryId;
	
	@Column(name = "category_name")
	private String categoryName;
	
	@OneToMany(mappedBy = "category")
	private List<ConcertCategory> concertCategories = new ArrayList<>();
	
	public void addConcertCategory(ConcertCategory concertCategory) {
		this.concertCategories.add(concertCategory);
		concertCategory.setCategory(this);
	}
	
	@Builder
	public Category(String categoryName) {
		this.categoryName = categoryName;
	}
}
