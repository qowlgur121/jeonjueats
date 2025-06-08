package com.jeonjueats.exception;

/**
 * 장바구니 관련 비즈니스 규칙 위반 시 발생하는 예외
 * HTTP 400 Bad Request로 처리됨 (예: 다른 가게 메뉴 추가 시도)
 */
public class InvalidCartOperationException extends RuntimeException {
    
    public InvalidCartOperationException(String message) {
        super(message);
    }
} 