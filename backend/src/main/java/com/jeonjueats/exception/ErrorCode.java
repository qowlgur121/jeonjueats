package com.jeonjueats.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션에서 사용하는 에러 코드 정의
 * 
 * 사용 예시:
 * - throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // === 공통 에러 ===
    INVALID_INPUT_VALUE("COMMON_001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED("COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR("COMMON_003", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE("COMMON_004", "잘못된 타입의 값입니다."),
    ACCESS_DENIED("COMMON_005", "접근이 거부되었습니다."),
    
    // === 인증/인가 에러 ===
    UNAUTHORIZED("AUTH_001", "인증이 필요합니다."),
    INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("AUTH_003", "만료된 토큰입니다."),
    INVALID_CREDENTIALS("AUTH_004", "이메일 또는 비밀번호가 올바르지 않습니다."),
    
    // === 사용자 관련 에러 ===
    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS("USER_002", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS("USER_003", "이미 사용 중인 닉네임입니다."),
    
    // === 가게 관련 에러 ===
    STORE_NOT_FOUND("STORE_001", "가게를 찾을 수 없습니다."),
    STORE_NOT_OPEN("STORE_002", "현재 영업하지 않는 가게입니다."),
    
    // === 메뉴 관련 에러 ===
    MENU_NOT_FOUND("MENU_001", "메뉴를 찾을 수 없습니다."),
    MENU_SOLD_OUT("MENU_002", "품절된 메뉴입니다."),
    
    // === 주문 관련 에러 ===
    ORDER_NOT_FOUND("ORDER_001", "주문을 찾을 수 없습니다."),
    MINIMUM_ORDER_NOT_MET("ORDER_002", "최소 주문 금액을 충족하지 않습니다."),
    CART_DIFFERENT_STORE("ORDER_003", "장바구니에는 같은 가게의 메뉴만 담을 수 있습니다."),
    
    // === 파일 관련 에러 ===
    FILE_UPLOAD_FAILED("FILE_001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE("FILE_002", "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED("FILE_003", "파일 크기가 제한을 초과했습니다.");
    
    private final String code;
    private final String message;
} 