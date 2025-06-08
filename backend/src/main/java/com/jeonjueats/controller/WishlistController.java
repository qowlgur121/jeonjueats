package com.jeonjueats.controller;

import com.jeonjueats.dto.WishlistResponseDto;
import com.jeonjueats.dto.WishlistToggleResponseDto;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 찜하기 기능 Controller
 * 가게 찜하기/해제, 찜 목록 조회 API 제공
 */
@RestController
@RequestMapping("/api/wishes/stores")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtUtil jwtUtil;

    /**
     * 가게 찜 상태 토글 (찜하기/해제)
     * POST /api/wishes/stores/{storeId}/toggle
     */
    @PostMapping("/{storeId}/toggle")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WishlistToggleResponseDto> toggleWishlist(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("찜 토글 API 호출 - 가게 ID: {}", storeId);

        // JWT에서 사용자 ID 추출
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 찜 상태 토글
        WishlistToggleResponseDto response = wishlistService.toggleWishlist(userId, storeId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    /**
     * 내 찜 목록 조회 (페이징)
     * GET /api/wishes/stores
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<WishlistResponseDto>> getMyWishlists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        log.info("찜 목록 조회 API 호출 - 페이지: {}, 크기: {}", page, size);

        // JWT에서 사용자 ID 추출
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 페이징 정보 생성
        Pageable pageable = PageRequest.of(page, size);

        // 찜 목록 조회
        Page<WishlistResponseDto> wishlists = wishlistService.getMyWishlists(userId, pageable);

        return ResponseEntity.ok(wishlists);
    }

    /**
     * 특정 가게의 찜 여부 확인
     * GET /api/wishes/stores/{storeId}/status
     */
    @GetMapping("/{storeId}/status")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Boolean> getWishlistStatus(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        log.info("찜 상태 확인 API 호출 - 가게 ID: {}", storeId);

        // JWT에서 사용자 ID 추출
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 찜 여부 확인
        boolean isWished = wishlistService.isWished(userId, storeId);

        return ResponseEntity.ok(isWished);
    }
} 