package com.jeonjueats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티 (MVP 버전)
 * MVP 범위: 기본 인증, 이메일 로그인, 역할 구분 (일반/사장님)
 * 제외 기능: 소셜 로그인, 주소 관리, 프로필 이미지, 로그인 이력
 */
@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email; // 이메일 (로그인 ID)

    @Column(nullable = false, length = 255)
    private String password; // 암호화된 비밀번호 (BCrypt)

    @Column(nullable = false, unique = true, length = 50)
    private String nickname; // 닉네임

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role; // 사용자 역할 (ROLE_USER, ROLE_OWNER)

    @Column(nullable = false, length = 20)
    private String provider = "LOCAL"; // 가입 경로 (MVP에서는 'LOCAL' 고정)

    @Column(name = "provider_id", length = 255)
    private String providerId; // 소셜 로그인 ID (MVP에서는 사용 안 함)

    // 기본 배달 주소 관련 필드들 (MVP에서 사용)
    @Column(name = "default_zipcode", length = 10)
    private String defaultZipcode; // 기본 배달 주소 - 우편번호

    @Column(name = "default_address1", length = 255)
    private String defaultAddress1; // 기본 배달 주소 - 기본 주소

    @Column(name = "default_address2", length = 255)
    private String defaultAddress2; // 기본 배달 주소 - 상세 주소

    // MVP 이후 구현 예정 필드들
    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // 전화번호 (MVP 이후)

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl; // 프로필 이미지 URL (MVP 이후)

    @Column(name = "last_logged_in_at")
    private LocalDateTime lastLoggedInAt; // 마지막 로그인 일시 (MVP 이후)

    // JPA Auditing을 통한 자동 시간 관리
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 계정 생성 일시

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 정보 수정 일시

    /**
     * 생성자
     */
    public User(String email, String password, String nickname, UserRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.provider = "LOCAL";
    }

    /**
     * 비즈니스 메서드
     */
    public boolean isOwner() {
        return this.role == UserRole.ROLE_OWNER;
    }

    public boolean isUser() {
        return this.role == UserRole.ROLE_USER;
    }

    /**
     * 기본 배달 주소 설정
     */
    public void updateDefaultAddress(String zipcode, String address1, String address2) {
        this.defaultZipcode = zipcode;
        this.defaultAddress1 = address1;
        this.defaultAddress2 = address2;
    }

    /**
     * 닉네임 수정
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * MVP 이후: 전화번호 설정
     */
    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * MVP 이후: 프로필 이미지 설정
     */
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * MVP 이후: 마지막 로그인 시간 업데이트
     */
    public void updateLastLoggedInAt() {
        this.lastLoggedInAt = LocalDateTime.now();
    }
} 