package com.jeonjueats.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티 (PRD 4.2.1 user 테이블 명세)
 * 
 * 일반 사용자(ROLE_USER)와 사장님(ROLE_OWNER) 모두 포함
 */
@Entity
@Table(name = "user", 
       indexes = {
           @Index(name = "idx_user_email", columnList = "email"),
           @Index(name = "idx_user_nickname", columnList = "nickname")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password") // 비밀번호는 toString에서 제외
public class User {
    
    /**
     * 사용자 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 이메일 (로그인 ID로 사용, 중복 불가)
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    /**
     * 암호화된 비밀번호
     */
    @Column(nullable = false, length = 255)
    private String password;
    
    /**
     * 닉네임 (중복 불가)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;
    
    /**
     * 사용자 역할 (ROLE_USER, ROLE_OWNER)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
    
    /**
     * 소셜 로그인 제공자 (MVP에서는 'LOCAL' 고정)
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String provider = "LOCAL";
    
    /**
     * 소셜 로그인 제공자별 사용자 ID (MVP에서는 사용 안 함)
     */
    @Column(length = 255)
    private String providerId;
    
    /**
     * 기본 배달 주소 - 우편번호
     */
    @Column(length = 10)
    private String defaultZipcode;
    
    /**
     * 기본 배달 주소 - 기본주소 (도로명주소)
     */
    @Column(length = 255)
    private String defaultAddress1;
    
    /**
     * 기본 배달 주소 - 상세주소
     */
    @Column(length = 255)
    private String defaultAddress2;
    
    /**
     * 전화번호 (MVP 이후 기능)
     */
    @Column(length = 20)
    private String phoneNumber;
    
    /**
     * 프로필 이미지 URL (MVP 이후 기능)
     */
    @Column(length = 255)
    private String profileImageUrl;
    
    /**
     * 마지막 로그인 시간 (MVP 이후 기능)
     */
    private LocalDateTime lastLoggedInAt;
    
    /**
     * 계정 생성 일시 
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 정보 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 