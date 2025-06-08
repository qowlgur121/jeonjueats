package com.jeonjueats.controller;

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
 * 가게/메뉴 통합 검색 컨트롤러
 * 키워드로 가게명이나 메뉴명을 검색하여 가게 목록을 반환
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final StoreService storeService;

    /**
     * 가게/메뉴 통합 검색 (공개 API)
     * 키워드로 가게명 또는 메뉴명을 검색하여 해당하는 가게 목록을 반환
     * 
     * @param keyword 검색 키워드 (가게명 또는 메뉴명)
     * @param pageable 페이징 정보 (기본: 페이지 0, 사이즈 10, 최신 등록순)
     * @return 검색된 가게 목록 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> searchStores(
            @RequestParam("keyword") String keyword,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("가게/메뉴 통합 검색 요청 - 키워드: '{}', 페이지: {}", keyword, pageable.getPageNumber());
        
        // 키워드가 비어있는 경우 빈 결과 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("검색 키워드가 비어있습니다.");
            return ResponseEntity.badRequest().build();
        }
        
        Page<StoreResponseDto> searchResults = storeService.searchStores(keyword.trim(), pageable);
        
        log.info("가게/메뉴 통합 검색 완료 - 키워드: '{}', 결과: {}개 가게", 
                keyword, searchResults.getTotalElements());
        
        return ResponseEntity.ok(searchResults);
    }
} 