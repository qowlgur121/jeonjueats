package com.jeonjueats.controller;

import com.jeonjueats.dto.StoreDetailResponseDto;
import com.jeonjueats.dto.StoreResponseDto;
import com.jeonjueats.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 일반 사용자용 가게 조회 컨트롤러
 * 인증 없이 접근 가능한 가게 목록 조회 기능을 제공
 */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;

    /**
     * 가게 목록 조회 (공개 API)
     * OPEN 상태인 가게만 조회하며, 카테고리 필터링과 페이징을 지원
     * 
     * @param categoryId 카테고리 ID (선택사항, null이면 전체 카테고리)
     * @param pageable 페이징 정보 (기본: 페이지 0, 사이즈 10, 최신 등록순)
     * @return 페이징된 가게 목록
     */
    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> getStores(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("가게 목록 조회 요청 - 카테고리 ID: {}, 페이지: {}", categoryId, pageable.getPageNumber());
        
        Page<StoreResponseDto> stores = storeService.getOpenStores(categoryId, pageable);
        
        log.info("가게 목록 조회 완료 - 총 {}개 가게 (카테고리: {})", 
                stores.getTotalElements(), categoryId != null ? categoryId : "전체");
        
        return ResponseEntity.ok(stores);
    }

    /**
     * 가게 상세 정보 및 메뉴 목록 조회 (공개 API)
     * OPEN 상태인 가게의 상세 정보와 해당 가게의 모든 메뉴를 함께 조회
     * 
     * @param storeId 조회할 가게 ID
     * @return 가게 상세 정보와 메뉴 목록
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponseDto> getStoreDetail(@PathVariable("storeId") Long storeId) {
        
        log.info("가게 상세 정보 조회 요청 - 가게 ID: {}", storeId);
        
        StoreDetailResponseDto storeDetail = storeService.getStoreDetail(storeId);
        
        log.info("가게 상세 정보 조회 완료 - 가게: {}, 메뉴 {}개", 
                storeDetail.getName(), storeDetail.getMenus().size());
        
        return ResponseEntity.ok(storeDetail);
    }
} 