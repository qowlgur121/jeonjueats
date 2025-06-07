package com.jeonjueats.repository;

import com.jeonjueats.entity.Menu;
import com.jeonjueats.entity.MenuStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 메뉴 Repository 인터페이스
 * Spring Data JPA를 통해 Menu 엔티티에 대한 기본 CRUD 및 커스텀 조회 메서드 제공
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * 매장별 메뉴 목록 조회 (논리적 삭제 제외)
     * 가게 상세 정보 및 메뉴 목록 조회
     */
    List<Menu> findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(Long storeId);

    /**
     * 매장별 + 상태별 메뉴 조회
     */
    Page<Menu> findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    /**
     * 메뉴 소유권 확인 (사장님 권한 체크용)
     * PRD 기반 - storeId와 ownerId 연결을 위해 서브쿼리 사용
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.id = :menuId AND m.storeId IN " +
           "(SELECT s.id FROM Store s WHERE s.ownerId = :ownerId) AND m.isDeleted = false")
    boolean existsByMenuIdAndOwnerIdAndIsDeletedFalse(@Param("menuId") Long menuId, @Param("ownerId") Long ownerId);

    /**
     * 매장별 + 판매상태별 메뉴 조회
     * 가게 상세에서 품절 상태 포함 메뉴 표시
     */
    List<Menu> findByStoreIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(Long storeId, MenuStatus status);

    /**
     * 매장별 판매 가능한 메뉴 조회 (주문 가능 메뉴)
     * 장바구니에 메뉴 추가 시 AVAILABLE 체크
     */
    List<Menu> findByStoreIdAndStatusAndIsDeletedFalse(Long storeId, MenuStatus status);

    /**
     * 메뉴명으로 검색 (통합 검색용)
     * 가게 또는 메뉴 검색
     */
    @Query("SELECT DISTINCT m.storeId FROM Menu m WHERE m.name LIKE %:keyword% AND m.isDeleted = false")
    List<Long> findStoreIdsByMenuNameContaining(@Param("keyword") String keyword);

    /**
     * 메뉴 ID와 매장 ID로 메뉴 조회 (권한 체크 포함)
     */
    Optional<Menu> findByIdAndStoreIdAndIsDeletedFalse(Long menuId, Long storeId);

    /**
     * 매장별 메뉴 개수 조회
     */
    long countByStoreIdAndIsDeletedFalse(Long storeId);

    /**
     * 매장별 판매 가능한 메뉴 개수 조회
     */
    long countByStoreIdAndStatusAndIsDeletedFalse(Long storeId, MenuStatus status);
} 