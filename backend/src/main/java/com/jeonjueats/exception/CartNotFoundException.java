package com.jeonjueats.exception;

/**
 * 장바구니를 찾을 수 없을 때 발생하는 예외
 * HTTP 404 Not Found로 처리됨
 */
public class CartNotFoundException extends RuntimeException {
    
    public CartNotFoundException(String message) {
        super(message);
    }
    
    public CartNotFoundException(Long userId) {
        super("사용자 ID " + userId + "의 장바구니를 찾을 수 없습니다.");
    }
} 