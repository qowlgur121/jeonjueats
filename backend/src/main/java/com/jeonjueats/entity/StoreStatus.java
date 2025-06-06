package com.jeonjueats.entity;

/**
 * 가게 운영 상태 Enum 
 * - OPEN: 영업 중
 * - CLOSED: 영업 종료
 */
public enum StoreStatus {
    OPEN("영업 중"),
    CLOSED("영업 종료");
    
    private final String description;
    
    StoreStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 