package com.jeonjueats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 이미지 업로드 API 응답 DTO
 * 업로드된 이미지의 URL과 기본 정보를 포함
 */
@Getter
@Builder
@AllArgsConstructor
public class ImageUploadResponseDto {
    
    /**
     * 업로드된 이미지 파일명
     */
    private final String filename;
    
    /**
     * 웹에서 접근 가능한 이미지 URL
     */
    private final String imageUrl;
    
    /**
     * 원본 파일명
     */
    private final String originalFilename;
    
    /**
     * 파일 크기 (바이트)
     */
    private final Long fileSize;
    
    /**
     * MIME 타입 (예: image/jpeg, image/png)
     */
    private final String contentType;
} 