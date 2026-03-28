package com.tiketing.api.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 외부에서 new 생성자 사용 금지
public class ApiResponse<T> {
	
	private final boolean success = true;
	private final String message; // 메시지
	private final T data; // 응답 객체
	
	// 데이터 & 커스텀 메시지
	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(message, data);
	}
	
	// 데이터
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("요청이 성공적으로 처리되었습니다.", data);
    }

    // 리턴할 데이터가 없을 때
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(message, null);
    }
}
