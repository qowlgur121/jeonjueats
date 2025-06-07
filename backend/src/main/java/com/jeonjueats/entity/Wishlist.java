package com.jeonjueats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 찜 목록 엔티티 (MVP 버전)
 * MVP 범위: 사용자가 좋아하는 가게 찜하기/취소 기능
 * 제외 기능: 찜 목록 공유, 찜 알림, 찜 카테고리 분류
 */
@Entity
@Table(name = "wishlist", 
    indexes = {
        @Index(name = "idx_wishlist_user", columnList = "user_id"),
        @Index(name = "idx_wishlist_store", columnList = "store_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_wishlist_user_store", columnNames = {"user_id", "store_id"})
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long id;

    /**
     * MVP 핵심 필드들
     */
    @Column(name = "user_id", nullable = false)
    private Long userId; // 찜한 사용자 ID

    @Column(name = "store_id", nullable = false)
    private Long storeId; // 찜한 가게 ID

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 찜한 일시

    /**
     * 생성자 (MVP 필수 필드)
     */
    public Wishlist(Long userId, Long storeId) {
        this.userId = userId;
        this.storeId = storeId;
    }

    /**
     * 비즈니스 메서드들
     */
    
    /**
     * 특정 사용자의 찜인지 확인
     */
    public boolean isWishlistByUser(Long userId) {
        return this.userId.equals(userId);
    }

    /**
     * 특정 가게의 찜인지 확인
     */
    public boolean isWishlistForStore(Long storeId) {
        return this.storeId.equals(storeId);
    }

    /**
     * 사용자와 가게가 모두 일치하는지 확인
     */
    public boolean matches(Long userId, Long storeId) {
        return this.userId.equals(userId) && this.storeId.equals(storeId);
    }

    /**
     * 찜 생성 시간을 기준으로 최근 찜인지 확인 (특정 일수 이내)
     */
    public boolean isRecentWishlist(int days) {
        if (days <= 0) {
            return false;
        }
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return this.createdAt.isAfter(cutoffDate);
    }
} 