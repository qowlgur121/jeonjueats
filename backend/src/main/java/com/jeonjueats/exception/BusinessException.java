package com.jeonjueats.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 발생하는 커스텀 예외
 * 
 * 사용 예시:
 * - throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * - throw new BusinessException("사용자를 찾을 수 없습니다");
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    /**
     * 에러 코드와 메시지로 예외 생성
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 메시지만으로 예외 생성 (에러 코드는 기본값)
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    /**
     * ErrorCode Enum을 사용한 예외 생성 (나중에 ErrorCode 만들 예정)
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }
} 