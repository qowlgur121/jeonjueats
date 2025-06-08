package com.jeonjueats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 메뉴를 찾을 수 없을 때 발생하는 예외
 * HTTP 404 Not Found 상태 코드를 반환합니다.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MenuNotFoundException extends RuntimeException {
    
    public MenuNotFoundException(String message) {
        super(message);
    }
    
    public MenuNotFoundException(Long menuId) {
        super("메뉴 ID " + menuId + "를 찾을 수 없습니다.");
    }
    
    public MenuNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 