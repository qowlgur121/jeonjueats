package com.jeonjueats.dto;

import com.jeonjueats.entity.MenuStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 메뉴 조회 응답 DTO
 * 일반 사용자와 사장님 모두 사용 가능한 공통 응답 형식
 */
@Getter
@Builder
public class MenuResponseDto {
    
    private Long menuId;
    private Long storeId;
    private String name;
    private String description;
    private BigDecimal price;
    private MenuStatus status;
    private String menuImageUrl;
    private String menuCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 