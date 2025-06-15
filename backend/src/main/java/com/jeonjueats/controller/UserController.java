package com.jeonjueats.controller;

import com.jeonjueats.dto.UserProfileDto;
import com.jeonjueats.dto.UserProfileUpdateRequestDto;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 관련 API 컨트롤러
 * JWT 인증이 필요한 사용자 정보 조회 및 수정 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 현재 로그인한 사용자의 프로필 정보 조회
     * JWT 토큰에서 추출한 인증 정보를 바탕으로 사용자 정보 반환
     *
     * @return 사용자 프로필 정보
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_OWNER')")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) {
        try {
            // JWT에서 사용자 ID 추출
            String token = jwtUtil.resolveToken(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            log.info("현재 인증된 사용자 프로필 조회 요청: userId={}", userId);

            // 서비스에서 사용자 정보 조회
            UserProfileDto profileDto = userService.getMyProfile(userId);

            return ResponseEntity.ok(profileDto);

        } catch (RuntimeException e) {
            log.error("사용자 프로필 조회 중 오류 발생: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보 수정
     * 닉네임과 기본 배달 주소 수정 가능
     *
     * @param requestDto 수정할 프로필 정보
     * @param bindingResult 유효성 검사 결과
     * @param request HTTP 요청 객체
     * @return 업데이트된 사용자 프로필 정보
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_OWNER')")
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto,
            BindingResult bindingResult,
            HttpServletRequest request) {

        log.info("사용자 프로필 수정 API 호출 - 닉네임: {}, 우편번호: {}", requestDto.getNickname(), requestDto.getDefaultZipcode());

        // 1. 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "INVALID_INPUT_VALUE");
            errorResponse.put("message", "입력값이 올바르지 않습니다.");
            Map<String, String> fieldErrors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    fieldErrors.put(error.getField(), error.getDefaultMessage())
            );
            errorResponse.put("errors", fieldErrors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            // JWT에서 사용자 ID 추출
            String token = jwtUtil.resolveToken(request);
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 서비스에서 프로필 수정 처리
            UserProfileDto updatedProfile = userService.updateMyProfile(
                    userId,
                    requestDto.getNickname(),
                    requestDto.getPhoneNumber(),
                    requestDto.getDefaultZipcode(),
                    requestDto.getDefaultAddress1(),
                    requestDto.getDefaultAddress2(),
                    requestDto.getCurrentPassword(),
                    requestDto.getNewPassword()
            );

            log.info("사용자 프로필 수정 완료 - userId: {}", userId);
            return ResponseEntity.ok(updatedProfile);

        } catch (IllegalArgumentException e) {
            // 닉네임 중복 등 비즈니스 로직 오류 처리
            log.warn("사용자 프로필 수정 실패: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            if (e.getMessage().contains("NICKNAME_ALREADY_EXISTS")) {
                errorResponse.put("code", "NICKNAME_ALREADY_EXISTS");
                errorResponse.put("message", "이미 사용 중인 닉네임입니다.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            } else if (e.getMessage().contains("CURRENT_PASSWORD_REQUIRED")) {
                errorResponse.put("code", "CURRENT_PASSWORD_REQUIRED");
                errorResponse.put("message", "비밀번호 변경 시 현재 비밀번호가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (e.getMessage().contains("INVALID_CURRENT_PASSWORD")) {
                errorResponse.put("code", "INVALID_CURRENT_PASSWORD");
                errorResponse.put("message", "현재 비밀번호가 일치하지 않습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else {
                errorResponse.put("code", "BAD_REQUEST");
                errorResponse.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        } catch (Exception e) {
            log.error("사용자 프로필 수정 중 예상치 못한 오류 발생: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "서버 내부 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 