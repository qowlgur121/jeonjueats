package com.jeonjueats.repository;

import com.jeonjueats.entity.Orders;
import com.jeonjueats.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Orders 엔티티 Repository
 */
@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    /**
     * 사용자별 주문 목록 조회 (페이징)
     * 내 주문 목록 조회
     */
    Page<Orders> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자별 주문 목록 조회 (전체)
     */
    List<Orders> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 주문 소유권 확인 (사용자별 접근 권한 체크)
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * 매장 주문 소유권 확인 (사장님 권한 체크)
     */
    @Query("SELECT COUNT(o) > 0 FROM Orders o WHERE o.id = :orderId AND o.storeId = :storeId")
    boolean existsByOrderIdAndStoreId(@Param("orderId") Long orderId, @Param("storeId") Long storeId);

    /**
     * 매장별 주문 목록 조회 (페이징)
     * 주문 목록 조회 (전체 상태)
     */
    Page<Orders> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    /**
     * 매장별 + 상태별 주문 조회 (페이징)
     */
    Page<Orders> findByStoreIdAndStatusOrderByCreatedAtDesc(Long storeId, OrderStatus status, Pageable pageable);

    /**
     * 매장별 특정 주문 조회 (권한 체크 포함)
     * 주문 상세 조회
     */
    Optional<Orders> findByIdAndStoreId(Long orderId, Long storeId);

    /**
     * 사용자별 특정 주문 조회 (권한 체크 포함)
     * 내 주문 상세 조회
     */
    Optional<Orders> findByIdAndUserId(Long orderId, Long userId);

    /**
     * 사용자별 상태별 주문 조회
     */
    List<Orders> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

    /**
     * 매장별 상태별 주문 조회 (전체)
     */
    List<Orders> findByStoreIdAndStatusOrderByCreatedAtDesc(Long storeId, OrderStatus status);

    /**
     * 매장별 주문 개수 조회
     */
    long countByStoreId(Long storeId);

    /**
     * 매장별 + 상태별 주문 개수 조회
     */
    long countByStoreIdAndStatus(Long storeId, OrderStatus status);

    /**
     * 사용자별 주문 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 기간별 주문 조회 (매장별 매출 분석용)
     */
    @Query("SELECT o FROM Orders o WHERE o.storeId = :storeId AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Orders> findByStoreIdAndCreatedAtBetween(@Param("storeId") Long storeId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
} 