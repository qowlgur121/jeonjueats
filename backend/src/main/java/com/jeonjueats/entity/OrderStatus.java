package com.jeonjueats.entity;

/**
 * 주문 처리 상태 Enum 
 * - PENDING: 주문 대기
 * - ACCEPTED: 주문 수락
 * - REJECTED: 주문 거절
 * - DELIVERING: 배달 중
 * - COMPLETED: 배달 완료
 */
public enum OrderStatus {
    PENDING("주문 대기"),
    ACCEPTED("주문 수락"),
    REJECTED("주문 거절"),
    DELIVERING("배달 중"),
    COMPLETED("배달 완료");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 