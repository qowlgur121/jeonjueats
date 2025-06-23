package com.jeonjueats.controller;

import com.jeonjueats.dto.UserProfileDto;
import com.jeonjueats.dto.UserProfileUpdateRequestDto;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "마이페이지 API", description = "사용자 정보 조회 및 수정 API (JWT 인증 필요)")
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 사용자의 프로필 정보를 조회합니다. 일반 사용자와 사장님 모두 접근 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "프로필 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileDto.class),
                examples = @ExampleObject(
                    name = "내 정보",
                    value = """
                        {
                          "userId": 1,
                          "email": "user@example.com",
                          "nickname": "전주미식가",
                          "phoneNumber": "010-1234-5678",
                          "role": "ROLE_USER",
                          "defaultZipcode": "54999",
                          "defaultAddress1": "전북 전주시 완산구 전주객사3길 22",
                          "defaultAddress2": "201호",
                          "createdAt": "2024-06-01T10:00:00Z"
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
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "인증이 필요합니다.",
                          "path": "/api/users/me"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "사용자 없음",
                    value = """
                        {
                          "code": "INTERNAL_SERVER_ERROR",
                          "message": "사용자를 찾을 수 없습니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_OWNER')")
    public ResponseEntity<?> getMyProfile(
            @Parameter(hidden = true) HttpServletRequest request) {
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

    @Operation(
        summary = "내 정보 수정",
        description = "현재 사용자의 프로필 정보를 수정합니다. 닉네임, 전화번호, 기본 배달 주소, 비밀번호 변경이 가능합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 프로필 정보 (수정할 필드만 포함)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileUpdateRequestDto.class),
                examples = {
                    @ExampleObject(
                        name = "닉네임과 주소 수정",
                        value = """
                            {
                              "nickname": "새로운닉네임",
                              "phoneNumber": "010-9876-5432",
                              "defaultZipcode": "54998",
                              "defaultAddress1": "전북 전주시 덕진구 백제대로 567",
                              "defaultAddress2": "3층 301호"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "비밀번호 변경",
                        value = """
                            {
                              "currentPassword": "oldPassword123!",
                              "newPassword": "newPassword456!"
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
            description = "프로필 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileDto.class),
                examples = @ExampleObject(
                    name = "수정된 프로필",
                    value = """
                        {
                          "userId": 1,
                          "email": "user@example.com",
                          "nickname": "새로운닉네임",
                          "phoneNumber": "010-9876-5432",
                          "role": "ROLE_USER",
                          "defaultZipcode": "54998",
                          "defaultAddress1": "전북 전주시 덕진구 백제대로 567",
                          "defaultAddress2": "3층 301호",
                          "createdAt": "2024-06-01T10:00:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효성 검사 실패, 현재 비밀번호 불일치 등)",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "유효성 검사 실패",
                        value = """
                            {
                              "code": "INVALID_INPUT_VALUE",
                              "message": "입력값이 올바르지 않습니다.",
                              "errors": {
                                "nickname": "닉네임은 2글자 이상이어야 합니다."
                              }
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "현재 비밀번호 불일치",
                        value = """
                            {
                              "code": "INVALID_CURRENT_PASSWORD",
                              "message": "현재 비밀번호가 일치하지 않습니다."
                            }
                            """
                    )
                }
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
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "인증이 필요합니다.",
                          "path": "/api/users/me"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "닉네임 중복",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "닉네임 중복",
                    value = """
                        {
                          "code": "NICKNAME_ALREADY_EXISTS",
                          "message": "이미 사용 중인 닉네임입니다."
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_OWNER')")
    public ResponseEntity<?> updateMyProfile(
            @Parameter(description = "수정할 프로필 정보", required = true)
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto,
            @Parameter(hidden = true) BindingResult bindingResult,
            @Parameter(hidden = true) HttpServletRequest request) {

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