package com.jeonjueats.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 찜 상태 변경(토글) API 응답 DTO
 * POST /api/wishes/stores/{storeId}/toggle 응답에 사용
 */
@Getter
@Builder
public class WishlistToggleResponseDto {
    
    /**
     * 가게 ID
     */
    private Long storeId;
    
    /**
     * 변경된 찜 상태
     * true: 찜함, false: 찜 해제
     */
    private boolean isWished;
    
    /**
     * 응답 메시지
     */
    private String message;
} 