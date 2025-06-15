package com.jeonjueats.repository;

import com.jeonjueats.entity.Store;
import com.jeonjueats.entity.StoreStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 매장 Repository 인터페이스
 * Spring Data JPA를 통해 Store 엔티티에 대한 기본 CRUD 및 커스텀 조회 메서드 제공
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 사장님별 매장 목록 조회 (페이징)
     * 내 가게 목록 조회
     */
    Page<Store> findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    /**
     * 사장님별 매장 목록 조회 (전체)
     */
    List<Store> findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(Long ownerId);

    /**
     * 매장 소유권 확인 (사장님 권한 체크용)
     * 사장님용 가게 관리 API 권한 체크
     */
    boolean existsByIdAndOwnerIdAndIsDeletedFalse(Long storeId, Long ownerId);

    /**
     * 카테고리별 + 상태별 매장 조회 (페이징)
     * 가게 목록 조회 (카테고리 필터링)
     */
    Page<Store> findByCategoryIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(Long categoryId, StoreStatus status, Pageable pageable);

    /**
     * 상태별 매장 조회 (페이징) - 전체 카테고리
     * 가게 목록 조회 (전체 카테고리)
     */
    Page<Store> findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(StoreStatus status, Pageable pageable);

    /**
     * 매장명으로 검색 (페이징)
     * 가게 또는 메뉴 검색
     */
    Page<Store> findByNameContainingAndIsDeletedFalseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    /**
     * 통합 검색: 매장명 + 메뉴명 (페이징)
     * 가게 또는 메뉴 검색 - 매장명/메뉴명 통합
     */
    @Query("SELECT DISTINCT s FROM Store s WHERE s.isDeleted = false AND " +
           "(s.name LIKE %:keyword% OR s.id IN " +
           "(SELECT m.storeId FROM Menu m WHERE m.name LIKE %:keyword% AND m.isDeleted = false)) " +
           "ORDER BY s.createdAt DESC")
    Page<Store> findByStoreOrMenuNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 통합 검색: 매장명 + 메뉴명 (OPEN 상태만, 페이징)
     * 가게 또는 메뉴 검색 - 영업 중인 가게만
     */
    @Query("SELECT DISTINCT s FROM Store s WHERE s.isDeleted = false AND s.status = :status AND " +
           "(s.name LIKE %:keyword% OR s.id IN " +
           "(SELECT m.storeId FROM Menu m WHERE m.name LIKE %:keyword% AND m.isDeleted = false)) " +
           "ORDER BY s.createdAt DESC")
    Page<Store> findByStoreOrMenuNameContainingAndStatus(@Param("keyword") String keyword, 
                                                         @Param("status") StoreStatus status, 
                                                         Pageable pageable);

    /**
     * 매장 ID 목록으로 매장 조회 (페이징)
     * 검색 결과용
     */
    Page<Store> findByIdInAndIsDeletedFalseOrderByCreatedAtDesc(List<Long> storeIds, Pageable pageable);

    /**
     * 매장 상세 조회 (삭제되지 않은 매장만)
     * 가게 상세 정보 조회
     */
    Optional<Store> findByIdAndIsDeletedFalse(Long storeId);

    /**
     * 사장님 특정 매장 조회 (권한 체크 포함)
     * 내 가게 상세 정보 조회
     */
    Optional<Store> findByIdAndOwnerIdAndIsDeletedFalse(Long storeId, Long ownerId);

    /**
     * 영업 중인 매장 수 조회
     */
    long countByStatusAndIsDeletedFalse(StoreStatus status);

    /**
     * 카테고리별 영업 중인 매장 수 조회
     */
    long countByCategoryIdAndStatusAndIsDeletedFalse(Long categoryId, StoreStatus status);

    /**
     * 카테고리별 모든 매장 조회 (영업 상태 무관, 페이징)
     */
    Page<Store> findByCategoryIdAndIsDeletedFalseOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    /**
     * 모든 매장 조회 (영업 상태 무관, 페이징) - 전체 카테고리
     */
    Page<Store> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
} 