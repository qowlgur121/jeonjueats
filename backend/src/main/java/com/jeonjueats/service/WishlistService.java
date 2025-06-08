package com.jeonjueats.service;

import com.jeonjueats.dto.WishlistResponseDto;
import com.jeonjueats.dto.WishlistToggleResponseDto;
import com.jeonjueats.entity.Store;
import com.jeonjueats.entity.Wishlist;
import com.jeonjueats.exception.StoreNotFoundException;
import com.jeonjueats.repository.StoreRepository;
import com.jeonjueats.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 찜하기 기능 Service
 * 가게 찜하기/해제, 찜 목록 조회 등의 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final StoreRepository storeRepository;

    /**
     * 가게 찜 상태 토글 (찜하기/해제)
     * 이미 찜한 상태면 찜 해제, 아니면 찜 추가
     *
     * @param userId 사용자 ID
     * @param storeId 가게 ID
     * @return 변경된 찜 상태 정보
     */
    @Transactional
    public WishlistToggleResponseDto toggleWishlist(Long userId, Long storeId) {
        log.info("찜 상태 토글 요청 - 사용자 ID: {}, 가게 ID: {}", userId, storeId);

        // 가게 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("가게를 찾을 수 없습니다."));

        // 기존 찜 여부 확인
        Optional<Wishlist> existingWishlist = wishlistRepository.findByUserIdAndStoreId(userId, storeId);

        boolean isWished;
        String message;

        if (existingWishlist.isPresent()) {
            // 이미 찜한 상태 -> 찜 해제
            wishlistRepository.deleteByUserIdAndStoreId(userId, storeId);
            isWished = false;
            message = "찜을 해제했습니다.";
            log.info("찜 해제 완료 - 사용자 ID: {}, 가게 ID: {}", userId, storeId);
        } else {
            // 찜하지 않은 상태 -> 찜 추가
            Wishlist newWishlist = new Wishlist(userId, storeId);
            wishlistRepository.save(newWishlist);
            isWished = true;
            message = "찜했습니다.";
            log.info("찜 추가 완료 - 사용자 ID: {}, 가게 ID: {}", userId, storeId);
        }

        return WishlistToggleResponseDto.builder()
                .storeId(storeId)
                .isWished(isWished)
                .message(message)
                .build();
    }

    /**
     * 내 찜 목록 조회 (페이징)
     * 찜한 가게들의 간략한 정보를 최신순으로 조회
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 찜 목록 (가게 정보 포함)
     */
    public Page<WishlistResponseDto> getMyWishlists(Long userId, Pageable pageable) {
        log.info("찜 목록 조회 요청 - 사용자 ID: {}, 페이지: {}, 크기: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Wishlist> wishlistPage = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return wishlistPage.map(this::convertToWishlistResponseDto);
    }

    /**
     * 특정 가게의 찜 여부 확인
     *
     * @param userId 사용자 ID
     * @param storeId 가게 ID
     * @return 찜 여부
     */
    public boolean isWished(Long userId, Long storeId) {
        return wishlistRepository.existsByUserIdAndStoreId(userId, storeId);
    }

    /**
     * Wishlist 엔티티를 WishlistResponseDto로 변환
     */
    private WishlistResponseDto convertToWishlistResponseDto(Wishlist wishlist) {
        // Store 정보 조회 (동적 조회 - Task 10 패턴 활용)
        Store store = storeRepository.findById(wishlist.getStoreId())
                .orElse(null); // 가게가 삭제되었을 경우를 대비

        WishlistResponseDto.StoreInfo storeInfo;
        if (store != null) {
            storeInfo = WishlistResponseDto.StoreInfo.builder()
                    .storeId(store.getId())
                    .storeName(store.getName())
                    .storeImageUrl(store.getStoreImageUrl()) // Store 엔티티의 getStoreImageUrl() 사용
                    .minOrderAmount(store.getMinOrderAmount().intValue())
                    .deliveryTip(store.getDeliveryFee().intValue())
                    .storeStatus(store.getStatus().name())
                    .description(store.getDescription())
                    .build();
        } else {
            // 가게가 삭제된 경우
            storeInfo = WishlistResponseDto.StoreInfo.builder()
                    .storeId(wishlist.getStoreId())
                    .storeName("삭제된 가게")
                    .storeImageUrl(null)
                    .minOrderAmount(0)
                    .deliveryTip(0)
                    .storeStatus("DELETED")
                    .description("더 이상 존재하지 않는 가게입니다.")
                    .build();
        }

        return WishlistResponseDto.builder()
                .wishlistId(wishlist.getId())
                .wishedAt(wishlist.getCreatedAt())
                .store(storeInfo)
                .build();
    }
} 