package com.tiketing.api.global.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {
	private String city; // 서울, 인천, 경기
	private String streetAddress; // 전체 도로명 주소
	private String zipcode;
}
