package com.jeonjueats.entity;

/**
 * 주문 상태 Enum
 * PENDING: 대기 중 (사장님 확인 대기)
 * ACCEPTED: 수락됨 (사장님이 주문 수락)
 * REJECTED: 거절됨 (사장님이 주문 거절)
 * DELIVERING: 배달 중 (음식 준비 완료, 배달 시작)
 * COMPLETED: 배달 완료
 */
public enum OrderStatus {
    PENDING("대기 중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
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