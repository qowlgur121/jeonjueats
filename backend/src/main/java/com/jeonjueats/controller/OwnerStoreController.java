package com.jeonjueats.controller;

import com.jeonjueats.dto.StoreCreateRequestDto;
import com.jeonjueats.dto.StoreResponseDto;
import com.jeonjueats.dto.StoreUpdateRequestDto;
import com.jeonjueats.service.OwnerStoreService;
import com.jeonjueats.security.JwtUtil;
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
     * 현재 로그인한 사장님이 소유한 가게 목록을 페이징 처리하여 조회합니다.
     * 
     * @param pageable 페이징 정보 (기본: 페이지 0, 사이즈 10, createdAt 내림차순)
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 페이징된 가게 목록
     */
    @GetMapping("/stores")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<Page<StoreResponseDto>> getMyStores(
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
     * 현재 로그인한 사장님이 소유한 특정 가게의 상세 정보를 조회합니다.
     * 
     * @param storeId 조회할 가게 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 가게 상세 정보
     */
    @GetMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<StoreResponseDto> getMyStore(
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
     * 현재 로그인한 사장님이 소유한 가게의 정보를 수정합니다.
     * 요청 본문에 포함된 필드만 업데이트됩니다.
     * 
     * @param storeId 수정할 가게 ID
     * @param requestDto 수정할 가게 정보 (선택적 필드들)
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 수정된 가게 정보
     */
    @PutMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<StoreResponseDto> updateStore(
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
     * 현재 로그인한 사장님이 소유한 가게의 운영 상태를 OPEN ↔ CLOSED로 변경합니다.
     * 
     * @param storeId 상태를 변경할 가게 ID
     * @param request HTTP 요청 (JWT 토큰 추출용)
     * @return 변경된 가게 정보 (새로운 운영 상태 포함)
     */
    @PostMapping("/stores/{storeId}/toggle-operation")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public ResponseEntity<StoreResponseDto> toggleStoreOperationStatus(
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
     * JWT 토큰에서 현재 사용자 ID 추출
     * Authorization 헤더의 Bearer 토큰을 파싱하여 사용자 ID를 반환합니다.
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