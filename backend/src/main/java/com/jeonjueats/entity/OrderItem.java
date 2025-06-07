package com.jeonjueats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 아이템 엔티티 (MVP 버전)
 * MVP 범위: 주문별 메뉴 정보, 수량, 주문 당시 가격 저장
 * 제외 기능: 메뉴 옵션 선택, 개별 할인 적용
 */
@Entity
@Table(name = "order_item", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_menu", columnList = "menu_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MVP 핵심 필드들
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId; // 주문 ID (외래키)

    @Column(name = "menu_id", nullable = false)
    private Long menuId; // 주문된 메뉴 ID (외래키)

    @Column(nullable = false)
    private Integer quantity; // 주문 수량 (1 이상)

    @Column(name = "price_at_order", nullable = false, precision = 10, scale = 0)
    private BigDecimal priceAtOrder; // 주문 당시 메뉴 개당 가격

    /**
     * JPA Auditing을 통한 자동 시간 관리
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정 일시

    /**
     * 생성자
     */
    public OrderItem(Long orderId, Long menuId, Integer quantity, BigDecimal priceAtOrder) {
        this.orderId = orderId;
        this.menuId = menuId;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    /**
     * 비즈니스 메서드
     */
    
    /**
     * 수량 변경
     */
    public void updateQuantity(Integer quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    /**
     * 해당 주문 아이템의 총 금액 계산 (개당 가격 × 수량)
     */
    public BigDecimal getTotalPrice() {
        return this.priceAtOrder.multiply(BigDecimal.valueOf(this.quantity));
    }

    /**
     * 주문 당시 가격 업데이트 (주문 생성 시에만 사용)
     */
    public void updatePriceAtOrder(BigDecimal priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    /**
     * 수량 확인 메서드
     */
    public boolean isValidQuantity() {
        return this.quantity != null && this.quantity >= 1;
    }

    /**
     * 주문 아이템이 특정 메뉴인지 확인
     */
    public boolean isForMenu(Long menuId) {
        return this.menuId.equals(menuId);
    }

    /**
     * 주문 아이템이 특정 주문에 속하는지 확인
     */
    public boolean belongsToOrder(Long orderId) {
        return this.orderId.equals(orderId);
    }
} 