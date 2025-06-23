package com.jeonjueats.controller;

import com.jeonjueats.dto.CartItemAddRequestDto;
import com.jeonjueats.dto.CartItemRequestDto;
import com.jeonjueats.dto.CartItemResponseDto;
import com.jeonjueats.dto.CartItemUpdateRequestDto;
import com.jeonjueats.dto.CartResponseDto;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.CartService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "장바구니 API", description = "일반 사용자의 장바구니 관리 API (JWT 인증 필요)")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    @Operation(
        summary = "장바구니 메뉴 추가/수량 변경",
        description = "장바구니에 메뉴를 추가하거나 기존 메뉴의 수량을 변경합니다. 한 번에 한 가게의 메뉴만 담을 수 있습니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "메뉴 추가 또는 수량 변경 정보",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartItemRequestDto.class),
                examples = {
                    @ExampleObject(
                        name = "메뉴 추가",
                        value = """
                            {
                              "storeId": 1,
                              "menuId": 5,
                              "quantity": 2
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "수량 변경",
                        value = """
                            {
                              "cartItemId": 10,
                              "quantity": 3
                            }
                            """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "장바구니 처리 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartItemResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                        {
                          "cartItemId": 10,
                          "menuId": 5,
                          "menuName": "김치찌개",
                          "menuPrice": 8000,
                          "quantity": 2,
                          "totalPrice": 16000,
                          "storeId": 1,
                          "storeName": "김치찌개 전문점"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (품절 메뉴, 다른 가게 메뉴 등)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "다른 가게 메뉴",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "다른 가게의 메뉴는 함께 주문할 수 없습니다.",
                          "path": "/api/carts/items"
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
                          "path": "/api/carts/items"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "메뉴를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "메뉴 없음",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 404,
                          "error": "Not Found",
                          "message": "메뉴를 찾을 수 없습니다.",
                          "path": "/api/carts/items"
                        }
                        """
                )
            )
        )
    })
    @PutMapping("/items")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CartItemResponseDto> processCartItem(
            @Parameter(description = "메뉴 추가 또는 수량 변경 정보", required = true)
            @Valid @RequestBody CartItemRequestDto requestDto,
            @Parameter(hidden = true) HttpServletRequest request) {
        
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

    @Operation(
        summary = "내 장바구니 조회",
        description = "현재 사용자의 장바구니 내용과 주문 요약 정보를 조회합니다. 비어있을 경우 isEmpty: true로 응답됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "장바구니 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "장바구니 있음",
                        value = """
                            {
                              "cartId": 101,
                              "storeId": 1,
                              "storeName": "김치찌개 전문점",
                              "cartItems": [
                                {
                                  "cartItemId": 5,
                                  "menuId": 1,
                                  "menuName": "김치찌개",
                                  "menuPrice": 8000,
                                  "quantity": 2,
                                  "totalPrice": 16000
                                }
                              ],
                              "subtotalAmount": 16000,
                              "deliveryFee": 2000,
                              "finalPrice": 18000,
                              "totalItemCount": 1,
                              "isEmpty": false
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "빈 장바구니",
                        value = """
                            {
                              "cartId": null,
                              "storeId": null,
                              "storeName": null,
                              "cartItems": [],
                              "subtotalAmount": 0,
                              "deliveryFee": 0,
                              "finalPrice": 0,
                              "totalItemCount": 0,
                              "isEmpty": true
                            }
                            """
                    )
                }
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
                          "path": "/api/carts"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CartResponseDto> getCart(
            @Parameter(hidden = true) HttpServletRequest request) {
        
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

    @Operation(
        summary = "장바구니 특정 메뉴 삭제",
        description = "장바구니에서 특정 아이템을 삭제합니다. 본인의 장바구니 아이템만 삭제할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "장바구니 아이템 삭제 성공"
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
                          "path": "/api/carts/items/5"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음 (다른 사용자의 장바구니 아이템)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 403,
                          "error": "Forbidden",
                          "message": "해당 장바구니 아이템에 접근할 권한이 없습니다.",
                          "path": "/api/carts/items/5"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "장바구니 아이템을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "아이템 없음",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 404,
                          "error": "Not Found",
                          "message": "장바구니 아이템을 찾을 수 없습니다.",
                          "path": "/api/carts/items/5"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteCartItem(
            @Parameter(description = "삭제할 장바구니 아이템 ID", required = true, example = "5")
            @PathVariable Long cartItemId,
            @Parameter(hidden = true) HttpServletRequest request) {
        
        log.info("장바구니 아이템 삭제 요청 - 아이템 ID: {}", cartItemId);
        
        Long userId = getCurrentUserId(request);
        cartService.deleteCartItem(userId, cartItemId);
        
        log.info("장바구니 아이템 삭제 완료 - 사용자 ID: {}, 아이템 ID: {}", userId, cartItemId);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "장바구니 전체 비우기",
        description = "사용자의 장바구니를 완전히 비웁니다. 모든 아이템이 삭제됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "장바구니 비우기 성공"
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
                          "path": "/api/carts"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> clearCart(
            @Parameter(hidden = true) HttpServletRequest request) {
        
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