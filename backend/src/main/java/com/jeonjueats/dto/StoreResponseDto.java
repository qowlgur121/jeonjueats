package com.jeonjueats.dto;

import com.jeonjueats.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 가게 정보 응답 DTO
 * 가게 등록, 조회, 수정 API에서 사용
 */
@Getter
@Builder
@AllArgsConstructor
public class StoreResponseDto {

    /**
     * 가게 ID
     */
    private final Long storeId;

    /**
     * 가게명
     */
    private final String name;

    /**
     * 가게 설명
     */
    private final String description;

    /**
     * 우편번호
     */
    private final String zipcode;

    /**
     * 기본 주소
     */
    private final String address1;

    /**
     * 상세 주소
     */
    private final String address2;

    /**
     * 전화번호
     */
    private final String phoneNumber;

    /**
     * 가게 이미지 URL
     */
    private final String storeImageUrl;

    /**
     * 카테고리 ID
     */
    private final Long categoryId;

    /**
     * 최소 주문 금액
     */
    private final BigDecimal minOrderAmount;

    /**
     * 배달비
     */
    private final BigDecimal deliveryFee;

    /**
     * 운영 상태 (OPEN, CLOSED)
     */
    private final StoreStatus status;

    /**
     * 평균 평점
     */
    private final BigDecimal averageRating;

    /**
     * 리뷰 개수
     */
    private final Integer reviewCount;

    /**
     * 영업 시간
     */
    private final String operatingHours;

    /**
     * 가게 등록일시
     */
    private final LocalDateTime createdAt;

    /**
     * 가게 정보 수정일시
     */
    private final LocalDateTime updatedAt;
} 