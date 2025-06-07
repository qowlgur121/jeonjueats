package com.jeonjueats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 매장 엔티티 (MVP 버전)
 * MVP 범위: 기본 매장 정보, 카테고리 분류, 영업 상태
 * 제외 기능: 위치 기반 서비스, 매장 평점/리뷰, 배달비 정책, 예약 시스템
 */
@Entity
@Table(name = "store", indexes = {
    @Index(name = "idx_store_owner", columnList = "owner_id"),
    @Index(name = "idx_store_category", columnList = "category_id"),
    @Index(name = "idx_store_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE store SET is_deleted = true WHERE store_id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    /**
     * MVP 핵심 필드들
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, length = 255)
    private String address1;

    @Column(length = 255)
    private String address2;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 1000)
    private String description;

    @Column(name = "store_image_url", length = 255)
    private String storeImageUrl;

    @Column(name = "min_order_amount", nullable = false, precision = 10, scale = 0)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 0)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoreStatus status = StoreStatus.OPEN;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "operating_hours", length = 255)
    private String operatingHours;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성자
    public Store(Long ownerId, Long categoryId, String name, String zipcode, String address1, String phoneNumber) {
        this.ownerId = ownerId;
        this.categoryId = categoryId;
        this.name = name;
        this.zipcode = zipcode;
        this.address1 = address1;
        this.phoneNumber = phoneNumber;
        this.status = StoreStatus.OPEN;
        this.isDeleted = false;
        this.minOrderAmount = BigDecimal.ZERO;
        this.deliveryFee = BigDecimal.ZERO;
    }

    // 비즈니스 메서드

    /**
     * 가게 기본 정보 업데이트
     */
    public void updateBasicInfo(String name, String description, String phoneNumber) {
        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
    }
    public void open() {
        this.status = StoreStatus.OPEN;
    }

    public void close() {
        this.status = StoreStatus.CLOSED;
    }

    public boolean isOpen() {
        return this.status == StoreStatus.OPEN && !this.isDeleted;
    }

    public boolean isClosed() {
        return this.status == StoreStatus.CLOSED;
    }

    public boolean isDeleted() {
        return this.isDeleted;
    }



    public void updateAddress(String zipcode, String address1, String address2) {
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
    }

    public void updateStoreImage(String storeImageUrl) {
        this.storeImageUrl = storeImageUrl;
    }

    public void updateOrderSettings(BigDecimal minOrderAmount, BigDecimal deliveryFee) {
        this.minOrderAmount = minOrderAmount;
        this.deliveryFee = deliveryFee;
    }

    public void updateStatus(StoreStatus status) {
        this.status = status;
    }

    /**
     * 논리적 삭제 (soft delete)
     */
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public void updateOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public void updateRating(BigDecimal averageRating, Integer reviewCount) {
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }
} 