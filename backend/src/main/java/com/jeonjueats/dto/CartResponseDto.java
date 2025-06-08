package com.jeonjueats.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 장바구니 전체 응답 DTO
 * 사용자의 장바구니 전체 내용과 주문 요약 정보를 전달
 */
@Getter
@Builder
public class CartResponseDto {

    private Long cartId;
    private Long storeId;
    private String storeName;
    private String storeImageUrl;
    private List<CartItemResponseDto> items;
    
    // 주문 요약 정보
    private Integer totalItemCount; // 총 아이템 종류 수
    private Integer totalQuantity; // 총 수량
    private BigDecimal totalPrice; // 총 주문 금액
    private BigDecimal deliveryFee; // 배달비 (MVP에서는 고정값)
    private BigDecimal finalPrice; // 최종 결제 금액 (총 주문 금액 + 배달비)
    
    private boolean isEmpty; // 장바구니가 비어있는지 여부
} 