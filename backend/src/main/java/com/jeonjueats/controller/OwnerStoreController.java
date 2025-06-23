package com.jeonjueats.controller;

import com.jeonjueats.dto.StoreCreateRequestDto;
import com.jeonjueats.dto.StoreResponseDto;
import com.jeonjueats.dto.StoreUpdateRequestDto;
import com.jeonjueats.service.OwnerStoreService;
import com.jeonjueats.security.JwtUtil;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 사장님용 가게 관리 컨트롤러
 * 가게 등록, 조회, 수정, 운영 상태 관리 등의 기능을 제공
 */
@Tag(name = "사장님 가게 관리", description = "사장님이 자신의 가게를 등록, 조회, 수정, 운영 상태 관리할 수 있는 API")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerStoreController {

    private final OwnerStoreService ownerStoreService;
    private final JwtUtil jwtUtil;

    /**
     * 새 가게 등록
     * 사장님이 새로운 가게를 등록합니다.
     * 
     * @param requestDto 가게 등록 정보
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 등록된 가게 정보
     */
    @Operation(summary = "내 가게 등록", description = "사장님이 새로운 가게를 등록합니다. 등록 시 초기 상태는 'CLOSED'로 설정됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "가게 등록 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 등록 성공 예시", value = """
                {
                  "storeId": 15,
                  "name": "전주맛집 한옥마을점",
                  "description": "전주 한옥마을 근처의 전통 맛집입니다",
                  "zipcode": "55041",
                  "address1": "전북 전주시 완산구 전주객사3길 22",
                  "address2": "1층",
                  "minOrderAmount": 15000,
                  "deliveryFee": 3000,
                  "imageUrl": "/images/stores/abcdef12-3456-7890-abcd-ef1234567890.png",
                  "category": {
                    "categoryId": 6,
                    "categoryName": "한식"
                  },
                  "status": "CLOSED",
                  "operatingHours": "{\\"mon\\":\\"09:00-21:00\\",\\"tue\\":\\"09:00-21:00\\",\\"wed\\":\\"09:00-21:00\\",\\"thu\\":\\"09:00-21:00\\",\\"fri\\":\\"09:00-21:00\\",\\"sat\\":\\"10:00-22:00\\",\\"sun\\":\\"10:00-20:00\\"}",
                  "createdAt": "2025-06-23T10:30:00.000Z",
                  "updatedAt": "2025-06-23T10:30:00.000Z"
                }
                """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "유효성 검사 실패", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "가게명은 필수입니다",
                  "path": "/api/owner/stores",
                  "errorCode": "INVALID_INPUT_VALUE"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "토큰 만료", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "JWT 토큰이 만료되었습니다",
                  "path": "/api/owner/stores",
                  "errorCode": "TOKEN_EXPIRED"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (사장님 권한 필요)", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "권한 부족", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "사장님 권한이 필요합니다",
                  "path": "/api/owner/stores",
                  "errorCode": "ACCESS_DENIED"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/stores")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<StoreResponseDto> createStore(
            @Valid @RequestBody StoreCreateRequestDto requestDto,
            HttpServletRequest request) {
        
        log.info("가게 등록 요청: {}", requestDto.getName());
        
        Long ownerId = getCurrentUserId(request);
        StoreResponseDto response = ownerStoreService.createStore(ownerId, requestDto);
        
        log.info("가게 등록 완료 - ID: {}, 이름: {}", response.getStoreId(), response.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 가게 목록 조회
     * 현재 로그인한 사장님이 소유한 가게 목록을 페이징 처리하여 조회
     * 
     * @param pageable 페이징 정보 (기본: 페이지 0, 사이즈 10, createdAt 내림차순)
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 페이징된 가게 목록
     */
    @Operation(summary = "내 가게 목록 조회", description = "현재 로그인한 사장님이 소유한 가게 목록을 페이징 처리하여 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "가게 목록 조회 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 목록 조회 성공 예시", value = """
                {
                  "content": [
                    {
                      "storeId": 15,
                      "name": "전주맛집 한옥마을점",
                      "status": "OPEN",
                      "category": {
                        "categoryId": 6,
                        "categoryName": "한식"
                      },
                      "imageUrl": "/images/stores/store15.jpg",
                      "address1": "전북 전주시 완산구 전주객사3길 22",
                      "minOrderAmount": 15000,
                      "deliveryFee": 3000,
                      "createdAt": "2025-06-23T10:30:00.000Z"
                    },
                    {
                      "storeId": 12,
                      "name": "전주비빔밥 맛집",
                      "status": "CLOSED",
                      "category": {
                        "categoryId": 6,
                        "categoryName": "한식"
                      },
                      "imageUrl": "/images/stores/store12.jpg",
                      "address1": "전북 전주시 덕진구 기린대로 147",
                      "minOrderAmount": 12000,
                      "deliveryFee": 2500,
                      "createdAt": "2025-06-22T15:20:00.000Z"
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
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 (사장님 권한 필요)", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "권한 부족", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "사장님 권한이 필요합니다",
                  "path": "/api/owner/stores",
                  "errorCode": "ACCESS_DENIED"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/stores")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Page<StoreResponseDto>> getMyStores(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") 
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable,
            HttpServletRequest request) {
        
        Long ownerId = getCurrentUserId(request);
        log.info("내 가게 목록 조회 요청 - 사장님 ID: {}, 페이지: {}", ownerId, pageable.getPageNumber());
        
        Page<StoreResponseDto> stores = ownerStoreService.getMyStores(ownerId, pageable);
        
        log.info("내 가게 목록 조회 완료 - 총 {}개 가게", stores.getTotalElements());
        return ResponseEntity.ok(stores);
    }

    /**
     * 내 가게 상세 정보 조회
     * 현재 로그인한 사장님이 소유한 특정 가게의 상세 정보를 조회
     * 
     * @param storeId 조회할 가게 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 가게 상세 정보
     */
    @Operation(summary = "내 가게 상세 조회", description = "현재 로그인한 사장님이 소유한 특정 가게의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "가게 상세 조회 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 상세 조회 성공 예시", value = """
                {
                  "storeId": 15,
                  "name": "전주맛집 한옥마을점",
                  "description": "전주 한옥마을 근처의 전통 맛집입니다",
                  "zipcode": "55041",
                  "address1": "전북 전주시 완산구 전주객사3길 22",
                  "address2": "1층",
                  "minOrderAmount": 15000,
                  "deliveryFee": 3000,
                  "imageUrl": "/images/stores/store15.jpg",
                  "category": {
                    "categoryId": 6,
                    "categoryName": "한식"
                  },
                  "status": "OPEN",
                  "operatingHours": "{\\"mon\\":\\"09:00-21:00\\",\\"tue\\":\\"09:00-21:00\\",\\"wed\\":\\"09:00-21:00\\",\\"thu\\":\\"09:00-21:00\\",\\"fri\\":\\"09:00-21:00\\",\\"sat\\":\\"10:00-22:00\\",\\"sun\\":\\"10:00-20:00\\"}",
                  "createdAt": "2025-06-23T10:30:00.000Z",
                  "updatedAt": "2025-06-23T12:15:00.000Z"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 가게", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 가게에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15",
                  "errorCode": "FORBIDDEN_ACCESS_STORE"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 없음", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 가게를 찾을 수 없습니다",
                  "path": "/api/owner/stores/999",
                  "errorCode": "STORE_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<StoreResponseDto> getMyStore(
            @Parameter(description = "조회할 가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            HttpServletRequest request) {
        
        Long ownerId = getCurrentUserId(request);
        log.info("내 가게 상세 조회 요청 - 사장님 ID: {}, 가게 ID: {}", ownerId, storeId);
        
        StoreResponseDto store = ownerStoreService.getStoreDetail(storeId, ownerId);
        
        log.info("내 가게 상세 조회 완료 - 가게명: {}", store.getName());
        return ResponseEntity.ok(store);
    }

    /**
     * 내 가게 정보 수정
     * 현재 로그인한 사장님이 소유한 가게의 정보를 수정
     * 요청 본문에 포함된 필드만 업데이트됩니다.
     * 
     * @param storeId 수정할 가게 ID
     * @param requestDto 수정할 가게 정보 (선택적 필드들)
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 수정된 가게 정보
     */
    @Operation(summary = "내 가게 정보 수정", description = "현재 로그인한 사장님이 소유한 가게의 정보를 수정합니다. 요청 본문에 포함된 필드만 업데이트됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "가게 정보 수정 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 정보 수정 성공 예시", value = """
                {
                  "storeId": 15,
                  "name": "전주맛집 한옥마을점 (리뉴얼)",
                  "description": "전주 한옥마을 근처의 전통 맛집입니다. 신선한 재료로 만든 건강한 음식을 제공합니다.",
                  "zipcode": "55041",
                  "address1": "전북 전주시 완산구 전주객사3길 22",
                  "address2": "1층",
                  "minOrderAmount": 18000,
                  "deliveryFee": 3500,
                  "imageUrl": "/images/stores/store15_new.jpg",
                  "category": {
                    "categoryId": 6,
                    "categoryName": "한식"
                  },
                  "status": "OPEN",
                  "operatingHours": "{\\"mon\\":\\"09:00-21:00\\",\\"tue\\":\\"09:00-21:00\\",\\"wed\\":\\"09:00-21:00\\",\\"thu\\":\\"09:00-21:00\\",\\"fri\\":\\"09:00-21:00\\",\\"sat\\":\\"10:00-22:00\\",\\"sun\\":\\"10:00-20:00\\"}",
                  "createdAt": "2025-06-23T10:30:00.000Z",
                  "updatedAt": "2025-06-23T14:45:00.000Z"
                }
                """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "유효성 검사 실패", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "최소 주문 금액은 0원 이상이어야 합니다",
                  "path": "/api/owner/stores/15",
                  "errorCode": "INVALID_INPUT_VALUE"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 가게", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 가게에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15",
                  "errorCode": "FORBIDDEN_ACCESS_STORE"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 없음", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 가게를 찾을 수 없습니다",
                  "path": "/api/owner/stores/999",
                  "errorCode": "STORE_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<StoreResponseDto> updateStore(
            @Parameter(description = "수정할 가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequestDto requestDto,
            HttpServletRequest request) {
        
        Long ownerId = getCurrentUserId(request);
        log.info("가게 정보 수정 요청 - 사장님 ID: {}, 가게 ID: {}", ownerId, storeId);
        
        StoreResponseDto updatedStore = ownerStoreService.updateStore(ownerId, storeId, requestDto);
        
        log.info("가게 정보 수정 완료 - 가게명: {}", updatedStore.getName());
        return ResponseEntity.ok(updatedStore);
    }

    /**
     * 내 가게 운영 상태 변경 (토글)
     * 현재 로그인한 사장님이 소유한 가게의 운영 상태를 OPEN ↔ CLOSED로 변경
     * 
     * @param storeId 상태를 변경할 가게 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 변경된 가게 정보 (새로운 운영 상태 포함)
     */
    @Operation(summary = "내 가게 운영 상태 변경", description = "현재 로그인한 사장님이 소유한 가게의 운영 상태를 OPEN ↔ CLOSED로 토글합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "운영 상태 변경 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "운영 상태 변경 성공 예시", value = """
                {
                  "storeId": 15,
                  "name": "전주맛집 한옥마을점",
                  "description": "전주 한옥마을 근처의 전통 맛집입니다",
                  "zipcode": "55041",
                  "address1": "전북 전주시 완산구 전주객사3길 22",
                  "address2": "1층",
                  "minOrderAmount": 15000,
                  "deliveryFee": 3000,
                  "imageUrl": "/images/stores/store15.jpg",
                  "category": {
                    "categoryId": 6,
                    "categoryName": "한식"
                  },
                  "status": "OPEN",
                  "operatingHours": "{\\"mon\\":\\"09:00-21:00\\",\\"tue\\":\\"09:00-21:00\\",\\"wed\\":\\"09:00-21:00\\",\\"thu\\":\\"09:00-21:00\\",\\"fri\\":\\"09:00-21:00\\",\\"sat\\":\\"10:00-22:00\\",\\"sun\\":\\"10:00-20:00\\"}",
                  "createdAt": "2025-06-23T10:30:00.000Z",
                  "updatedAt": "2025-06-23T15:20:00.000Z"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/toggle-operation",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 가게", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 가게에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/toggle-operation",
                  "errorCode": "FORBIDDEN_ACCESS_STORE"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 없음", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 가게를 찾을 수 없습니다",
                  "path": "/api/owner/stores/999/toggle-operation",
                  "errorCode": "STORE_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/stores/{storeId}/toggle-operation")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<StoreResponseDto> toggleStoreOperationStatus(
            @Parameter(description = "운영 상태를 변경할 가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            HttpServletRequest request) {
        
        Long ownerId = getCurrentUserId(request);
        log.info("가게 운영 상태 변경 요청 - 사장님 ID: {}, 가게 ID: {}", ownerId, storeId);
        
        StoreResponseDto updatedStore = ownerStoreService.toggleStoreOperationStatus(ownerId, storeId);
        
        log.info("가게 운영 상태 변경 완료 - 가게명: {}, 새 상태: {}", 
                updatedStore.getName(), updatedStore.getStatus());
        return ResponseEntity.ok(updatedStore);
    }

    /**
     * 내 가게 삭제 (논리적 삭제)
     * 현재 로그인한 사장님이 소유한 가게를 논리적으로 삭제
     * 실제 데이터는 유지되지만 서비스에서는 더 이상 노출되지 않음
     * 
     * @param storeId 삭제할 가게 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 204 No Content (삭제 성공)
     */
    @Operation(summary = "내 가게 삭제", description = "현재 로그인한 사장님이 소유한 가게를 논리적으로 삭제합니다. 실제 데이터는 보존되지만 서비스에서 더 이상 노출되지 않습니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "가게 삭제 성공 (No Content)",
            content = @Content()
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 가게", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 가게에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15",
                  "errorCode": "FORBIDDEN_ACCESS_STORE"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 없음", value = """
                {
                  "timestamp": "2025-06-23T10:30:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 가게를 찾을 수 없습니다",
                  "path": "/api/owner/stores/999",
                  "errorCode": "STORE_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Void> deleteStore(
            @Parameter(description = "삭제할 가게 ID", example = "15", required = true) 
            @PathVariable Long storeId, 
            HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        log.info("가게 삭제 요청 - 사장님 ID: {}, 가게 ID: {}", ownerId, storeId);
        
        ownerStoreService.deleteStore(ownerId, storeId);
        
        log.info("가게 삭제 완료 - 가게 ID: {}", storeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * JWT 토큰에서 현재 사용자 ID 추출
     * Authorization 헤더의 Bearer 토큰을 파싱하여 사용자 ID를 반환
     * 
     * @param request HTTP 요청
     * @return 현재 로그인한 사용자의 ID
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