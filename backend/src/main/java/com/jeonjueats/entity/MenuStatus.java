package com.jeonjueats.entity;

/**
 * 메뉴 판매 상태 Enum
 * AVAILABLE: 판매 가능
 * SOLD_OUT: 품절
 */
public enum MenuStatus {
    AVAILABLE("판매 가능"),
    SOLD_OUT("품절");

    private final String description;

    MenuStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 