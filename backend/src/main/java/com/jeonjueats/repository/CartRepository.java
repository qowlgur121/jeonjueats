package com.jeonjueats.repository;

import com.jeonjueats.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 장바구니 Repository 인터페이스
 * Spring Data JPA를 통해 Cart 엔티티에 대한 기본 CRUD 및 커스텀 조회 메서드 제공
 * MVP: 한 사용자당 하나의 장바구니만 존재 (1:1 관계)
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 사용자별 장바구니 조회
     * 한 사용자당 하나의 장바구니만 존재 (unique 제약)
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * 사용자별 장바구니 존재 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 특정 가게의 장바구니 수 조회
     * 통계용으로 활용 가능
     */
    long countByStoreId(Long storeId);

    /**
     * 빈 장바구니 조회 (storeId가 null인 경우)
     * 정리 작업용
     */
    long countByStoreIdIsNull();

    /**
     * 사용자의 장바구니 삭제
     * 사용자 탈퇴 시 사용
     */
    void deleteByUserId(Long userId);
} 