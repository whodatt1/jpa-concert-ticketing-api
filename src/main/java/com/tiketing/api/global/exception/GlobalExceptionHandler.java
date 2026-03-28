package com.tiketing.api.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	// 커스텀 객체 BusinessException이 터질 경우 잡음
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ErrorApiResponse> handleBusinessException(BusinessException e) {
		log.warn("BusinessException : {}", e.getMessage());
		
		ErrorCode errorCode = e.getErrorCode();
		
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ErrorApiResponse.from(errorCode));
	}
	
	// 입력값 검증 예외
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.warn("MethodArgumentNotValidException : {}", e.getMessage());
		
		// 첫번째 에러 메시지만 전달
		String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorApiResponse.of("VALID_INPUT_VALUE", errorMessage));
	}
	
	// 기타 서버 내부 에러
	@ExceptionHandler
	protected ResponseEntity<ErrorApiResponse> handleException(Exception e) {
		// 이 에러는 심각하므로 warn이 아닌 error 로그
        log.error("Internal Server Error : ", e);
        
        return ResponseEntity
        		.status(HttpStatus.INTERNAL_SERVER_ERROR)
        		.body(ErrorApiResponse.of("INTERNAL SERVER ERROR", "서버 내부에서 에러가 발생했습니다. 관리자에게 문의해주세요."));
	}
}
