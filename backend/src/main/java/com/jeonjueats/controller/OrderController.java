package com.jeonjueats.controller;

import com.jeonjueats.dto.OrderCreateRequestDto;
import com.jeonjueats.dto.OrderListResponseDto;
import com.jeonjueats.dto.OrderResponseDto;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "주문 API", description = "일반 사용자의 주문 관리 API (JWT 인증 필요)")
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @Operation(
        summary = "주문 생성",
        description = "장바구니 내용을 기반으로 주문을 생성합니다. 결제 처리 후 장바구니가 비워집니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "주문 생성 정보 (배달 주소, 요청사항)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderCreateRequestDto.class),
                examples = @ExampleObject(
                    name = "주문 생성 요청",
                    value = """
                        {
                          "deliveryZipcode": "54999",
                          "deliveryAddress1": "전북 전주시 완산구 전주객사3길 22",
                          "deliveryAddress2": "201호",
                          "requests": "문 앞에 놓아주세요"
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "주문 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponseDto.class),
                examples = @ExampleObject(
                    name = "주문 생성 성공",
                    value = """
                        {
                          "orderId": 501,
                          "orderStatus": "PENDING",
                          "orderedAt": "2024-06-23T10:30:00Z",
                          "totalPrice": 18000,
                          "storeInfo": {
                            "storeId": 1,
                            "storeName": "김치찌개 전문점"
                          },
                          "deliveryInfo": {
                            "deliveryZipcode": "54999",
                            "deliveryAddress1": "전북 전주시 완산구 전주객사3길 22",
                            "deliveryAddress2": "201호",
                            "requests": "문 앞에 놓아주세요"
                          },
                          "paymentInfo": {
                            "paymentMethod": "CARD",
                            "totalAmountPaid": 18000
                          },
                          "orderItems": [
                            {
                              "menuName": "김치찌개",
                              "menuPrice": 8000,
                              "quantity": 2,
                              "totalPrice": 16000
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (빈 장바구니, 최소 주문금액 미달 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "빈 장바구니",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "장바구니가 비어있습니다.",
                          "path": "/api/orders"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "인증이 필요합니다.",
                          "path": "/api/orders"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<OrderResponseDto> createOrder(
            @Parameter(description = "주문 생성 정보", required = true)
            @Valid @RequestBody OrderCreateRequestDto requestDto,
            @Parameter(hidden = true) HttpServletRequest request) {
        
        log.info("주문 생성 요청 - 배달 주소: {}", requestDto.getDeliveryAddress1());
        
        Long userId = getCurrentUserId(request);
        OrderResponseDto orderResponse = orderService.createOrder(userId, requestDto);
        
        log.info("주문 생성 성공 - 주문 ID: {}, 사용자 ID: {}, 총 금액: {}", 
                orderResponse.getOrderId(), userId, orderResponse.getTotalPrice());

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @Operation(
        summary = "내 주문 목록 조회",
        description = "현재 사용자의 모든 주문 목록을 페이징으로 조회합니다. 최신순으로 정렬됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "주문 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "주문 목록",
                    value = """
                        {
                          "content": [
                            {
                              "orderId": 501,
                              "orderedAt": "2024-06-23T10:30:00Z",
                              "orderStatus": "COMPLETED",
                              "storeName": "김치찌개 전문점",
                              "totalPrice": 18000,
                              "menuNames": "김치찌개 외 1개"
                            },
                            {
                              "orderId": 500,
                              "orderedAt": "2024-06-22T19:15:00Z",
                              "orderStatus": "PENDING",
                              "storeName": "치킨나라",
                              "totalPrice": 25000,
                              "menuNames": "후라이드치킨"
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 10,
                            "sort": {
                              "sorted": true,
                              "orders": [{"property": "orderedAt", "direction": "DESC"}]
                            }
                          },
                          "totalPages": 1,
                          "totalElements": 2,
                          "first": true,
                          "last": true
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "인증이 필요합니다.",
                          "path": "/api/orders"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<OrderListResponseDto>> getMyOrders(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) HttpServletRequest request) {
        
        log.info("주문 목록 조회 요청 - 페이지: {}, 크기: {}", page, size);
        
        Long userId = getCurrentUserId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderListResponseDto> orders = orderService.getMyOrders(userId, pageable);
        
        log.info("주문 목록 조회 성공 - 사용자 ID: {}, 총 주문 수: {}", userId, orders.getTotalElements());

        return ResponseEntity.ok(orders);
    }

    @Operation(
        summary = "내 주문 상세 조회",
        description = "특정 주문의 상세 정보를 조회합니다. 본인의 주문만 조회할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "주문 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponseDto.class),
                examples = @ExampleObject(
                    name = "주문 상세 정보",
                    value = """
                        {
                          "orderId": 501,
                          "orderStatus": "COMPLETED",
                          "orderedAt": "2024-06-23T10:30:00Z",
                          "totalPrice": 18000,
                          "storeInfo": {
                            "storeId": 1,
                            "storeName": "김치찌개 전문점",
                            "storePhoneNumber": "02-1234-5678"
                          },
                          "deliveryInfo": {
                            "deliveryZipcode": "54999",
                            "deliveryAddress1": "전북 전주시 완산구 전주객사3길 22",
                            "deliveryAddress2": "201호",
                            "requests": "문 앞에 놓아주세요"
                          },
                          "paymentInfo": {
                            "paymentMethod": "CARD",
                            "totalAmountPaid": 18000
                          },
                          "orderItems": [
                            {
                              "menuName": "김치찌개",
                              "menuPrice": 8000,
                              "quantity": 2,
                              "totalPrice": 16000
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "인증 실패",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "인증이 필요합니다.",
                          "path": "/api/orders/501"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (다른 사용자의 주문)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 403,
                          "error": "Forbidden",
                          "message": "해당 주문에 접근할 권한이 없습니다.",
                          "path": "/api/orders/501"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "주문 없음",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 404,
                          "error": "Not Found",
                          "message": "주문을 찾을 수 없습니다.",
                          "path": "/api/orders/501"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<OrderResponseDto> getMyOrderDetail(
            @Parameter(description = "주문 ID", required = true, example = "501")
            @PathVariable Long orderId,
            @Parameter(hidden = true) HttpServletRequest request) {
        
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