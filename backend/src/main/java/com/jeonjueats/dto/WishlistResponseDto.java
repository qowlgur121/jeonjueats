package com.jeonjueats.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 찜 목록 조회 API 응답 DTO
 * GET /api/wishes/stores 응답에 사용
 */
@Getter
@Builder
public class WishlistResponseDto {
    
    /**
     * 찜 ID
     */
    private Long wishlistId;
    
    /**
     * 찜한 일시
     */
    private LocalDateTime wishedAt;
    
    /**
     * 가게 정보
     */
    private StoreInfo store;
    
    /**
     * 중첩 DTO: 가게 간략 정보
     */
    @Getter
    @Builder
    public static class StoreInfo {
        
        /**
         * 가게 ID
         */
        private Long storeId;
        
        /**
         * 가게 이름
         */
        private String storeName;
        
        /**
         * 가게 이미지 URL
         */
        private String storeImageUrl;
        
        /**
         * 최소 주문 금액
         */
        private int minOrderAmount;
        
        /**
         * 배달 팁
         */
        private int deliveryTip;
        
        /**
         * 가게 상태 (OPEN, CLOSED, TEMPORARILY_CLOSED)
         */
        private String storeStatus;
        
        /**
         * 가게 설명
         */
        private String description;
    }
} 