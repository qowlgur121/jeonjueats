package com.jeonjueats.exception;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 * HTTP 상태 코드 404 Not Found 에 해당
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 