package com.jeonjueats.dto;

import com.jeonjueats.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 목록 조회 응답 DTO
 * 주문 목록에서 보여줄 간략한 정보만 포함
 */
@Getter
@Builder
public class OrderListResponseDto {

    private Long orderId;
    
    // 가게 정보
    private Long storeId;
    private String storeName;
    private String storeImageUrl;
    
    // 주문 기본 정보
    private OrderStatus status;
    private String statusDisplayName;
    
    // 금액 정보
    private BigDecimal totalPrice;
    
    // 대표 메뉴 정보 (예: "후라이드 치킨 외 2건")
    private String representativeMenuName;
    private int totalMenuCount;
    
    // 주문 시간
    private LocalDateTime orderedAt;
    
    // 주문 아이템 수량 (총 메뉴 개수)
    private int totalQuantity;
} 