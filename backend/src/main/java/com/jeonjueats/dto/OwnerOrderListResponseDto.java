package com.jeonjueats.dto;

import com.jeonjueats.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사장님용 주문 목록 조회 응답 DTO
 * 사장님이 가게에 들어온 주문 목록을 조회할 때 사용
 */
@Getter
@Builder
public class OwnerOrderListResponseDto {

    private Long orderId;
    
    // 주문자 정보
    private Long userId;
    private String userNickname;
    
    // 주문 기본 정보
    private OrderStatus status;
    private String statusDisplayName;
    
    // 금액 정보
    private BigDecimal totalPrice;
    private BigDecimal subtotalAmount;
    private BigDecimal deliveryFeeAtOrder;
    
    // 주문 내용 요약
    private String representativeMenuName;
    private int totalMenuCount;
    private int totalQuantity;
    
    // 배달 주소 정보
    private String deliveryAddress1;
    private String deliveryAddress2;
    
    // 주문 시간
    private LocalDateTime orderedAt;
    
    // 요청 사항 (있는 경우만)
    private String requests;
} 