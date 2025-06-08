package com.jeonjueats.controller;

import com.jeonjueats.dto.OwnerOrderListResponseDto;
import com.jeonjueats.dto.OrderResponseDto;
import com.jeonjueats.dto.OrderStatusUpdateRequestDto;
import com.jeonjueats.entity.OrderStatus;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.OwnerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 사장님용 주문 관리 컨트롤러
 * 가게별 주문 조회 및 관리 API 제공
 */
@RestController
@RequestMapping("/api/owner/stores/{storeId}/orders")
@RequiredArgsConstructor
@Slf4j
public class OwnerOrderController {

    private final OwnerOrderService ownerOrderService;
    private final JwtUtil jwtUtil;

    /**
     * 가게별 주문 목록 조회 (상태별 필터링 지원)
     * GET /api/owner/stores/{storeId}/orders?status=PENDING&page=0&size=10
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Page<OwnerOrderListResponseDto>> getStoreOrders(
            @PathVariable Long storeId,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request) {

        log.info("사장님용 주문 목록 조회 - storeId: {}, status: {}, page: {}, size: {}", 
                storeId, status, pageable.getPageNumber(), pageable.getPageSize());

        // JWT에서 사장님 ID 추출
        String token = jwtUtil.resolveToken(request);
        Long ownerId = jwtUtil.getUserIdFromToken(token);

        // 주문 목록 조회
        Page<OwnerOrderListResponseDto> orders = ownerOrderService.getStoreOrders(storeId, status, ownerId, pageable);

        log.info("사장님용 주문 목록 조회 완료 - 총 {}건 조회, 현재 페이지: {}/{}", 
                orders.getTotalElements(), orders.getNumber() + 1, orders.getTotalPages());

        return ResponseEntity.ok(orders);
    }

    /**
     * 가게별 특정 주문 상세 조회
     * GET /api/owner/stores/{storeId}/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<OrderResponseDto> getStoreOrderDetail(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            HttpServletRequest request) {

        log.info("사장님용 주문 상세 조회 - storeId: {}, orderId: {}", storeId, orderId);

        // JWT에서 사장님 ID 추출
        String token = jwtUtil.resolveToken(request);
        Long ownerId = jwtUtil.getUserIdFromToken(token);

        // 주문 상세 조회
        OrderResponseDto orderDetail = ownerOrderService.getStoreOrderDetail(storeId, orderId, ownerId);

        log.info("사장님용 주문 상세 조회 완료 - orderId: {}, status: {}", orderId, orderDetail.getStatus());

        return ResponseEntity.ok(orderDetail);
    }

    /**
     * 사장님용 주문 상태 변경
     * PUT /api/owner/stores/{storeId}/orders/{orderId}/status
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequestDto request,
            HttpServletRequest httpRequest) {
        
        log.info("사장님용 주문 상태 변경 - storeId: {}, orderId: {}, newStatus: {}", 
                storeId, orderId, request.getNewStatus());

        // JWT에서 사장님 ID 추출
        String token = jwtUtil.resolveToken(httpRequest);
        Long ownerId = jwtUtil.getUserIdFromToken(token);

        // 주문 상태 변경
        OrderResponseDto updatedOrder = ownerOrderService.updateOrderStatus(storeId, orderId, ownerId, request);
        
        log.info("주문 상태 변경 완료 - orderId: {}, newStatus: {}", orderId, updatedOrder.getStatus());
        
        return ResponseEntity.ok(updatedOrder);
    }
} 