package com.jeonjueats.controller;

import com.jeonjueats.dto.ImageUploadResponseDto;
import com.jeonjueats.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 이미지 업로드 관련 API 컨트롤러
 * 인증된 사용자만 이미지를 업로드할 수 있음
 */
@Tag(name = "이미지 업로드 API", description = "가게 및 메뉴 이미지 업로드 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지 파일 업로드 API
     * 인증된 사용자가 이미지를 업로드하고 접근 가능한 URL을 받음
     *
     * @param file 업로드할 이미지 파일 (multipart/form-data)
     * @param domain 이미지가 사용될 도메인 (stores, menus 등)
     * @return 업로드된 이미지 정보 (201 Created)
     */
    @Operation(
        summary = "이미지 파일 업로드",
        description = "인증된 사용자가 가게 대표 이미지나 메뉴 이미지를 서버에 업로드합니다. JPEG, PNG 형식만 지원하며 최대 5MB까지 업로드 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "이미지 업로드 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "메뉴 이미지 업로드 성공",
                    value = """
                    {
                      "imageUrl": "/images/menus/abcdef12-3456-7890-abcd-ef1234567890.png",
                      "originalFileName": "갈비탕.png"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "지원하지 않는 파일 형식",
                        value = """
                        {
                          "code": "INVALID_FILE_FORMAT",
                          "message": "지원하지 않는 파일 형식입니다. JPEG 또는 PNG 파일만 업로드 가능합니다."
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "파일 크기 초과",
                        value = """
                        {
                          "code": "FILE_SIZE_EXCEEDED",
                          "message": "파일 크기가 5MB를 초과합니다."
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
                    name = "인증되지 않은 사용자",
                    value = """
                    {
                      "code": "UNAUTHORIZED",
                      "message": "인증되지 않은 사용자입니다."
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
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @Parameter(description = "업로드할 이미지 파일 (JPEG, PNG, 최대 5MB)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "이미지 분류 (stores: 가게 이미지, menus: 메뉴 이미지)", example = "menus")
            @RequestParam("domain") String domain) {
        try {
            // 1. 인증 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("code", "UNAUTHORIZED");
                errorResponse.put("message", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            String userEmail = (String) authentication.getPrincipal();
            log.info("이미지 업로드 요청: email={}, filename={}, domain={}", 
                    userEmail, file.getOriginalFilename(), domain);

            // 2. 이미지 업로드 처리
            ImageUploadResponseDto response = imageService.uploadImage(file, domain);

            log.info("이미지 업로드 성공: email={}, filename={}, imageUrl={}", 
                    userEmail, response.getFilename(), response.getImageUrl());

            // 3. 성공 응답 (201 Created)
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // 4. 일반적인 유효성 검사 실패 처리 (400 Bad Request)
            log.warn("이미지 업로드 유효성 검사 실패: {}", e.getMessage());

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", "INVALID_FILE");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (RuntimeException e) {
            // 5. 구체적인 유효성 검사 실패 처리 (400 Bad Request)
            String errorCode = e.getMessage();
            String errorMessage;
            
            if ("INVALID_FILE_FORMAT".equals(errorCode)) {
                errorMessage = "지원하지 않는 파일 형식입니다. JPEG 또는 PNG 파일만 업로드 가능합니다.";
                log.warn("이미지 업로드 실패 - 파일 형식 오류: {}", errorMessage);
            } else if ("FILE_SIZE_EXCEEDED".equals(errorCode)) {
                errorMessage = "파일 크기가 5MB를 초과합니다.";
                log.warn("이미지 업로드 실패 - 파일 크기 초과: {}", errorMessage);
            } else {
                // 일반적인 RuntimeException 처리
                errorMessage = e.getSuppressed().length > 0 ? 
                    e.getSuppressed()[0].getMessage() : e.getMessage();
                errorCode = "INVALID_FILE";
                log.warn("이미지 업로드 런타임 오류: {}", errorMessage);
            }

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", errorCode);
            errorResponse.put("message", errorMessage);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            // 6. 예상치 못한 서버 오류 처리 (500 Internal Server Error)
            log.error("이미지 업로드 중 예상치 못한 오류 발생", e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "서버 내부 오류가 발생했습니다.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 