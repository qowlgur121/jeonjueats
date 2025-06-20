package com.jeonjueats.service;

import com.jeonjueats.dto.UserProfileDto;
import com.jeonjueats.entity.User;
import com.jeonjueats.exception.UnauthorizedAccessException;
import com.jeonjueats.exception.UserNotFoundException;
import com.jeonjueats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 사용자 프로필 조회 및 수정 기능 포함
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 내 정보 조회
     * 
     * @param userId 현재 로그인한 사용자 ID
     * @return UserProfileDto 사용자 프로필 정보
     */
    public UserProfileDto getMyProfile(Long userId) {
        log.info("내 정보 조회 요청 - 사용자 ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return UserProfileDto.of(user.getId(), user.getEmail(), user.getNickname(), user.getPhoneNumber(), user.getRole().name(),
                                user.getDefaultZipcode(), user.getDefaultAddress1(), user.getDefaultAddress2());
    }

    /**
     * 내 정보 수정
     * 닉네임, 전화번호, 기본 배달 주소, 비밀번호 수정 가능
     * 
     * @param userId 현재 로그인한 사용자 ID
     * @param nickname 수정할 닉네임 (nullable)
     * @param phoneNumber 수정할 전화번호 (nullable)
     * @param defaultZipcode 수정할 기본 배달 주소 우편번호 (nullable)
     * @param defaultAddress1 수정할 기본 배달 주소 기본 주소 (nullable)
     * @param defaultAddress2 수정할 기본 배달 주소 상세 주소 (nullable)
     * @param currentPassword 현재 비밀번호 (비밀번호 변경 시 필수)
     * @param newPassword 새 비밀번호 (nullable)
     * @return 업데이트된 UserProfileDto
     */
    @Transactional
    public UserProfileDto updateMyProfile(Long userId,
                                        String nickname,
                                        String phoneNumber,
                                        String defaultZipcode,
                                        String defaultAddress1,
                                        String defaultAddress2,
                                        String currentPassword,
                                        String newPassword) {
        log.info("내 정보 수정 요청 - 사용자 ID: {}, 닉네임: {}, 우편번호: {}", userId, nickname, defaultZipcode);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 닉네임이 제공되었고, 기존 닉네임과 다르며, 다른 사용자가 이미 사용 중인 경우 검증
        if (nickname != null && !nickname.isEmpty() && !user.getNickname().equals(nickname)) {
            if (userRepository.findByNickname(nickname).isPresent()) {
                throw new IllegalArgumentException("NICKNAME_ALREADY_EXISTS: 이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(nickname);
        }

        // 전화번호 업데이트
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }

        // 주소 정보 업데이트
        if (defaultZipcode != null) {
            user.setDefaultZipcode(defaultZipcode);
        }
        if (defaultAddress1 != null) {
            user.setDefaultAddress1(defaultAddress1);
        }
        if (defaultAddress2 != null) {
            user.setDefaultAddress2(defaultAddress2);
        }

        // 비밀번호 업데이트
        if (newPassword != null && !newPassword.isEmpty()) {
            if (currentPassword == null || currentPassword.isEmpty()) {
                throw new IllegalArgumentException("CURRENT_PASSWORD_REQUIRED: 비밀번호 변경 시 현재 비밀번호가 필요합니다.");
            }
            
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("INVALID_CURRENT_PASSWORD: 현재 비밀번호가 일치하지 않습니다.");
            }
            
            user.setPassword(passwordEncoder.encode(newPassword));
            log.info("비밀번호 변경 완료 - 사용자 ID: {}", userId);
        }

        // 변경 감지(Dirty Checking)에 의해 트랜잭션 종료 시 자동 저장
        // userRepository.save(user); // 명시적 호출 필요 없음

        log.info("내 정보 수정 완료 - 사용자 ID: {}", userId);
        return UserProfileDto.of(user.getId(), user.getEmail(), user.getNickname(), user.getPhoneNumber(), user.getRole().name(),
                                user.getDefaultZipcode(), user.getDefaultAddress1(), user.getDefaultAddress2());
    }
} 