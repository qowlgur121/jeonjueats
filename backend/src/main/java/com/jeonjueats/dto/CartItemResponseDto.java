package com.jeonjueats.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 장바구니 아이템 응답 DTO
 * 장바구니 조회 시 각 아이템의 정보를 전달
 */
@Getter
@Builder
public class CartItemResponseDto {

    private Long cartItemId;
    private Long menuId;
    private String menuName;
    private String menuDescription;
    private BigDecimal menuPrice;
    private String menuImageUrl;
    private Integer quantity;
    private BigDecimal itemTotalPrice; // 메뉴 가격 × 수량
    private LocalDateTime addedAt; // 장바구니에 담은 시간
} 