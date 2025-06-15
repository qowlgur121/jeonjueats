package com.jeonjueats.service;

import com.jeonjueats.dto.MenuResponseDto;
import com.jeonjueats.dto.StoreDetailResponseDto;
import com.jeonjueats.dto.StoreResponseDto;
import com.jeonjueats.entity.Menu;
import com.jeonjueats.entity.Store;
import com.jeonjueats.entity.StoreStatus;
import com.jeonjueats.exception.StoreNotFoundException;
import com.jeonjueats.repository.MenuRepository;
import com.jeonjueats.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 일반 사용자용 가게 서비스
 * 인증 없이 접근 가능한 가게 목록 조회, 검색 기능을 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    /**
     * 모든 가게 목록 조회 (카테고리 필터링 지원)
     * 영업 상태에 관계없이 모든 가게를 반환하며, 카테고리별 필터링과 페이징을 지원
     * 
     * @param categoryId 카테고리 ID (null이면 전체 카테고리)
     * @param pageable 페이징 정보
     * @return 페이징된 가게 목록
     */
    public Page<StoreResponseDto> getOpenStores(Long categoryId, Pageable pageable) {
        log.info("가게 목록 조회 시작 - 카테고리 ID: {}, 페이지: {}, 사이즈: {}", 
                 categoryId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Store> storePage;
        
        if (categoryId != null) {
            // 특정 카테고리의 모든 가게 조회 (영업 상태 무관)
            storePage = storeRepository.findByCategoryIdAndIsDeletedFalseOrderByCreatedAtDesc(
                    categoryId, pageable);
        } else {
            // 전체 카테고리의 모든 가게 조회 (영업 상태 무관)
            storePage = storeRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);
        }

        log.info("가게 목록 조회 완료 - 총 {}개 가게 ({}페이지 중 {}페이지)", 
                 storePage.getTotalElements(), storePage.getTotalPages(), pageable.getPageNumber() + 1);

        // Store 엔티티를 StoreResponseDto로 변환
        return storePage.map(this::convertToResponseDto);
    }

    /**
     * 가게명 또는 메뉴명으로 가게 검색
     * OPEN 상태인 가게만 검색 결과에 포함
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 가게 목록 (페이징)
     */
    public Page<StoreResponseDto> searchStores(String keyword, Pageable pageable) {
        log.info("가게/메뉴 통합 검색 시작 - 키워드: '{}', 페이지: {}, 사이즈: {}", 
                 keyword, pageable.getPageNumber(), pageable.getPageSize());

        // OPEN 상태인 가게만 검색하는 메서드 사용
        // 가게명과 메뉴명을 모두 검색하여 영업 중인 가게들만 반환
        Page<Store> searchResults = storeRepository.findByStoreOrMenuNameContainingAndStatus(
                keyword, StoreStatus.OPEN, pageable);
        
        // StoreResponseDto로 변환 (OPEN 상태이고 삭제되지 않은 가게만 변환됨)
        Page<StoreResponseDto> results = searchResults.map(this::convertToResponseDto);

        log.info("가게/메뉴 통합 검색 완료 - 키워드: '{}', 총 {}개 결과 ({}페이지 중 {}페이지)", 
                 keyword, results.getTotalElements(), 
                 results.getTotalPages(), pageable.getPageNumber() + 1);

        return results;
    }

    /**
     * 가게 상세 정보 및 메뉴 목록 조회
     * 영업 상태에 관계없이 가게 상세 정보와 해당 가게의 모든 메뉴를 함께 조회
     * 
     * @param storeId 조회할 가게 ID
     * @return 가게 상세 정보와 메뉴 목록 (판매중 + 품절 포함)
     * @throws StoreNotFoundException 가게를 찾을 수 없는 경우
     */
    public StoreDetailResponseDto getStoreDetail(Long storeId) {
        log.info("가게 상세 정보 조회 시작 - 가게 ID: {}", storeId);

        // 가게 조회 (영업 상태 무관)
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("가게를 찾을 수 없습니다."));

        // 해당 가게의 모든 메뉴 조회 (판매중 + 품절 포함)
        List<Menu> menus = menuRepository.findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(storeId);

        log.info("가게 상세 정보 조회 완료 - 가게: {}, 메뉴 {}개", store.getName(), menus.size());

        // StoreDetailResponseDto로 변환
        return convertToStoreDetailResponseDto(store, menus);
    }

    /**
     * Store 엔티티를 StoreResponseDto로 변환
     * 
     * @param store Store 엔티티
     * @return StoreResponseDto
     */
    private StoreResponseDto convertToResponseDto(Store store) {
        return StoreResponseDto.builder()
                .storeId(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .zipcode(store.getZipcode())
                .address1(store.getAddress1())
                .address2(store.getAddress2())
                .phoneNumber(store.getPhoneNumber())
                .storeImageUrl(store.getStoreImageUrl())
                .categoryId(store.getCategoryId())
                .minOrderAmount(store.getMinOrderAmount())
                .deliveryFee(store.getDeliveryFee())
                .status(store.getStatus())
                .averageRating(store.getAverageRating())
                .reviewCount(store.getReviewCount())
                .operatingHours(store.getOperatingHours())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }

    /**
     * Store 엔티티와 Menu 목록을 StoreDetailResponseDto로 변환
     * 
     * @param store Store 엔티티
     * @param menus 메뉴 목록
     * @return StoreDetailResponseDto
     */
    private StoreDetailResponseDto convertToStoreDetailResponseDto(Store store, List<Menu> menus) {
        List<MenuResponseDto> menuResponseDtos = menus.stream()
                .map(this::convertToMenuResponseDto)
                .collect(Collectors.toList());

        return StoreDetailResponseDto.builder()
                .storeId(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .zipcode(store.getZipcode())
                .address1(store.getAddress1())
                .address2(store.getAddress2())
                .phoneNumber(store.getPhoneNumber())
                .storeImageUrl(store.getStoreImageUrl())
                .categoryId(store.getCategoryId())
                .minOrderAmount(store.getMinOrderAmount())
                .deliveryFee(store.getDeliveryFee())
                .status(store.getStatus())
                .averageRating(store.getAverageRating())
                .reviewCount(store.getReviewCount())
                .operatingHours(store.getOperatingHours())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .menus(menuResponseDtos)
                .build();
    }

    /**
     * Menu 엔티티를 MenuResponseDto로 변환
     * 
     * @param menu Menu 엔티티
     * @return MenuResponseDto
     */
    private MenuResponseDto convertToMenuResponseDto(Menu menu) {
        return MenuResponseDto.builder()
                .menuId(menu.getId())
                .storeId(menu.getStoreId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .status(menu.getStatus())
                .menuImageUrl(menu.getMenuImageUrl())
                .menuCategory(menu.getMenuCategory())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
} 