package com.jeonjueats.controller;

import com.jeonjueats.dto.WishlistResponseDto;
import com.jeonjueats.dto.WishlistToggleResponseDto;
import com.jeonjueats.security.JwtUtil;
import com.jeonjueats.service.WishlistService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "찜하기 API", description = "가게 찜하기/해제 및 찜 목록 조회 API (JWT 인증 필요)")
@RestController
@RequestMapping("/api/wishes/stores")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;
    private final JwtUtil jwtUtil;

    @Operation(
        summary = "가게 찜 상태 변경 (토글)",
        description = "특정 가게의 찜 상태를 반전시킵니다. 찜하지 않은 가게는 찜하기, 찜한 가게는 찜 해제됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "찜 상태 변경 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WishlistToggleResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "찜하기 성공",
                        value = """
                            {
                              "storeId": 123,
                              "isWished": true
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "찜 해제 성공",
                        value = """
                            {
                              "storeId": 123,
                              "isWished": false
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
                          "path": "/api/wishes/stores/123/toggle"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "가게를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "가게 없음",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 404,
                          "error": "Not Found",
                          "message": "가게를 찾을 수 없습니다.",
                          "path": "/api/wishes/stores/123/toggle"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{storeId}/toggle")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WishlistToggleResponseDto> toggleWishlist(
            @Parameter(description = "찜할 가게 ID", required = true, example = "123")
            @PathVariable Long storeId,
            @Parameter(hidden = true) HttpServletRequest request) {

        log.info("찜 토글 API 호출 - 가게 ID: {}", storeId);

        // JWT에서 사용자 ID 추출
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 찜 상태 토글
        WishlistToggleResponseDto response = wishlistService.toggleWishlist(userId, storeId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
        summary = "내 찜 목록 조회",
        description = "현재 사용자가 찜한 모든 가게 목록을 페이징으로 조회합니다. 가게 정보와 함께 찜 상태가 포함됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "찜 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "찜 목록",
                    value = """
                        {
                          "content": [
                            {
                              "storeId": 123,
                              "storeName": "김치찌개 전문점",
                              "storeImageUrl": "/images/stores/kimchi_jjigae.jpg",
                              "minOrderAmount": 15000,
                              "deliveryFee": 2000,
                              "averageRating": 4.5,
                              "reviewCount": 128,
                              "isWished": true
                            },
                            {
                              "storeId": 45,
                              "storeName": "전주 비빔밥 맛집",
                              "storeImageUrl": "/images/stores/bibimbap.jpg",
                              "minOrderAmount": 12000,
                              "deliveryFee": 2500,
                              "averageRating": 4.8,
                              "reviewCount": 89,
                              "isWished": true
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 10,
                            "sort": {
                              "sorted": false
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
                          "path": "/api/wishes/stores"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<WishlistResponseDto>> getMyWishlists(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) HttpServletRequest request) {

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

    @Operation(
        summary = "특정 가게 찜 여부 확인",
        description = "현재 사용자가 특정 가게를 찜했는지 여부를 확인합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "찜 여부 확인 성공",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "찜한 가게",
                        value = "true"
                    ),
                    @ExampleObject(
                        name = "찜하지 않은 가게",
                        value = "false"
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
                          "path": "/api/wishes/stores/123/status"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "가게를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "가게 없음",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 404,
                          "error": "Not Found",
                          "message": "가게를 찾을 수 없습니다.",
                          "path": "/api/wishes/stores/123/status"
                        }
                        """
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{storeId}/status")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Boolean> getWishlistStatus(
            @Parameter(description = "확인할 가게 ID", required = true, example = "123")
            @PathVariable Long storeId,
            @Parameter(hidden = true) HttpServletRequest request) {

        log.info("찜 상태 확인 API 호출 - 가게 ID: {}", storeId);

        // JWT에서 사용자 ID 추출
        String token = jwtUtil.resolveToken(request);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 찜 여부 확인
        boolean isWished = wishlistService.isWished(userId, storeId);

        return ResponseEntity.ok(isWished);
    }
} 