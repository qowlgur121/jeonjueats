package com.jeonjueats.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Spring MVC 설정 클래스
 * 정적 리소스 서빙, CORS 설정 및 웹 관련 설정을 담당
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    /**
     * CORS 설정
     * 프론트엔드(localhost:3000, localhost:3001)에서 백엔드 API 접근 허용
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",  // 고객용 Vue.js 앱
                    "http://localhost:3001"   // 사장님용 Vue.js 앱
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1시간 캐싱
        
        System.out.println("CORS 설정 완료: localhost:3000, localhost:3001에서 API 접근 허용");
    }

    /**
     * 정적 리소스 핸들러 설정
     * 업로드된 이미지 파일들을 웹에서 접근할 수 있도록 설정
     * 
     * URL 패턴: /api/images/**
     * 실제 경로: file:./uploads/ (프로젝트 루트 기준)
     * 
     * 예시:
     * - 요청: http://localhost:8080/api/images/chicken1.jpg
     * - 실제 파일: ./uploads/images/chicken1.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 업로드 디렉토리를 절대 경로로 변환
        String uploadPath = Paths.get(uploadDirectory).toAbsolutePath().toString();
        
        // 2. /api/images/** URL 패턴을 로컬 파일 시스템에 매핑
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:" + uploadPath + "/images/")
                .setCachePeriod(3600) // 1시간 캐싱
                .resourceChain(true);

        // 로깅용 - 개발 시 확인
        System.out.println("정적 리소스 서빙 설정 완료:");
        System.out.println("- URL 패턴: /api/images/**");
        System.out.println("- 실제 경로: file:" + uploadPath + "/images/");
    }
} 