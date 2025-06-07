package com.jeonjueats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 카테고리 엔티티 (MVP 버전)
 * MVP 범위: 기본 카테고리 분류 (치킨, 피자, 중식, 한식 등)
 * 제외 기능: 계층 구조, 카테고리별 이벤트, 커스텀 아이콘
 */
@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // 카테고리명 (10종 사전 정의)

    @Column(name = "icon_image_url", length = 255)
    private String iconImageUrl; // 카테고리 아이콘 이미지 URL

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0; // 표시 순서

    // JPA Auditing을 통한 자동 시간 관리
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정 일시

    // 생성자
    public Category(String name, Integer sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public Category(String name, String iconImageUrl, Integer sortOrder) {
        this.name = name;
        this.iconImageUrl = iconImageUrl;
        this.sortOrder = sortOrder;
    }

    /**
     * 카테고리 아이콘 이미지 업데이트
     */
    public void updateIconImage(String iconImageUrl) {
        this.iconImageUrl = iconImageUrl;
    }

    /**
     * 표시 순서 변경
     */
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 카테고리명 변경
     */
    public void updateName(String name) {
        this.name = name;
    }
} 