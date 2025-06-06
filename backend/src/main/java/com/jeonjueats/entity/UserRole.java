package com.jeonjueats.entity;

/**
 * 사용자 역할 Enum
 * - ROLE_USER: 일반 사용자 (음식 주문)
 * - ROLE_OWNER: 사장님 (가게 운영)
 */
public enum UserRole {
    ROLE_USER("일반 사용자"),
    ROLE_OWNER("사장님");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 