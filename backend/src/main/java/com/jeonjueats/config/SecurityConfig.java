package com.jeonjueats.config;

import com.jeonjueats.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정 클래스
 * 
 * 이 클래스는 웹 애플리케이션의 보안 정책을 정의합니다:
 * - 어떤 API에 누가 접근할 수 있는지 (권한 설정)
 * - 세션 관리 정책 (JWT 사용을 위한 Stateless 설정)
 * - CORS(Cross-Origin Resource Sharing) 설정 (프론트엔드-백엔드 통신 허용)
 * - CSRF(Cross-Site Request Forgery) 보호 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 비밀번호 암호화를 위한 BCryptPasswordEncoder Bean 등록
     * 
     * BCrypt란?
     * - 비밀번호를 안전하게 해싱(암호화)하는 알고리즘
     * - 같은 비밀번호라도 매번 다른 해시값이 나옴 (Salt 사용)
     * - 역계산이 매우 어려워 보안상 안전함
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     * 
     * CORS란?
     * - 웹 브라우저의 보안 정책으로, 다른 도메인 간의 요청을 제한함
     * - 예: localhost:3000(프론트엔드)에서 localhost:8080(백엔드)로 요청할 때 필요
     * - 허용할 Origin, HTTP Method, Header 등을 명시적으로 설정해야 함
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin (프론트엔드 도메인)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // 사용자용 Vue.js 앱
            "http://localhost:3001"   // 사장님용 Vue.js 앱
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 쿠키나 인증 정보 포함 요청 허용
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Spring Security 필터 체인 설정
     * 
     * 이 메서드는 전체 보안 정책의 핵심입니다:
     * 1. 어떤 URL에 어떤 권한이 필요한지 정의
     * 2. 세션 관리 정책 설정 (JWT 사용을 위해 STATELESS)
     * 3. CORS, CSRF 설정 적용
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 비활성화 (JWT 사용 시 불필요)
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // API 경로별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 공개 API
                .requestMatchers(
                    "/api/auth/**",           // 회원가입, 로그인 API
                    "/api/categories",        // 카테고리 목록 조회
                    "/api/stores/**",         // 가게 목록/상세 조회 (일반 사용자용)
                    "/api/search",            // 검색 API
                    "/swagger-ui/**",         // Swagger UI
                    "/v3/api-docs/**",        // API 문서
                    "/api-docs/**",           // API 문서
                    "/swagger-resources/**",  // Swagger 리소스
                    "/webjars/**",           // Swagger 웹자르
                    "/images/**"             // 업로드된 이미지 파일 접근
                ).permitAll()
                
                // 사장님 전용 API (ROLE_OWNER 권한 필요)
                .requestMatchers("/api/owner/**").hasRole("OWNER")
                
                // 그 외 모든 /api/** 경로는 인증 필요
                .requestMatchers("/api/**").authenticated()
                
                // 나머지 모든 요청은 허용
                .anyRequest().permitAll()
            )
            
            // 세션 관리 정책: STATELESS (JWT 사용을 위해 세션 생성하지 않음)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 