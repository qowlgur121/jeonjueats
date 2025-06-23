package com.jeonjueats.controller;

import com.jeonjueats.dto.LoginRequestDto;
import com.jeonjueats.dto.LoginResponseDto;
import com.jeonjueats.dto.SignupRequestDto;
import com.jeonjueats.dto.SignupResponseDto;
import com.jeonjueats.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증 API", description = "회원가입, 로그인 등 사용자 인증을 담당하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "회원가입",
        description = "새로운 사용자(일반 사용자 또는 사장님)를 등록합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 요청 정보",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignupRequestDto.class),
                examples = {
                    @ExampleObject(
                        name = "일반 사용자 회원가입",
                        value = """
                            {
                              "email": "user@example.com",
                              "password": "password123!",
                              "nickname": "김고객",
                              "phone": "010-1234-5678",
                              "address": "전주시 완산구 효자동",
                              "role": "ROLE_USER"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "사장님 회원가입",
                        value = """
                            {
                              "email": "owner@example.com",
                              "password": "password123!",
                              "nickname": "김사장",
                              "phone": "010-9876-5432",
                              "address": "전주시 덕진구 금암동",
                              "role": "ROLE_OWNER"
                            }
                            """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignupResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                        {
                          "id": 1,
                          "email": "user@example.com",
                          "nickname": "김고객",
                          "role": "ROLE_USER",
                          "createdAt": "2024-06-23T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "입력값 유효성 검사 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "유효성 검사 오류",
                    value = """
                        {
                          "code": "INVALID_INPUT_VALUE",
                          "message": "입력값이 올바르지 않습니다.",
                          "errors": {
                            "email": "이메일 형식이 올바르지 않습니다.",
                            "password": "비밀번호는 8자 이상이어야 합니다."
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이메일 또는 닉네임 중복",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "중복 오류",
                    value = """
                        {
                          "code": "EMAIL_ALREADY_EXISTS",
                          "message": "이미 존재하는 이메일입니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "code": "INTERNAL_SERVER_ERROR",
                          "message": "서버 내부 오류가 발생했습니다."
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Parameter(description = "회원가입 요청 정보", required = true)
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

    @Operation(
        summary = "로그인",
        description = "사용자 인증을 통해 JWT 토큰을 발급받습니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 요청 정보",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequestDto.class),
                examples = {
                    @ExampleObject(
                        name = "일반 사용자 로그인",
                        value = """
                            {
                              "email": "user1@example.com",
                              "password": "password"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "사장님 로그인",
                        value = """
                            {
                              "email": "owner1@example.com",
                              "password": "password"
                            }
                            """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "user": {
                            "id": 1,
                            "email": "user1@example.com",
                            "nickname": "김고객",
                            "role": "ROLE_USER"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "입력값 유효성 검사 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "유효성 검사 오류",
                    value = """
                        {
                          "code": "INVALID_INPUT_VALUE",
                          "message": "입력값이 올바르지 않습니다.",
                          "errors": {
                            "email": "이메일 형식이 올바르지 않습니다.",
                            "password": "비밀번호는 필수입니다."
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                        {
                          "code": "AUTHENTICATION_FAILED",
                          "message": "이메일 또는 비밀번호가 올바르지 않습니다."
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "code": "INTERNAL_SERVER_ERROR",
                          "message": "서버 내부 오류가 발생했습니다."
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "로그인 요청 정보", required = true)
            @Valid @RequestBody LoginRequestDto requestDto, 
            BindingResult bindingResult) {
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