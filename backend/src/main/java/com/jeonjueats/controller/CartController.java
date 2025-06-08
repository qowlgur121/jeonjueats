package com.jeonjueats.controller;

import com.jeonjueats.dto.CartItemAddRequestDto;
import com.jeonjueats.dto.CartItemRequestDto;
import com.jeonjueats.dto.CartItemResponseDto;
import com.jeonjueats.dto.CartItemUpdateRequestDto;
import com.jeonjueats.dto.CartResponseDto;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 장바구니 컨트롤러
 * 일반 사용자의 장바구니 관련 API 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    /**
     * 장바구니 메뉴 추가/수량 변경 통합 API 
     * cartItemId 존재 여부로 추가/변경을 구분하여 처리합니다.
     * 
     * @param requestDto 메뉴 추가 또는 수량 변경 정보
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 처리된 장바구니 아이템 정보
     */
    @PutMapping("/items")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CartItemResponseDto> processCartItem(
            @Valid @RequestBody CartItemRequestDto requestDto,
            HttpServletRequest request) {
        
        log.info("장바구니 처리 요청 - 타입: {}", 
                requestDto.isAddMenuRequest() ? "메뉴 추가" : "수량 변경");
        
        Long userId = getCurrentUserId(request);
        CartItemResponseDto response = cartService.processCartItem(userId, requestDto);
        
        log.info("장바구니 처리 완료 - 사용자 ID: {}, 메뉴: {}, 장바구니 아이템 ID: {}", 
                userId, response.getMenuName(), response.getCartItemId());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장바구니에 메뉴 추가 API (기존 API, 호환성 유지)
     * @deprecated 새로운 통합 API PUT /api/carts/items 사용 권장
     */
    @Deprecated
    @PostMapping("/items")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CartItemResponseDto> addMenuToCart(
            @Valid @RequestBody CartItemAddRequestDto requestDto,
            HttpServletRequest request) {
        
        log.info("[DEPRECATED] 장바구니 추가 요청 - 메뉴 ID: {}, 수량: {}", 
                requestDto.getMenuId(), requestDto.getQuantity());
        
        Long userId = getCurrentUserId(request);
        CartItemResponseDto response = cartService.addMenuToCart(userId, requestDto);
        
        log.info("[DEPRECATED] 장바구니 추가 완료 - 사용자 ID: {}, 메뉴: {}, 장바구니 아이템 ID: {}", 
                userId, response.getMenuName(), response.getCartItemId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 장바구니 조회 API
     * 현재 사용자의 장바구니 내용과 주문 요약 정보를 조회합니다.
     * 
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 장바구니 전체 정보 (아이템 목록, 주문 요약 등)
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CartResponseDto> getCart(HttpServletRequest request) {
        
        log.info("장바구니 조회 요청");
        
        Long userId = getCurrentUserId(request);
        CartResponseDto response = cartService.getCart(userId);
        
        log.info("장바구니 조회 완료 - 사용자 ID: {}, 아이템 수: {}, 총 금액: {}", 
                userId, response.getTotalItemCount(), response.getFinalPrice());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 아이템 수량 변경 API (기존 API, 호환성 유지)
     * @deprecated 새로운 통합 API PUT /api/carts/items 사용 권장
     */
    @Deprecated
    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CartItemResponseDto> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequestDto requestDto,
            HttpServletRequest request) {
        
        log.info("[DEPRECATED] 장바구니 아이템 수량 변경 요청 - 아이템 ID: {}, 새 수량: {}", 
                cartItemId, requestDto.getQuantity());
        
        Long userId = getCurrentUserId(request);
        CartItemResponseDto response = cartService.updateCartItem(userId, cartItemId, requestDto.getQuantity());
        
        log.info("[DEPRECATED] 장바구니 아이템 수량 변경 완료 - 사용자 ID: {}, 아이템 ID: {}, 새 수량: {}", 
                userId, cartItemId, response.getQuantity());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 아이템 삭제 API
     * 특정 장바구니 아이템을 삭제합니다.
     * 
     * @param cartItemId 삭제할 장바구니 아이템 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 204 No Content
     */
    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable Long cartItemId,
            HttpServletRequest request) {
        
        log.info("장바구니 아이템 삭제 요청 - 아이템 ID: {}", cartItemId);
        
        Long userId = getCurrentUserId(request);
        cartService.deleteCartItem(userId, cartItemId);
        
        log.info("장바구니 아이템 삭제 완료 - 사용자 ID: {}, 아이템 ID: {}", userId, cartItemId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * 전체 장바구니 비우기 API
     * 사용자의 장바구니를 완전히 비웁니다.
     * 
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 204 No Content
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        
        log.info("전체 장바구니 비우기 요청");
        
        Long userId = getCurrentUserId(request);
        cartService.clearCart(userId);
        
        log.info("전체 장바구니 비우기 완료 - 사용자 ID: {}", userId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * JWT 토큰에서 사용자 ID 추출 
     * 
     * @param request HTTP 요청
     * @return 사용자 ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("JWT 토큰을 찾을 수 없습니다.");
    }
} 