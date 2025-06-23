package com.jeonjueats.controller;

import com.jeonjueats.dto.StoreResponseDto;
import com.jeonjueats.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "검색 API", description = "가게 및 메뉴 통합 검색 API (인증 불필요)")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final StoreService storeService;

    @Operation(
        summary = "가게 또는 메뉴 검색",
        description = "키워드로 가게명 또는 메뉴명을 검색하여 일치하는 가게 목록을 반환합니다. 메뉴명으로 검색 시 해당 메뉴를 판매하는 가게가 반환됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "검색 결과",
                    value = """
                        {
                          "content": [
                            {
                              "storeId": 77,
                              "storeName": "청춘휴게소",
                              "storeImageUrl": "/images/stores/rest_stop.jpg",
                              "minOrderAmount": 15000,
                              "deliveryFee": 2500,
                              "status": "OPEN",
                              "averageRating": 4.2,
                              "reviewCount": 34,
                              "searchedMenuInfo": {
                                "menuName": "닭갈비 덮밥",
                                "price": 8000
                              }
                            },
                            {
                              "storeId": 33,
                              "storeName": "전주 닭갈비 맛집",
                              "storeImageUrl": "/images/stores/chicken_galbi.jpg",
                              "minOrderAmount": 12000,
                              "deliveryFee": 2000,
                              "status": "OPEN",
                              "averageRating": 4.7,
                              "reviewCount": 89
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 10,
                            "sort": {
                              "sorted": true,
                              "orders": [{"property": "createdAt", "direction": "DESC"}]
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
            responseCode = "400",
            description = "잘못된 요청 (빈 키워드)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "빈 키워드",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "검색 키워드가 필요합니다.",
                          "path": "/api/search"
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> searchStores(
            @Parameter(description = "검색 키워드 (가게명 또는 메뉴명)", required = true, example = "닭갈비")
            @RequestParam("keyword") String keyword,
            @Parameter(description = "페이징 정보 (page, size, sort 파라미터 사용)")
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