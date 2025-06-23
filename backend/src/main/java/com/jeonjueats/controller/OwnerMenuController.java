package com.jeonjueats.controller;

import com.jeonjueats.dto.MenuCreateRequestDto;
import com.jeonjueats.dto.MenuUpdateRequestDto;
import com.jeonjueats.entity.Menu;
import com.jeonjueats.service.OwnerMenuService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 사장님용 메뉴 관리 컨트롤러
 * 메뉴 등록, 조회, 수정, 판매 상태 관리 등의 기능을 제공
 */
@Tag(name = "사장님 메뉴 관리", description = "사장님이 자신의 가게 메뉴를 등록, 조회, 수정, 판매 상태 관리할 수 있는 API")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerMenuController {

    private final OwnerMenuService ownerMenuService;
    private final JwtUtil jwtUtil;

    /**
     * 새 메뉴 등록
     * 사장님이 자신의 가게에 새로운 메뉴를 등록합니다.
     * 
     * @param storeId 메뉴를 등록할 가게 ID
     * @param requestDto 메뉴 등록 정보
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 등록된 메뉴 정보
     */
    @Operation(summary = "메뉴 등록", description = "사장님이 자신의 가게에 새로운 메뉴를 등록합니다. 등록 시 초기 상태는 'AVAILABLE'로 설정됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "메뉴 등록 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 등록 성공 예시", value = """
                {
                  "id": 205,
                  "name": "전주비빔밥",
                  "description": "전주 전통 비빔밥으로 신선한 나물과 고추장이 일품입니다",
                  "price": 12000,
                  "imageUrl": "/images/menus/menu205.jpg",
                  "status": "AVAILABLE",
                  "createdAt": "2025-06-23T16:30:00.000Z",
                  "updatedAt": "2025-06-23T16:30:00.000Z"
                }
                """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "유효성 검사 실패", value = """
                {
                  "timestamp": "2025-06-23T16:30:00.000Z",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "메뉴명은 필수입니다",
                  "path": "/api/owner/stores/15/menus",
                  "errorCode": "INVALID_INPUT_VALUE"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T16:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/menus",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 가게", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T16:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 가게에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/menus",
                  "errorCode": "FORBIDDEN_ACCESS_STORE"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 없음", value = """
                {
                  "timestamp": "2025-06-23T16:30:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 가게를 찾을 수 없습니다",
                  "path": "/api/owner/stores/999/menus",
                  "errorCode": "STORE_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/stores/{storeId}/menus")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Menu> createMenu(
            @Parameter(description = "메뉴를 등록할 가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Valid @RequestBody MenuCreateRequestDto requestDto,
            HttpServletRequest request) {
        
        log.info("메뉴 등록 요청 - 가게 ID: {}, 메뉴명: {}", storeId, requestDto.getMenuName());
        
        Long ownerId = getCurrentUserId(request);
        Menu createdMenu = ownerMenuService.createMenu(ownerId, storeId, requestDto);
        
        log.info("메뉴 등록 완료 - 메뉴 ID: {}, 메뉴명: {}", createdMenu.getId(), createdMenu.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMenu);
    }

    /**
     * 가게 메뉴 목록 조회
     * 사장님이 자신의 가게 메뉴 목록을 페이징하여 조회
     * 
     * @param storeId 조회할 가게 ID
     * @param pageable 페이징 정보
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 메뉴 목록 (페이징)
     */
    @Operation(summary = "가게 메뉴 목록 조회", description = "사장님이 자신의 가게 메뉴 목록을 페이징하여 조회합니다. 품절된 메뉴도 포함되며, 논리적으로 삭제된 메뉴는 제외됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메뉴 목록 조회 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 목록 조회 성공 예시", value = """
                {
                  "content": [
                    {
                      "id": 205,
                      "name": "전주비빔밥",
                      "description": "전주 전통 비빔밥으로 신선한 나물과 고추장이 일품입니다",
                      "price": 12000,
                      "imageUrl": "/images/menus/menu205.jpg",
                      "status": "AVAILABLE",
                      "createdAt": "2025-06-23T16:30:00.000Z",
                      "updatedAt": "2025-06-23T16:30:00.000Z"
                    },
                    {
                      "id": 206,
                      "name": "전주콩나물국밥",
                      "description": "시원하고 담백한 전주 콩나물국밥",
                      "price": 8000,
                      "imageUrl": "/images/menus/menu206.jpg",
                      "status": "SOLD_OUT",
                      "createdAt": "2025-06-23T15:45:00.000Z",
                      "updatedAt": "2025-06-23T17:20:00.000Z"
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
                  "timestamp": "2025-06-23T16:30:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/menus",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 가게", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T16:30:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 가게에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/menus",
                  "errorCode": "FORBIDDEN_ACCESS_STORE"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "가게 없음", value = """
                {
                  "timestamp": "2025-06-23T16:30:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 가게를 찾을 수 없습니다",
                  "path": "/api/owner/stores/999/menus",
                  "errorCode": "STORE_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/stores/{storeId}/menus")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Page<Menu>> getMenus(
            @Parameter(description = "조회할 가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") 
            Pageable pageable,
            HttpServletRequest request) {
        
        log.info("메뉴 목록 조회 요청 - 가게 ID: {}", storeId);
        
        Long ownerId = getCurrentUserId(request);
        Page<Menu> menus = ownerMenuService.getMenus(ownerId, storeId, pageable);
        
        log.info("메뉴 목록 조회 완료 - 가게 ID: {}, 메뉴 수: {}", storeId, menus.getContent().size());
        return ResponseEntity.ok(menus);
    }

    /**
     * 메뉴 정보 수정
     * 사장님이 자신의 가게 메뉴 정보를 수정
     * 
     * @param storeId 가게 ID
     * @param menuId 수정할 메뉴 ID
     * @param requestDto 수정할 메뉴 정보
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 수정된 메뉴 정보
     */
    @Operation(summary = "메뉴 정보 수정", description = "사장님이 자신의 가게 메뉴 정보를 수정합니다. 요청 본문에 포함된 필드만 업데이트됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메뉴 정보 수정 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 정보 수정 성공 예시", value = """
                {
                  "id": 205,
                  "name": "전주비빔밥 (프리미엄)",
                  "description": "전주 전통 비빔밥으로 신선한 나물과 고추장이 일품입니다. 더욱 푸짐해진 구성으로 업그레이드!",
                  "price": 15000,
                  "imageUrl": "/images/menus/menu205_premium.jpg",
                  "status": "AVAILABLE",
                  "createdAt": "2025-06-23T16:30:00.000Z",
                  "updatedAt": "2025-06-23T18:45:00.000Z"
                }
                """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "유효성 검사 실패", value = """
                {
                  "timestamp": "2025-06-23T18:45:00.000Z",
                  "status": 400,
                  "error": "Bad Request",
                  "message": "가격은 0원 이상이어야 합니다",
                  "path": "/api/owner/stores/15/menus/205",
                  "errorCode": "INVALID_INPUT_VALUE"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T18:45:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/menus/205",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 메뉴", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T18:45:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 메뉴에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/menus/205",
                  "errorCode": "FORBIDDEN_ACCESS_MENU"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 없음", value = """
                {
                  "timestamp": "2025-06-23T18:45:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 메뉴를 찾을 수 없습니다",
                  "path": "/api/owner/stores/15/menus/999",
                  "errorCode": "MENU_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/stores/{storeId}/menus/{menuId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Menu> updateMenu(
            @Parameter(description = "가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Parameter(description = "수정할 메뉴 ID", example = "205", required = true) 
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequestDto requestDto,
            HttpServletRequest request) {
        
        log.info("메뉴 수정 요청 - 가게 ID: {}, 메뉴 ID: {}", storeId, menuId);
        
        Long ownerId = getCurrentUserId(request);
        Menu updatedMenu = ownerMenuService.updateMenu(ownerId, storeId, menuId, requestDto);
        
        log.info("메뉴 수정 완료 - 메뉴 ID: {}, 메뉴명: {}", updatedMenu.getId(), updatedMenu.getName());
        return ResponseEntity.ok(updatedMenu);
    }

    /**
     * 메뉴 삭제 (논리적)
     * 사장님이 자신의 가게 메뉴를 논리적으로 삭제 처리
     * 
     * @param storeId 가게 ID
     * @param menuId 삭제할 메뉴 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 삭제 완료 메시지
     */
    @Operation(summary = "메뉴 삭제", description = "사장님이 자신의 가게 메뉴를 논리적으로 삭제합니다. 실제로는 데이터가 삭제되지 않고 삭제 표시만 됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메뉴 삭제 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 삭제 성공 예시", value = """
                {
                  "deletedMenuId": 205,
                  "message": "메뉴가 성공적으로 삭제되었습니다."
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T19:15:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/menus/205",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 메뉴", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T19:15:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 메뉴에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/menus/205",
                  "errorCode": "FORBIDDEN_ACCESS_MENU"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 없음", value = """
                {
                  "timestamp": "2025-06-23T19:15:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 메뉴를 찾을 수 없습니다",
                  "path": "/api/owner/stores/15/menus/999",
                  "errorCode": "MENU_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/stores/{storeId}/menus/{menuId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<String> deleteMenu(
            @Parameter(description = "가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Parameter(description = "삭제할 메뉴 ID", example = "205", required = true) 
            @PathVariable Long menuId,
            HttpServletRequest request) {
        
        log.info("메뉴 삭제 요청 - 가게 ID: {}, 메뉴 ID: {}", storeId, menuId);
        
        Long ownerId = getCurrentUserId(request);
        ownerMenuService.deleteMenu(ownerId, storeId, menuId);
        
        log.info("메뉴 삭제 완료 - 메뉴 ID: {}", menuId);
        return ResponseEntity.ok("메뉴가 성공적으로 삭제되었습니다.");
    }

    /**
     * 메뉴 판매 상태 토글
     * 사장님이 자신의 가게 메뉴 판매 상태를 AVAILABLE ↔ SOLD_OUT으로 토글
     * 
     * @param storeId 가게 ID
     * @param menuId 상태 변경할 메뉴 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 변경된 메뉴 정보
     */
    @Operation(summary = "메뉴 판매 상태 변경", description = "사장님이 자신의 가게 메뉴 판매 상태를 AVAILABLE ↔ SOLD_OUT으로 토글합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메뉴 판매 상태 변경 성공", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 판매 상태 변경 성공 예시", value = """
                {
                  "id": 205,
                  "name": "전주비빔밥",
                  "description": "전주 전통 비빔밥으로 신선한 나물과 고추장이 일품입니다",
                  "price": 12000,
                  "imageUrl": "/images/menus/menu205.jpg",
                  "status": "SOLD_OUT",
                  "createdAt": "2025-06-23T16:30:00.000Z",
                  "updatedAt": "2025-06-23T19:45:00.000Z"
                }
                """))),
        @ApiResponse(responseCode = "401", description = "인증 실패", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "인증 실패", value = """
                {
                  "timestamp": "2025-06-23T19:45:00.000Z",
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "유효하지 않은 JWT 토큰입니다",
                  "path": "/api/owner/stores/15/menus/205/toggle-availability",
                  "errorCode": "INVALID_TOKEN"
                }
                """))),
        @ApiResponse(responseCode = "403", description = "권한 없음 또는 소유하지 않은 메뉴", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 접근 권한 없음", value = """
                {
                  "timestamp": "2025-06-23T19:45:00.000Z",
                  "status": 403,
                  "error": "Forbidden",
                  "message": "해당 메뉴에 대한 접근 권한이 없습니다",
                  "path": "/api/owner/stores/15/menus/205/toggle-availability",
                  "errorCode": "FORBIDDEN_ACCESS_MENU"
                }
                """))),
        @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음", 
            content = @Content(mediaType = "application/json", 
                examples = @ExampleObject(name = "메뉴 없음", value = """
                {
                  "timestamp": "2025-06-23T19:45:00.000Z",
                  "status": 404,
                  "error": "Not Found",
                  "message": "해당 메뉴를 찾을 수 없습니다",
                  "path": "/api/owner/stores/15/menus/999/toggle-availability",
                  "errorCode": "MENU_NOT_FOUND"
                }
                """)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/stores/{storeId}/menus/{menuId}/toggle-availability")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Menu> toggleMenuAvailability(
            @Parameter(description = "가게 ID", example = "15", required = true) 
            @PathVariable Long storeId,
            @Parameter(description = "상태를 변경할 메뉴 ID", example = "205", required = true) 
            @PathVariable Long menuId,
            HttpServletRequest request) {
        
        log.info("메뉴 판매 상태 토글 요청 - 가게 ID: {}, 메뉴 ID: {}", storeId, menuId);
        
        Long ownerId = getCurrentUserId(request);
        Menu updatedMenu = ownerMenuService.toggleMenuAvailability(ownerId, storeId, menuId);
        
        log.info("메뉴 판매 상태 토글 완료 - 메뉴 ID: {}, 변경된 상태: {}", 
                updatedMenu.getId(), updatedMenu.getStatus());
        return ResponseEntity.ok(updatedMenu);
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