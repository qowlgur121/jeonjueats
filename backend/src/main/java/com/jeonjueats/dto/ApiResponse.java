package com.jeonjueats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 모든 API 응답을 일관된 형식으로 포장하는 공통 응답 클래스
 * 
 * 사용 예시:
 * - 성공: ApiResponse.success(data)
 * - 실패: ApiResponse.error(message)
 * 
 * @param <T> 응답 데이터의 타입
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 응답 성공 여부
     */
    private Boolean success;
    
    /**
     * 응답 메시지
     */
    private String message;
    
    /**
     * 실제 응답 데이터
     */
    private T data;
    
    /**
     * 에러 코드 (실패 시에만 사용)
     */
    private String errorCode;
    
    // === 성공 응답 생성 메서드들 ===
    
    /**
     * 데이터와 함께 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }
    
    /**
     * 메시지와 데이터와 함께 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 메시지만으로 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }
    
    // === 실패 응답 생성 메서드들 ===
    
    /**
     * 에러 메시지로 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * 에러 코드와 메시지로 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
} 