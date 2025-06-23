package com.jeonjueats.controller;

import com.jeonjueats.dto.OwnerOrderListResponseDto;
import com.jeonjueats.dto.OrderResponseDto;
import com.jeonjueats.dto.OrderStatusUpdateRequestDto;
import com.jeonjueats.entity.OrderStatus;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.OwnerOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "사장님 주문 관리", description = "사장님이 자신의 가게 주문을 조회하고 상태를 관리할 수 있는 API")
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
    @Operation(summary = "가게 주문 목록 조회", description = "사장님이 자신의 가게에 들어온 주문 목록을 조회합니다. 주문 상태별 필터링과 페이징을 지원합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "주문 목록 조회 성공 예시", value = """
                {
                  "content": [
                    {
                      "orderId": 501,
                      "orderNumber": "ORDER-20250623-001",
                      "customerName": "김고객",
                      "orderStatus": "PENDING",
                      "totalAmount": 23500,
                      "orderedAt": "2025-06-23T20:15:00.000Z",
                      "orderItemCount": 3,
                      "firstMenuName": "전주비빔밥",
                      "deliveryAddress": "전북 전주시 완산구 동완산로 62"
                    },
                    {
                      "orderId": 500,
                      "orderNumber": "ORDER-20250623-002",
                      "customerName": "이손님",
                      "orderStatus": "ACCEPTED",
                      "totalAmount": 15000,
                      "orderedAt": "2025-06-23T19:45:00.000Z",
                      "orderItemCount": 2,
                      "firstMenuName": "콩나물국밥",
                      "deliveryAddress": "전북 전주시 덕진구 기린대로 147"
                    }
                  ],
                  "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10,
                    "sort": {
                      "sorted": true,
                      "direction": "DESC",
                      "orderBy": "createdAt"
                    }
                  },
                  "totalPages": 1,
                  "totalElements": 2,
                  "first": true,
                  "last": true,
                  "empty": false
                }
                """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "잘못된 주문 상태", value = """
                {
                  "timestamp": "2025-06-23T20:15:00.000Z",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "유효하지 않은 주문 상태입니다",
                  "path": "/api/owner/stores/15/orders",
                  "errorCode": "INVALID_INPUT_VALUE"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T20:15:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/orders",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 가게", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T20:15:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 가게에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/orders",
                  "errorCode": "FORBIDDEN_ACCESS_STORE"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 없음", value = """
                {
                  "timestamp": "2025-06-23T20:15:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 가게를 찾을 수 없습니다",
                  "path": "/api/owner/stores/999/orders",
                  "errorCode": "STORE_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Page<OwnerOrderListResponseDto>> getStoreOrders(
            @Parameter(description = "조회할 가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Parameter(description = "주문 상태 필터링 (PENDING, ACCEPTED, DELIVERING, COMPLETED, REJECTED)", example = "PENDING") 
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") 
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
    @Operation(summary = "주문 상세 조회", description = "사장님이 자신의 가게에 들어온 특정 주문의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "주문 상세 조회 성공 예시", value = """
                {
                  "orderId": 501,
                  "orderNumber": "ORDER-20250623-001",
                  "status": "PENDING",
                  "totalAmount": 23500,
                  "orderedAt": "2025-06-23T20:15:00.000Z",
                  "storeInfo": {
                    "storeId": 15,
                    "storeName": "전주맛집 한옥마을점"
                  },
                  "customerInfo": {
                    "customerName": "김고객",
                    "customerPhone": "010-1234-5678"
                  },
                  "deliveryInfo": {
                    "zipcode": "55041",
                    "address1": "전북 전주시 완산구 동완산로 62",
                    "address2": "101동 1001호",
                    "requests": "문 앞에 놓아주세요"
                  },
                  "paymentInfo": {
                    "paymentMethod": "카드결제",
                    "subtotalAmount": 21000,
                    "deliveryFee": 2500,
                    "totalAmount": 23500
                  },
                  "orderItems": [
                    {
                      "menuName": "전주비빔밥",
                      "quantity": 2,
                      "unitPrice": 12000,
                      "totalPrice": 24000
                    },
                    {
                      "menuName": "콩나물국",
                      "quantity": 1,
                      "unitPrice": 3000,
                      "totalPrice": 3000
                    }
                  ]
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T20:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/orders/501",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 주문", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "주문 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T20:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 주문에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/orders/501",
                  "errorCode": "FORBIDDEN_ACCESS_ORDER"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "주문 없음", value = """
                {
                  "timestamp": "2025-06-23T20:30:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 주문을 찾을 수 없습니다",
                  "path": "/api/owner/stores/15/orders/999",
                  "errorCode": "ORDER_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<OrderResponseDto> getStoreOrderDetail(
            @Parameter(description = "가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Parameter(description = "조회할 주문 ID", example = "501", required = true) 
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
    @Operation(summary = "주문 상태 변경", description = "사장님이 자신의 가게 주문의 상태를 변경합니다. (예: PENDING → ACCEPTED, ACCEPTED → DELIVERING 등)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주문 상태 변경 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "주문 상태 변경 성공 예시", value = """
                {
                  "orderId": 501,
                  "orderNumber": "ORDER-20250623-001",
                  "status": "ACCEPTED",
                  "totalAmount": 23500,
                  "orderedAt": "2025-06-23T20:15:00.000Z",
                  "storeInfo": {
                    "storeId": 15,
                    "storeName": "전주맛집 한옥마을점"
                  },
                  "customerInfo": {
                    "customerName": "김고객",
                    "customerPhone": "010-1234-5678"
                  },
                  "deliveryInfo": {
                    "zipcode": "55041",
                    "address1": "전북 전주시 완산구 동완산로 62",
                    "address2": "101동 1001호",
                    "requests": "문 앞에 놓아주세요"
                  },
                  "paymentInfo": {
                    "paymentMethod": "카드결제",
                    "subtotalAmount": 21000,
                    "deliveryFee": 2500,
                    "totalAmount": 23500
                  },
                  "orderItems": [
                    {
                      "menuName": "전주비빔밥",
                      "quantity": 2,
                      "unitPrice": 12000,
                      "totalPrice": 24000
                    }
                  ]
                }
                """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "잘못된 상태 변경", value = """
                {
                  "timestamp": "2025-06-23T21:00:00.000Z",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "유효하지 않은 주문 상태 변경입니다",
                  "path": "/api/owner/stores/15/orders/501/status",
                  "errorCode": "INVALID_INPUT_VALUE"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T21:00:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/orders/501/status",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 주문", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "주문 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T21:00:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 주문에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/orders/501/status",
                  "errorCode": "FORBIDDEN_ACCESS_ORDER"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "주문 없음", value = """
                {
                  "timestamp": "2025-06-23T21:00:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 주문을 찾을 수 없습니다",
                  "path": "/api/owner/stores/15/orders/999/status",
                  "errorCode": "ORDER_NOT_FOUND"
                }
                """))),
        @ApiResponse(responseCode = "409", description = "주문 상태 변경 불가", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "잘못된 상태 전환", value = """
                {
                  "timestamp": "2025-06-23T21:00:00.000Z",
                  "status": 409,
                  "error": "Conflict",
                  "message": "현재 상태에서는 해당 상태로 변경할 수 없습니다",
                  "path": "/api/owner/stores/15/orders/501/status",
                  "errorCode": "INVALID_ORDER_STATUS_TRANSITION"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @Parameter(description = "가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Parameter(description = "상태를 변경할 주문 ID", example = "501", required = true) 
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