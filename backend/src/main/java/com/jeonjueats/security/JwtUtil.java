package com.jeonjueats.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT(JSON Web Token) 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
 * 
 * - Access Token 생성 (로그인 성공 시)
 * - 토큰에서 사용자 정보 추출 (인증 필터에서 사용)
 * - 토큰 유효성 검증 (만료, 변조 확인)
 * 
 * - 로그인 성공 시 accessToken 발급
 * - Bearer Token 방식으로 인증 헤더 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    
    /**
     * JWT 토큰 서명에 사용할 비밀 키
     * application.yml의 jwt.secret-key 값을 주입받음
     */
    @Value("${jwt.secret-key}")
    private String secretKey;
    
    /**
     * JWT 토큰 만료 시간 (밀리초)
     * application.yml의 jwt.expiration-time 값을 주입받음 (기본: 24시간)
     */
    @Value("${jwt.expiration-time}")
    private long expirationTime;
    
    /**
     * JWT Access Token 생성
     * 
     * @param userId 사용자 ID (Primary Key)
     * @param email 사용자 이메일 (로그인 ID)
     * @param role 사용자 역할 (ROLE_USER 또는 ROLE_OWNER)
     * @return 생성된 JWT 토큰 문자열
     * 
     * - sub: 사용자 이메일 (Subject)
     * - userId: 사용자 ID 
     * - role: 사용자 권한
     * - iat: 발급 시간
     * - exp: 만료 시간
     */
    public String createAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
                .setSubject(email)                    // 이메일을 Subject로 설정
                .claim("userId", userId)              // 사용자 ID 클레임 추가
                .claim("role", role)                  // 권한 정보 클레임 추가
                .setIssuedAt(now)                     // 발급 시간
                .setExpiration(expiryDate)            // 만료 시간
                .signWith(getSigningKey())            // 비밀키로 서명
                .compact();                           // 최종 토큰 문자열 생성
    }
    
    /**
     * JWT 토큰에서 사용자 이메일 추출
     * 
     * @param token JWT 토큰 문자열
     * @return 토큰에 저장된 사용자 이메일
     */
    public String getUserEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }
    
    /**
     * JWT 토큰에서 사용자 ID 추출
     * 
     * @param token JWT 토큰 문자열
     * @return 토큰에 저장된 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * JWT 토큰에서 사용자 권한 추출
     * 
     * @param token JWT 토큰 문자열
     * @return 토큰에 저장된 사용자 권한 (ROLE_USER, ROLE_OWNER)
     */
    public String getUserRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * JWT 토큰 유효성 검증
     * 
     * @param token JWT 토큰 문자열
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     * 
     * 검증 내용:
     * - 토큰 형식 유효성
     * - 서명 검증 (변조 확인)
     * - 만료 시간 확인
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 토큰 형식: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있음: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("JWT 서명 검증 실패: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * JWT 토큰에서 Claims 정보 추출 (내부 메서드)
     * 
     * @param token JWT 토큰 문자열
     * @return JWT Claims 객체
     * @throws JwtException 토큰 파싱 실패 시
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())         // 서명 검증용 키 설정 (0.12.x API)
                .build()
                .parseSignedClaims(token)            // 토큰 파싱 및 서명 검증 (0.12.x API)
                .getPayload();                       // Claims 반환 (0.12.x API)
    }
    
    /**
     * JWT 서명/검증용 비밀키 생성 (내부 메서드)
     * 
     * @return HMAC-SHA 알고리즘용 SecretKey 객체
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     * "Bearer [TOKEN]" 형식에서 [TOKEN] 부분만 추출
     * 
     * @param request HTTP 서블릿 요청 객체
     * @return 추출된 JWT 토큰 문자열, 없으면 null
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }
        return null;
    }
} 