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
 * 주문 엔티티 (MVP 버전)
 * MVP 범위: 기본 주문 처리, 가상 결제, 주문 상태 관리, 배달 주소
 * 제외 기능: 실제 결제 연동, 할인/포인트 시스템, 실시간 추적
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user", columnList = "user_id"),
    @Index(name = "idx_orders_store", columnList = "store_id"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * MVP 핵심 필드들 - 주문 기본 정보
     */
    @Column(name = "user_id", nullable = false)
    private Long userId; // 주문 사용자 ID

    @Column(name = "store_id", nullable = false)
    private Long storeId; // 주문된 가게 ID

    @Column(name = "total_price", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalPrice; // 총 주문 금액 (상품가+배달팁)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status; // 주문 상태 (PENDING, ACCEPTED 등)

    /**
     * 배달 주소 정보
     */
    @Column(name = "delivery_zipcode", nullable = false, length = 10)
    private String deliveryZipcode; // 배달 주소 - 우편번호

    @Column(name = "delivery_address1", nullable = false, length = 255)
    private String deliveryAddress1; // 배달 주소 - 기본 주소

    @Column(name = "delivery_address2", length = 255)
    private String deliveryAddress2; // 배달 주소 - 상세 주소

    @Column(columnDefinition = "TEXT")
    private String requests; // 고객 요청 사항

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber; // 전화번호

    /**
     * 결제 관련 정보 (MVP는 가상 결제)
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "VIRTUAL_PAYMENT"; // 결제 수단 (MVP 'VIRTUAL_PAYMENT' 고정)

    @Column(name = "payment_transaction_id", length = 255)
    private String paymentTransactionId; // 결제 거래 ID (MVP NULL)

    /**
     * 금액 상세 정보
     */
    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 0)
    private BigDecimal subtotalAmount; // 상품 총액 (배달팁 제외)

    @Column(name = "delivery_fee_at_order", nullable = false, precision = 10, scale = 0)
    private BigDecimal deliveryFeeAtOrder; // 주문 당시 배달팁

    @Column(name = "discount_amount", precision = 10, scale = 0)
    private BigDecimal discountAmount = BigDecimal.ZERO; // 할인 금액 (MVP 이후)

    @Column(name = "points_used", precision = 10, scale = 0)
    private BigDecimal pointsUsed = BigDecimal.ZERO; // 사용 포인트 (MVP 이후)

    /**
     * JPA Auditing을 통한 자동 시간 관리
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 주문 생성 일시

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 정보 수정 일시

    /**
     * 생성자
     */
    public Orders(Long userId, Long storeId, BigDecimal subtotalAmount, BigDecimal deliveryFeeAtOrder,
                  String deliveryZipcode, String deliveryAddress1, String deliveryAddress2, 
                  String phoneNumber, String paymentMethod) {
        this.userId = userId;
        this.storeId = storeId;
        this.subtotalAmount = subtotalAmount;
        this.deliveryFeeAtOrder = deliveryFeeAtOrder;
        this.totalPrice = subtotalAmount.add(deliveryFeeAtOrder);
        this.deliveryZipcode = deliveryZipcode;
        this.deliveryAddress1 = deliveryAddress1;
        this.deliveryAddress2 = deliveryAddress2;
        this.phoneNumber = phoneNumber;
        this.status = OrderStatus.PENDING;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "VIRTUAL_PAYMENT";
        this.discountAmount = BigDecimal.ZERO;
        this.pointsUsed = BigDecimal.ZERO;
    }

    /**
     * 비즈니스 메서드
     */
    
    /**
     * 주문 상태 변경
     */
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * 주문 수락
     */
    public void accept() {
        this.status = OrderStatus.ACCEPTED;
    }

    /**
     * 주문 거절
     */
    public void reject() {
        this.status = OrderStatus.REJECTED;
    }

    /**
     * 배달 시작
     */
    public void startDelivery() {
        this.status = OrderStatus.DELIVERING;
    }

    /**
     * 배달 완료
     */
    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    /**
     * 주문 상태 확인 메서드들
     */
    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }

    public boolean isAccepted() {
        return this.status == OrderStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return this.status == OrderStatus.REJECTED;
    }

    public boolean isDelivering() {
        return this.status == OrderStatus.DELIVERING;
    }

    public boolean isCompleted() {
        return this.status == OrderStatus.COMPLETED;
    }

    /**
     * 요청사항 업데이트
     */
    public void updateRequests(String requests) {
        this.requests = requests;
    }

    /**
     * 배달 주소 업데이트
     */
    public void updateDeliveryAddress(String zipcode, String address1, String address2) {
        this.deliveryZipcode = zipcode;
        this.deliveryAddress1 = address1;
        this.deliveryAddress2 = address2;
    }

    /**
     * MVP 이후: 할인 적용
     */
    public void applyDiscount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        this.totalPrice = this.subtotalAmount.add(this.deliveryFeeAtOrder).subtract(discountAmount).subtract(this.pointsUsed);
    }

    /**
     * MVP 이후: 포인트 사용
     */
    public void usePoints(BigDecimal pointsUsed) {
        this.pointsUsed = pointsUsed;
        this.totalPrice = this.subtotalAmount.add(this.deliveryFeeAtOrder).subtract(this.discountAmount).subtract(pointsUsed);
    }

    /**
     * 총 금액 재계산
     */
    public void recalculateTotalPrice() {
        this.totalPrice = this.subtotalAmount.add(this.deliveryFeeAtOrder).subtract(this.discountAmount).subtract(this.pointsUsed);
    }
} 