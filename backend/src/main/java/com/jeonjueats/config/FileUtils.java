package com.jeonjueats.config;

import com.jeonjueats.exception.BusinessException;
import com.jeonjueats.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 파일 처리 관련 유틸리티 클래스
 * 
 * 이미지 업로드, 파일 검증 등의 기능을 제공합니다.
 */
@Slf4j
public class FileUtils {
    
    // 허용되는 이미지 파일 확장자들
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );
    
    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB in bytes
    
    /**
     * 파일이 유효한 이미지 파일인지 검증
     * 
     * @param file 검증할 파일
     * @throws BusinessException 유효하지 않은 파일인 경우
     */
    public static void validateImageFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE.getCode(), "파일이 비어있습니다.");
        }
        
        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        
        // 파일 확장자 확인
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE.getCode(), "파일명이 유효하지 않습니다.");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE.getCode(), 
                "지원하지 않는 파일 형식입니다. 허용되는 형식: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }
    }
    
    /**
     * 파일을 지정된 디렉토리에 저장하고 저장된 파일의 상대 경로를 반환
     * 
     * @param file 저장할 파일
     * @param uploadDir 업로드 기본 디렉토리 (예: "./uploads/")
     * @param domain 파일 도메인 (예: "stores", "menus")
     * @return 저장된 파일의 상대 경로 (예: "/images/stores/uuid_filename.jpg")
     */
    public static String saveFile(MultipartFile file, String uploadDir, String domain) {
        validateImageFile(file);
        
        try {
            // 원본 파일명과 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            
            // 고유한 파일명 생성 (UUID + 원본 파일명)
            String uniqueFilename = UUID.randomUUID().toString() + "_" + 
                    StringUtils.cleanPath(originalFilename);
            
            // 저장될 디렉토리 경로 생성 (uploadDir/domain/)
            Path uploadPath = Paths.get(uploadDir, domain);
            
            // 디렉토리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created directory: {}", uploadPath);
            }
            
            // 파일 저장
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 웹에서 접근 가능한 상대 경로 반환
            String relativePath = "/images/" + domain + "/" + uniqueFilename;
            
            log.info("File saved successfully: {} -> {}", originalFilename, relativePath);
            return relativePath;
            
        } catch (IOException e) {
            log.error("Failed to save file: {}", file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
    
    /**
     * 파일명에서 확장자 추출
     * 
     * @param filename 파일명
     * @return 확장자 (소문자)
     */
    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 파일 크기를 읽기 쉬운 형식으로 변환
     * 
     * @param bytes 바이트 크기
     * @return 읽기 쉬운 크기 문자열 (예: "1.5 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
} 