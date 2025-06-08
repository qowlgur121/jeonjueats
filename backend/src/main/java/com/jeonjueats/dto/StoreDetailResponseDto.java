package com.jeonjueats.dto;

import com.jeonjueats.entity.StoreStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 가게 상세 정보 + 메뉴 목록 복합 응답 DTO
 * 일반 사용자가 가게 상세 페이지를 조회할 때 사용
 */
@Getter
@Builder
public class StoreDetailResponseDto {
    
    // 가게 기본 정보
    private Long storeId;
    private String name;
    private String description;
    private String zipcode;
    private String address1;
    private String address2;
    private String phoneNumber;
    private String storeImageUrl;
    private Long categoryId;
    private BigDecimal minOrderAmount;
    private BigDecimal deliveryFee;
    private StoreStatus status;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String operatingHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 메뉴 목록 (품절 상태 포함)
    private List<MenuResponseDto> menus;
} 