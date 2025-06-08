package com.jeonjueats.controller;

import com.jeonjueats.dto.OrderCreateRequestDto;
import com.jeonjueats.dto.OrderListResponseDto;
import com.jeonjueats.dto.OrderResponseDto;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
 * 주문 컨트롤러
 * 일반 사용자의 주문 관련 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    /**
     * 주문 생성
     * 장바구니 내용을 기반으로 주문을 생성합니다.
     * 
     * @param requestDto 주문 생성 요청 (배달 주소, 요청사항)
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 생성된 주문 정보
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderCreateRequestDto requestDto,
            HttpServletRequest request) {
        
        log.info("주문 생성 요청 - 배달 주소: {}", requestDto.getDeliveryAddress1());
        
        Long userId = getCurrentUserId(request);
        OrderResponseDto orderResponse = orderService.createOrder(userId, requestDto);
        
        log.info("주문 생성 성공 - 주문 ID: {}, 사용자 ID: {}, 총 금액: {}", 
                orderResponse.getOrderId(), userId, orderResponse.getTotalPrice());

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    /**
     * 내 주문 목록 조회 (페이징)
     * 로그인한 사용자의 주문 내역을 페이징으로 조회합니다.
     * 
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 페이징된 주문 목록
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<OrderListResponseDto>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        log.info("주문 목록 조회 요청 - 페이지: {}, 크기: {}", page, size);
        
        Long userId = getCurrentUserId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderListResponseDto> orders = orderService.getMyOrders(userId, pageable);
        
        log.info("주문 목록 조회 성공 - 사용자 ID: {}, 총 주문 수: {}", userId, orders.getTotalElements());

        return ResponseEntity.ok(orders);
    }

    /**
     * 주문 상세 조회
     * 특정 주문의 상세 정보를 조회합니다.
     * 
     * @param orderId 주문 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 주문 상세 정보
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<OrderResponseDto> getMyOrderDetail(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        
        log.info("주문 상세 조회 요청 - 주문 ID: {}", orderId);
        
        Long userId = getCurrentUserId(request);
        OrderResponseDto orderDetail = orderService.getMyOrderDetail(userId, orderId);
        
        log.info("주문 상세 조회 성공 - 주문 ID: {}, 사용자 ID: {}", orderId, userId);

        return ResponseEntity.ok(orderDetail);
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