package com.jeonjueats.repository;

import com.jeonjueats.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 아이템 Repository 인터페이스
 * Spring Data JPA를 통해 OrderItem 엔티티에 대한 기본 CRUD 및 커스텀 조회 메서드 제공
 * MVP: 주문 상세 내역 관리, 주문에 포함된 개별 메뉴들 관리
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 특정 주문의 모든 아이템 조회
     * 주문 상세 페이지에서 "어떤 메뉴들을 몇 개씩 주문했는지" 보여주기용
     */
    List<OrderItem> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * 특정 메뉴의 주문 내역 조회
     * 메뉴별 판매 통계 또는 인기 메뉴 분석용
     */
    List<OrderItem> findByMenuIdOrderByCreatedAtDesc(Long menuId);

    /**
     * 특정 주문의 총 아이템 개수 조회
     * 주문 요약에서 "총 3개 상품" 표시용
     */
    long countByOrderId(Long orderId);

    /**
     * 특정 주문의 총 금액 계산
     * 주문 상세에서 상품 총액 계산용 (price_at_order × quantity 합계)
     */
    @Query("SELECT SUM(oi.priceAtOrder * oi.quantity) FROM OrderItem oi WHERE oi.orderId = :orderId")
    BigDecimal calculateTotalAmountByOrderId(@Param("orderId") Long orderId);

    /**
     * 특정 메뉴의 총 판매 수량 조회
     * 메뉴별 판매량 통계용
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.menuId = :menuId")
    Long getTotalQuantityByMenuId(@Param("menuId") Long menuId);

    /**
     * 특정 메뉴의 총 판매 금액 조회
     * 메뉴별 매출 통계용
     */
    @Query("SELECT SUM(oi.priceAtOrder * oi.quantity) FROM OrderItem oi WHERE oi.menuId = :menuId")
    BigDecimal getTotalSalesByMenuId(@Param("menuId") Long menuId);
} 