package com.jeonjueats.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 토큰 기반 인증 필터
 * 모든 HTTP 요청에서 Authorization 헤더의 JWT 토큰을 검증하고
 * 유효한 경우 Spring Security 컨텍스트에 인증 정보를 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * JWT 토큰 검증 및 인증 처리
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        log.debug("JWT 필터 실행: {} {}", request.getMethod(), request.getRequestURI());
        
        // 1. Authorization 헤더에서 JWT 토큰 추출
        String token = extractTokenFromRequest(request);
        
        log.debug("추출된 토큰: {}", token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null");
        
        if (token != null) {
            try {
                // 2. JWT 토큰 유효성 검증
                if (jwtUtil.validateToken(token)) {
                    // 3. 토큰에서 사용자 정보 추출
                    String email = jwtUtil.getUserEmailFromToken(token);
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String role = jwtUtil.getUserRoleFromToken(token);
                    
                    log.debug("JWT 인증 성공: userId={}, email={}, role={}", userId, email, role);
                    
                    // 4. Spring Security Authentication 객체 생성
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            email,  // principal: 사용자 식별자 (이메일)
                            null,   // credentials: 이미 검증된 토큰이므로 null
                            Collections.singletonList(new SimpleGrantedAuthority(role))  // authorities: 권한 정보
                    );
                    
                    // 5. SecurityContext에 인증 정보 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                } else {
                    log.warn("유효하지 않은 JWT 토큰: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                }
            } catch (Exception e) {
                log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
                // 토큰 처리 실패 시 SecurityContext 클리어
                SecurityContextHolder.clearContext();
            }
        }
        
        // 6. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     * Authorization 헤더에서 "Bearer " 접두사 제거 후 토큰 반환
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 또는 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // "Bearer " 제거
        }
        
        return null;
    }
} 