package com.jeonjueats.repository;

import com.jeonjueats.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 장바구니 아이템 Repository 인터페이스
 * Spring Data JPA를 통해 CartItem 엔티티에 대한 기본 CRUD 및 커스텀 조회 메서드 제공
 * MVP: 장바구니에 담긴 개별 메뉴들 관리
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 특정 장바구니의 모든 아이템 조회
     * 장바구니 내용 조회 시 사용
     */
    List<CartItem> findByCartIdOrderByCreatedAtDesc(Long cartId);

    /**
     * 특정 장바구니의 특정 메뉴 아이템 조회
     * 같은 메뉴 추가 시 수량 증가용
     */
    Optional<CartItem> findByCartIdAndMenuId(Long cartId, Long menuId);

    /**
     * 특정 장바구니의 아이템 개수 조회
     * 장바구니 요약 정보용
     */
    long countByCartId(Long cartId);

    /**
     * 특정 장바구니의 총 수량 조회
     * 장바구니 요약 정보용
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cartId = :cartId")
    Integer getTotalQuantityByCartId(@Param("cartId") Long cartId);

    /**
     * 특정 장바구니의 모든 아이템 삭제
     * 주문 완료 후 장바구니 비우기 시 사용
     */
    void deleteByCartId(Long cartId);

    /**
     * 특정 메뉴의 모든 장바구니 아이템 삭제
     * 메뉴 삭제 시 연관된 장바구니 아이템 정리용
     */
    void deleteByMenuId(Long menuId);

    /**
     * 특정 장바구니의 특정 메뉴 아이템 존재 확인
     * 중복 체크용
     */
    boolean existsByCartIdAndMenuId(Long cartId, Long menuId);
} 