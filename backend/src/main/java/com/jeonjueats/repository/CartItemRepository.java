package com.jeonjueats.repository;

import com.jeonjueats.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
     * 장바구니 페이지에서 담긴 메뉴들 보여주기용
     */
    List<CartItem> findByCartIdOrderByCreatedAtDesc(Long cartId);

    /**
     * 장바구니에서 특정 메뉴 아이템 찾기
     * 이미 담긴 메뉴인지 확인용 (수량 증가 vs 새로 추가)
     */
    Optional<CartItem> findByCartIdAndMenuId(Long cartId, Long menuId);

    /**
     * 장바구니의 총 아이템 개수 조회
     * 장바구니 아이콘에 숫자 표시용
     */
    long countByCartId(Long cartId);

    /**
     * 특정 장바구니의 모든 아이템 삭제
     * 장바구니 전체 비우기 또는 다른 매장 메뉴 담을 때 사용
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cartId = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);

    /**
     * 특정 메뉴가 장바구니에 담겨있는지 확인
     */
    boolean existsByCartIdAndMenuId(Long cartId, Long menuId);

    /**
     * 특정 메뉴를 담고 있는 모든 장바구니 아이템 조회
     * 메뉴 삭제 시 장바구니에서도 제거하기 위함
     */
    List<CartItem> findByMenuId(Long menuId);
} 