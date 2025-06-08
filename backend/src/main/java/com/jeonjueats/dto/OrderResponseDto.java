package com.jeonjueats.dto;

import com.jeonjueats.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 응답 DTO
 */
@Getter
@Builder
public class OrderResponseDto {

    private Long orderId;
    private Long userId;
    
    // 가게 정보
    private Long storeId;
    private String storeName;
    private String storeImageUrl;
    
    // 주문 기본 정보
    private OrderStatus status;
    private String statusDisplayName;
    
    // 금액 정보
    private BigDecimal subtotalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal totalPrice;
    
    // 배달 주소
    private String deliveryZipcode;
    private String deliveryAddress1;
    private String deliveryAddress2;
    private String fullDeliveryAddress;
    
    // 요청사항
    private String requests;
    
    // 결제 정보
    private String paymentMethod;
    private String paymentTransactionId;
    
    // 주문 아이템
    private List<OrderItemResponseDto> orderItems;
    private Integer totalItemCount;
    private Integer totalQuantity;
    
    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Getter
    @Builder
    public static class OrderItemResponseDto {
        private Long orderItemId;
        private Long menuId;
        private String menuName;
        private String menuDescription;
        private String menuImageUrl;
        private Integer quantity;
        private BigDecimal priceAtOrder;
        private BigDecimal itemTotalPrice;
    }
} 