package com.jeonjueats.repository;

import com.jeonjueats.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 찜 목록 Repository 인터페이스
 * Spring Data JPA를 통해 Wishlist 엔티티에 대한 기본 CRUD 및 커스텀 조회 메서드 제공
 * MVP: 사용자별 찜한 매장 관리, 찜 추가/해제
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * 사용자별 찜 목록 조회 (최신순)
     * 마이페이지의 "찜한 매장" 목록 보여주기용
     */
    Page<Wishlist> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자가 특정 매장을 찜했는지 확인
     * 매장 상세 페이지에서 하트 아이콘 색상 결정용
     */
    Optional<Wishlist> findByUserIdAndStoreId(Long userId, Long storeId);

    /**
     * 찜 여부 확인 (boolean 반환)
     * 더 간단한 찜 상태 체크용
     */
    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    /**
     * 특정 매장을 찜한 사용자들 조회
     * 매장 인기도 분석용
     */
    List<Wishlist> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    /**
     * 사용자의 총 찜 개수 조회
     * 마이페이지에서 "찜 3개" 이런 정보 표시용
     */
    long countByUserId(Long userId);

    /**
     * 특정 매장의 총 찜 개수 조회
     * 매장의 인기도 지표용
     */
    long countByStoreId(Long storeId);

    /**
     * 찜 해제 (사용자 + 매장 조합으로 삭제)
     * 하트 버튼 클릭 시 찜 해제용
     */
    void deleteByUserIdAndStoreId(Long userId, Long storeId);

    /**
     * 사용자 탈퇴 시 모든 찜 삭제
     */
    void deleteByUserId(Long userId);
} 