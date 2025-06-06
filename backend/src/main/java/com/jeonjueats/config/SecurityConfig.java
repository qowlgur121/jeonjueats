package com.jeonjueats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 * JWT 인증 및 API 보안 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 비밀번호 암호화를 위한 BCrypt 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (REST API이므로)
            .csrf(csrf -> csrf.disable())
            
            // 세션 사용 안함 (JWT 사용)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 경로별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // Swagger UI 관련 경로는 모두 허용
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                
                // H2 콘솔 허용 (개발용)
                .requestMatchers("/h2-console/**").permitAll()
                
                // 인증 관련 API는 모두 허용
                .requestMatchers("/api/auth/**").permitAll()
                
                // 이미지 관련 API는 모두 허용 (업로드는 인증 필요하지만 조회는 허용)
                .requestMatchers("/api/images/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                
                // 카테고리 조회는 허용
                .requestMatchers("/api/categories").permitAll()
                
                // 가게 조회 및 검색은 허용
                .requestMatchers("/api/stores/**").permitAll()
                .requestMatchers("/api/search/**").permitAll()
                
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated())
            
            // H2 콘솔을 위한 추가 설정
            .headers(headers -> headers
                .frameOptions().sameOrigin())  // H2 콘솔의 iframe 사용 허용
            
            // Form 로그인 비활성화 (JWT 사용)
            .formLogin(form -> form.disable())
            
            // HTTP Basic 인증 비활성화
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
} 