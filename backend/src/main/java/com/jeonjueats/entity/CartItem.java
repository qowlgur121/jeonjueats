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
 * 장바구니 아이템 엔티티 (MVP 버전)
 * MVP 범위: 장바구니에 담긴 개별 메뉴와 수량 관리
 * 제외 기능: 메뉴 옵션 선택, 임시 저장, 만료 시간
 */
@Entity
@Table(name = "cart_item", 
    indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_item_menu", columnList = "menu_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cart_item_cart_menu", columnNames = {"cart_id", "menu_id"})
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    /**
     * MVP 핵심 필드들
     */
    @Column(name = "cart_id", nullable = false)
    private Long cartId; // 소속 장바구니 ID

    @Column(name = "menu_id", nullable = false)
    private Long menuId; // 메뉴 ID

    @Column(nullable = false)
    private Integer quantity; // 수량 (1 이상)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 장바구니 담은 일시

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수량 변경 일시

    /**
     * 생성자 (MVP 필수 필드)
     */
    public CartItem(Long cartId, Long menuId, Integer quantity) {
        this.cartId = cartId;
        this.menuId = menuId;
        this.quantity = quantity;
    }

    /**
     * 비즈니스 메서드들
     */
    
    /**
     * 수량 증가
     */
    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가할 수량은 1 이상이어야 합니다.");
        }
        this.quantity += amount;
    }

    /**
     * 수량 감소
     */
    public void decreaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("감소할 수량은 1 이상이어야 합니다.");
        }
        if (this.quantity - amount < 1) {
            throw new IllegalArgumentException("수량은 최소 1개 이상이어야 합니다.");
        }
        this.quantity -= amount;
    }

    /**
     * 수량 직접 설정
     */
    public void updateQuantity(Integer quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 최소 1개 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    /**
     * 수량 유효성 검사
     */
    public boolean isValidQuantity() {
        return this.quantity != null && this.quantity >= 1;
    }

    /**
     * 같은 메뉴인지 확인
     */
    public boolean isMenuId(Long menuId) {
        return this.menuId.equals(menuId);
    }
} 