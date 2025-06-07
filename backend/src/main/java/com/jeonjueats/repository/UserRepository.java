package com.jeonjueats.repository;

import com.jeonjueats.entity.User;
import com.jeonjueats.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository 인터페이스
 * Spring Data JPA를 통해 User 엔티티에 대한 기본 CRUD 및 커스텀 조회 메서드 제공
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회 (로그인용)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회 (중복 체크용)
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     */
    boolean existsByNickname(String nickname);

    /**
     * 역할별 사용자 목록 조회
     */
    List<User> findByRole(UserRole role);

    /**
     * 일반 사용자만 조회
     */
    List<User> findByRoleOrderByCreatedAtDesc(UserRole role);
} 