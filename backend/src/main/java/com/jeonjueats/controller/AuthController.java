package com.jeonjueats.controller;

import com.jeonjueats.dto.LoginRequestDto;
import com.jeonjueats.dto.LoginResponseDto;
import com.jeonjueats.dto.SignupRequestDto;
import com.jeonjueats.dto.SignupResponseDto;
import com.jeonjueats.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API
     * 
     * @param requestDto 회원가입 요청 정보
     * @param bindingResult 유효성 검사 결과
     * @return 회원가입 응답 정보 (201 Created)
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequestDto requestDto,
            BindingResult bindingResult) {
        
        log.info("회원가입 API 호출: email={}, nickname={}, role={}", 
                requestDto.getEmail(), requestDto.getNickname(), requestDto.getRole());

        // 1. 유효성 검사 오류 처리 (400 Bad Request)
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
            // 2. 회원가입 처리
            SignupResponseDto response = authService.signup(requestDto);
            
            log.info("회원가입 성공: userId={}, email={}", response.getId(), response.getEmail());
            
            // 3. 성공 응답 (201 Created)
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            // 4. 비즈니스 로직 오류 처리 (409 Conflict)
            log.warn("회원가입 실패: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            
            // PRD 명세에 따른 에러 코드 설정
            if (e.getMessage().contains("이메일")) {
                errorResponse.put("code", "EMAIL_ALREADY_EXISTS");
            } else if (e.getMessage().contains("닉네임")) {
                errorResponse.put("code", "NICKNAME_ALREADY_EXISTS");
            } else {
                errorResponse.put("code", "INVALID_INPUT_VALUE");
            }
            
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            
        } catch (Exception e) {
            // 5. 예상치 못한 서버 오류 처리 (500 Internal Server Error)
            log.error("회원가입 중 예상치 못한 오류 발생", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "서버 내부 오류가 발생했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 로그인 API
     * 
     * @param requestDto 로그인 요청 정보
     * @param bindingResult 유효성 검사 결과
     * @return 로그인 성공 시 JWT 토큰과 사용자 정보
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto, BindingResult bindingResult) {
        log.info("로그인 API 호출: email={}", requestDto.getEmail());
        
        // 1. 입력값 유효성 검사
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "INVALID_INPUT_VALUE");
            errorResponse.put("message", "입력값이 올바르지 않습니다.");
            errorResponse.put("errors", errors);
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // 2. 로그인 처리
            LoginResponseDto response = authService.login(requestDto);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            // 3. 인증 실패 처리
            if ("AUTHENTICATION_FAILED".equals(e.getMessage())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("code", "AUTHENTICATION_FAILED");
                errorResponse.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // 4. 기타 예외 처리
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "서버 내부 오류가 발생했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 