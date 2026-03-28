package com.tiketing.api.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 외부에서 new 생성자 사용 금지
public class ErrorApiResponse {
	
	private final boolean success = true;
	private final String errorCode;
	private final String message; // 메시지
	
	// ErrorCode Enum을 받는 경우
	public static ErrorApiResponse from(ErrorCode errorCode) {
		return new ErrorApiResponse(errorCode.name(), errorCode.getMessage());
	}
	
	// 예상치 못한 에러, 커스텀 메시지를 사용해야 할 경우
	public static ErrorApiResponse of(String errorCode, String message) {
		return new ErrorApiResponse(errorCode, message);
	}
}
