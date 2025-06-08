package com.jeonjueats.controller;

import com.jeonjueats.dto.MenuCreateRequestDto;
import com.jeonjueats.dto.MenuUpdateRequestDto;
import com.jeonjueats.entity.Menu;
import com.jeonjueats.service.OwnerMenuService;
import com.jeonjueats.security.JwtUtil;
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
    @PostMapping("/stores/{storeId}/menus")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Menu> createMenu(
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
    @GetMapping("/stores/{storeId}/menus")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Page<Menu>> getMenus(
            @PathVariable Long storeId,
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
    @PutMapping("/stores/{storeId}/menus/{menuId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Menu> updateMenu(
            @PathVariable Long storeId,
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
    @DeleteMapping("/stores/{storeId}/menus/{menuId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<String> deleteMenu(
            @PathVariable Long storeId,
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
    @PostMapping("/stores/{storeId}/menus/{menuId}/toggle-availability")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Menu> toggleMenuAvailability(
            @PathVariable Long storeId,
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