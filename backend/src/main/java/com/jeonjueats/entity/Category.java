package com.jeonjueats.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 음식 카테고리 엔티티 (PRD 4.2.2 category 테이블 명세)
 * 
 * 예시: 한식, 중식, 치킨, 피자, 카페/디저트 등
 * MVP에서는 10개의 고정된 카테고리 사용
 */
@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Category extends BaseTimeEntity {
    
    /**
     * 카테고리 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 카테고리명 (예: "한식", "치킨") - UNIQUE 제약조건
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    /**
     * 카테고리 아이콘 이미지 URL
     */
    @Column(length = 255)
    private String iconImageUrl;
    
    /**
     * 화면 표시 순서 (작은 숫자가 앞에 표시)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
} 