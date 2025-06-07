package com.jeonjueats.controller;

import com.jeonjueats.dto.UserProfileDto;
import com.jeonjueats.entity.User;
import com.jeonjueats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 관련 API 컨트롤러
 * JWT 인증이 필요한 사용자 정보 조회 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자의 프로필 정보 조회
     * JWT 토큰에서 추출한 인증 정보를 바탕으로 사용자 정보 반환
     *
     * @return 사용자 프로필 정보
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            // 1. Spring Security 컨텍스트에서 인증 정보 획득
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("code", "UNAUTHORIZED");
                errorResponse.put("message", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // 2. 인증된 사용자의 이메일 (principal)로 사용자 조회
            String email = (String) authentication.getPrincipal();
            log.info("현재 인증된 사용자 프로필 조회: email={}", email);

            // 3. 데이터베이스에서 사용자 정보 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 4. DTO로 변환하여 응답
            UserProfileDto profileDto = UserProfileDto.of(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(profileDto);

        } catch (Exception e) {
            log.error("사용자 프로필 조회 중 오류 발생: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "서버 내부 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
} 