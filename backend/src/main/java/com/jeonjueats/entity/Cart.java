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
 * 장바구니 엔티티 (MVP 버전)
 * MVP 범위: 사용자별 단일 장바구니, 한 가게 메뉴만 담기 가능
 * 제외 기능: 다중 장바구니, 장바구니 공유, 임시 저장
 */
@Entity
@Table(name = "cart", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id"),
    @Index(name = "idx_cart_store", columnList = "store_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    /**
     * MVP 핵심 필드들
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId; // 사용자 ID (1:1 관계를 위한 UNIQUE 제약)

    @Column(name = "store_id")
    private Long storeId; // 현재 담긴 가게 ID (비어있으면 NULL)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 장바구니 생성 일시

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 장바구니 수정 일시

    /**
     * 생성자 (MVP 필수 필드)
     */
    public Cart(Long userId) {
        this.userId = userId;
        this.storeId = null; // 초기에는 빈 장바구니
    }

    /**
     * 비즈니스 메서드들
     */
    
    /**
     * 장바구니에 특정 가게의 메뉴를 담을 수 있는지 확인
     * MVP: 한 번에 한 가게 메뉴만 담기 가능
     */
    public boolean canAddMenuFromStore(Long storeId) {
        return this.storeId == null || this.storeId.equals(storeId);
    }

    /**
     * 장바구니를 특정 가게로 설정
     */
    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    /**
     * 장바구니 비우기 (다른 가게 메뉴 담을 때 사용)
     */
    public void clear() {
        this.storeId = null;
    }

    /**
     * 장바구니가 비어있는지 확인
     */
    public boolean isEmpty() {
        return this.storeId == null;
    }

    /**
     * 특정 가게의 장바구니인지 확인
     */
    public boolean isFromStore(Long storeId) {
        return this.storeId != null && this.storeId.equals(storeId);
    }
} 