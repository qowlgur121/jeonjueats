package com.jeonjueats.repository;

import com.jeonjueats.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository 인터페이스
 * Spring Data JPA를 통해 Category 엔티티에 대한 기본 CRUD 및 커스텀 조회 메서드 제공
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 카테고리명으로 조회
     */
    Optional<Category> findByName(String name);

    /**
     * 카테고리명 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 정렬 순서대로 모든 카테고리 조회
     */
    List<Category> findAllByOrderBySortOrderAsc();

    /**
     * 정렬 순서대로 카테고리 조회 (페이징 없이)
     */
    List<Category> findByOrderBySortOrderAscCreatedAtAsc();
} 