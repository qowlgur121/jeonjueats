package com.jeonjueats.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Spring MVC 설정 클래스
 * 정적 리소스 서빙 및 웹 관련 설정을 담당
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    /**
     * 정적 리소스 핸들러 설정
     * 업로드된 이미지 파일들을 웹에서 접근할 수 있도록 설정
     * 
     * URL 패턴: /api/images/**
     * 실제 경로: file:./uploads/ (프로젝트 루트 기준)
     * 
     * 예시:
     * - 요청: http://localhost:8080/api/images/stores/2025/01/15/20250115143022_a1b2c3d4.jpg
     * - 실제 파일: ./uploads/stores/2025/01/15/20250115143022_a1b2c3d4.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 업로드 디렉토리를 절대 경로로 변환
        String uploadPath = Paths.get(uploadDirectory).toAbsolutePath().toString();
        
        // 2. /api/images/** URL 패턴을 로컬 파일 시스템에 매핑
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(3600) // 1시간 캐싱
                .resourceChain(true);

        // 로깅용 - 개발 시 확인
        System.out.println("정적 리소스 서빙 설정 완료:");
        System.out.println("- URL 패턴: /api/images/**");
        System.out.println("- 실제 경로: file:" + uploadPath + "/");
    }
} 