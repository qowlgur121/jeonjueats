package com.jeonjueats.controller;

import com.jeonjueats.dto.ImageUploadResponseDto;
import com.jeonjueats.service.ImageService;
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
@Slf4j
@RestController
@RequestMapping("/api/images")
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
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
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