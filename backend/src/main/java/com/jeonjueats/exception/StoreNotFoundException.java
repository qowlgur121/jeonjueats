package com.jeonjueats.exception;

/**
 * 가게를 찾을 수 없을 때 발생하는 예외
 */
public class StoreNotFoundException extends RuntimeException {
    
    public StoreNotFoundException(String message) {
        super(message);
    }
    
    public StoreNotFoundException(Long storeId) {
        super("가게 ID " + storeId + "를 찾을 수 없습니다.");
    }
    
    public StoreNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 