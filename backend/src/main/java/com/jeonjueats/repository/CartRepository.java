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
     * 사용자 ID로 장바구니 조회
     * 가장 중요한 메서드: 로그인한 사용자의 장바구니 찾기
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * 사용자의 장바구니 존재 여부 확인
     * 회원가입 시 장바구니 생성 체크용
     */
    boolean existsByUserId(Long userId);

    /**
     * 특정 매장의 장바구니만 조회
     * MVP: 한 번에 한 매장 메뉴만 담기 가능하므로 사용
     */
    Optional<Cart> findByUserIdAndStoreId(Long userId, Long storeId);

    /**
     * 사용자의 장바구니 삭제 (사용자 탈퇴 시)
     */
    void deleteByUserId(Long userId);
} 