package com.jeonjueats.service;

import com.jeonjueats.dto.ImageUploadResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 이미지 업로드 관련 비즈니스 로직 처리 서비스
 */
@Slf4j
@Service
public class ImageService {

    // 허용 가능한 이미지 MIME 타입들 (JPEG, PNG만)
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png"
    );

    // 허용 가능한 도메인들
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
            "stores", "menus", "profiles", "categories"
    );

    // 최대 파일 크기: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 이미지 파일을 서버에 업로드하고 접근 가능한 URL 반환
     *
     * @param file 업로드할 이미지 파일
     * @param domain 이미지가 사용될 도메인 (stores, menus 등)
     * @return 업로드된 이미지 정보
     * @throws IOException 파일 저장 중 오류 발생 시
     * @throws IllegalArgumentException 파일 유효성 검사 실패 시
     */
    public ImageUploadResponseDto uploadImage(MultipartFile file, String domain) throws IOException {
        log.info("이미지 업로드 시작: originalFilename={}, size={}, contentType={}, domain={}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType(), domain);

        // 1. 파일 및 도메인 유효성 검사
        validateFile(file);
        validateDomain(domain);

        // 2. 업로드 디렉토리 생성
        Path uploadPath = createUploadDirectory(domain);

        // 3. 고유한 파일명 생성
        String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());

        // 4. 파일 저장
        Path targetLocation = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("이미지 업로드 완료: filename={}, path={}", uniqueFilename, targetLocation);

        // 5. 응답 DTO 생성
        return ImageUploadResponseDto.builder()
                .filename(uniqueFilename)
                .imageUrl(generateImageUrl(domain, uniqueFilename))
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    /**
     * 업로드된 파일의 유효성을 검사
     * PRD 요구사항에 따라 JPEG, PNG만 허용하고 5MB 크기 제한 적용
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 확인 (5MB 제한)
        if (file.getSize() > MAX_FILE_SIZE) {
            RuntimeException exception = new RuntimeException("FILE_SIZE_EXCEEDED");
            exception.addSuppressed(new IllegalArgumentException("파일 크기가 5MB를 초과합니다."));
            throw exception;
        }

        // MIME 타입 확인 (JPEG, PNG만 허용)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            RuntimeException exception = new RuntimeException("INVALID_FILE_FORMAT");
            exception.addSuppressed(new IllegalArgumentException("지원하지 않는 파일 형식입니다. JPEG 또는 PNG 파일만 업로드 가능합니다."));
            throw exception;
        }

        // 파일명 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
        }

        // 파일 확장자 추가 검증
        String filename = originalFilename.toLowerCase();
        if (!filename.endsWith(".jpg") && !filename.endsWith(".jpeg") && !filename.endsWith(".png")) {
            RuntimeException exception = new RuntimeException("INVALID_FILE_FORMAT");
            exception.addSuppressed(new IllegalArgumentException("파일 확장자가 올바르지 않습니다. .jpg, .jpeg, .png 파일만 허용됩니다."));
            throw exception;
        }
    }

    /**
     * 도메인 유효성 검사
     */
    private void validateDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("도메인 파라미터가 필요합니다.");
        }

        if (!ALLOWED_DOMAINS.contains(domain.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 도메인입니다. (stores, menus, profiles, categories만 가능)");
        }
    }

    /**
     * 업로드 디렉토리 생성 (기존 images 폴더에 저장)
     */
    private Path createUploadDirectory(String domain) throws IOException {
        // 기존 images 폴더 구조에 맞춤: uploads/images/
        Path uploadPath = Paths.get(uploadDirectory, "images");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("업로드 디렉토리 생성: {}", uploadPath);
        }

        return uploadPath;
    }

    /**
     * 중복 방지를 위한 고유한 파일명 생성
     */
    private String generateUniqueFilename(String originalFilename) {
        // 파일 확장자 추출
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }

        // UUID + 타임스탬프 + 확장자로 고유한 파일명 생성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s%s", timestamp, uuid, extension);
    }

    /**
     * 웹에서 접근 가능한 이미지 URL 생성
     */
    private String generateImageUrl(String domain, String filename) {
        // 상대 URL로 반환하여 배포 환경에서도 동작하도록 함
        return String.format("/api/images/%s", filename);
    }
} 