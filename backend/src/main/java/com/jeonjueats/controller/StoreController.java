package com.jeonjueats.controller;

import com.jeonjueats.dto.StoreDetailResponseDto;
import com.jeonjueats.dto.StoreResponseDto;
import com.jeonjueats.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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

@Tag(name = "가게 조회 API", description = "일반 사용자가 가게 정보를 조회하는 API (인증 불필요)")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;

    @Operation(
        summary = "가게 목록 조회",
        description = "영업중인 가게 목록을 조회합니다. 카테고리별 필터링과 페이징을 지원합니다.",
        parameters = {
            @Parameter(
                name = "categoryId",
                description = "카테고리 ID (1:한식, 2:중식, 3:일식, 4:양식, 5:치킨, 6:피자, 7:족발보쌈, 8:야식, 9:분식, 10:카페)",
                example = "1",
                required = false
            ),
            @Parameter(
                name = "page", 
                description = "페이지 번호 (0부터 시작)",
                example = "0"
            ),
            @Parameter(
                name = "size",
                description = "한 페이지당 조회할 가게 수",
                example = "10"
            ),
            @Parameter(
                name = "sort",
                description = "정렬 기준 (createdAt,desc 또는 name,asc 등)",
                example = "createdAt,desc"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "가게 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                        {
                          "content": [
                            {
                              "id": 1,
                              "name": "김치찌개 전문점",
                              "description": "정통 김치찌개를 맛볼 수 있는 곳",
                              "address": "전주시 완산구 효자동",
                              "phone": "063-1234-5678",
                              "imageUrl": "/api/images/korean1.jpg",
                              "categoryName": "한식",
                              "categoryId": 1,
                              "minOrderAmount": 15000,
                              "operatingHours": "09:00-22:00",
                              "status": "OPEN"
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 10,
                            "sort": {
                              "sorted": true,
                              "orders": [
                                {
                                  "property": "createdAt",
                                  "direction": "DESC"
                                }
                              ]
                            }
                          },
                          "last": false,
                          "totalPages": 3,
                          "totalElements": 25,
                          "size": 10,
                          "number": 0,
                          "sort": {
                            "sorted": true
                          },
                          "first": true,
                          "numberOfElements": 10,
                          "empty": false
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> getStores(
            @Parameter(hidden = true) @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(hidden = true) @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        log.info("가게 목록 조회 요청 - 카테고리 ID: {}, 페이지: {}", categoryId, pageable.getPageNumber());
        
        Page<StoreResponseDto> stores = storeService.getOpenStores(categoryId, pageable);
        
        log.info("가게 목록 조회 완료 - 총 {}개 가게 (카테고리: {})", 
                stores.getTotalElements(), categoryId != null ? categoryId : "전체");
        
        return ResponseEntity.ok(stores);
    }

    @Operation(
        summary = "가게 상세 정보 조회",
        description = "특정 가게의 상세 정보와 해당 가게의 모든 메뉴 목록을 조회합니다.",
        parameters = {
            @Parameter(
                name = "storeId",
                description = "조회할 가게 ID",
                example = "1",
                required = true
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "가게 상세 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StoreDetailResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                        {
                          "id": 1,
                          "name": "김치찌개 전문점",
                          "description": "정통 김치찌개를 맛볼 수 있는 곳",
                          "address": "전주시 완산구 효자동",
                          "phone": "063-1234-5678",
                          "imageUrl": "/api/images/korean1.jpg",
                          "categoryName": "한식",
                          "categoryId": 1,
                          "minOrderAmount": 15000,
                          "deliveryFee": 2000,
                          "operatingHours": "09:00-22:00",
                          "status": "OPEN",
                          "menus": [
                            {
                              "id": 1,
                              "name": "김치찌개",
                              "description": "돼지고기와 김치로 끓인 얼큰한 찌개",
                              "price": 8000,
                              "imageUrl": "/api/images/menus/korean/kimchi-jjigae.jpg",
                              "status": "AVAILABLE"
                            },
                            {
                              "id": 2,
                              "name": "된장찌개",
                              "description": "시원하고 구수한 된장찌개",
                              "price": 7000,
                              "imageUrl": "/api/images/menus/korean/doenjang-jjigae.jpg",
                              "status": "SOLD_OUT"
                            }
                          ]
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
                          "path": "/api/stores/999"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "서버 오류",
                    value = """
                        {
                          "timestamp": "2024-06-23T10:30:00Z",
                          "status": 500,
                          "error": "Internal Server Error",
                          "message": "서버 내부 오류가 발생했습니다.",
                          "path": "/api/stores/1"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponseDto> getStoreDetail(
            @Parameter(description = "조회할 가게 ID", required = true, example = "1")
            @PathVariable("storeId") Long storeId) {
        
        log.info("가게 상세 정보 조회 요청 - 가게 ID: {}", storeId);
        
        StoreDetailResponseDto storeDetail = storeService.getStoreDetail(storeId);
        
        log.info("가게 상세 정보 조회 완료 - 가게: {}, 메뉴 {}개", 
                storeDetail.getName(), storeDetail.getMenus().size());
        
        return ResponseEntity.ok(storeDetail);
    }
} 