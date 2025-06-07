package com.jeonjueats.service;

import com.jeonjueats.dto.StoreCreateRequestDto;
import com.jeonjueats.dto.StoreResponseDto;
import com.jeonjueats.dto.StoreUpdateRequestDto;
import com.jeonjueats.entity.Category;
import com.jeonjueats.entity.Store;
import com.jeonjueats.entity.StoreStatus;
import com.jeonjueats.exception.CategoryNotFoundException;
import com.jeonjueats.exception.StoreNotFoundException;
import com.jeonjueats.exception.UnauthorizedAccessException;
import com.jeonjueats.repository.CategoryRepository;
import com.jeonjueats.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사장님 가게 관리 서비스
 * 사장님(ROLE_OWNER)의 가게 등록, 조회, 수정, 운영 상태 변경 등을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerStoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 새로운 가게 등록
     * 초기 상태는 CLOSED로 설정
     * 
     * @param ownerId 가게 소유자 ID (현재 인증된 사장님)
     * @param requestDto 가게 등록 요청 정보
     * @return 등록된 가게 정보
     */
    @Transactional
    public StoreResponseDto createStore(Long ownerId, StoreCreateRequestDto requestDto) {
        log.info("가게 등록 시작 - 사장님 ID: {}, 가게명: {}", ownerId, requestDto.getName());

        // 1. Store 엔티티 생성 (초기 상태: CLOSED)
        Store store = new Store(
                ownerId,
                requestDto.getCategoryId(),
                requestDto.getName(),
                requestDto.getZipcode(),
                requestDto.getAddress1(),
                requestDto.getPhoneNumber()
        );

        // 2. 선택적 필드들 설정
        store.setAddress2(requestDto.getAddress2());
        store.setDescription(requestDto.getDescription());
        store.setStoreImageUrl(requestDto.getStoreImageUrl());
        store.setMinOrderAmount(requestDto.getMinOrderAmount());
        store.setDeliveryFee(requestDto.getDeliveryFee());
        
        // 3. 초기 운영 상태를 CLOSED로 설정 (요구사항)
        store.setStatus(StoreStatus.CLOSED);

        // 4. 가게 저장
        Store savedStore = storeRepository.save(store);

        log.info("가게 등록 완료 - 가게 ID: {}, 가게명: {}", savedStore.getId(), savedStore.getName());

        // 5. 응답 DTO 변환 후 반환
        return convertToResponseDto(savedStore);
    }

    /**
     * 내 가게 목록 조회 (페이징)
     * 현재 인증된 사장님이 소유한 모든 가게를 페이징하여 반환
     * 
     * @param ownerId 사장님 ID
     * @param pageable 페이징 정보
     * @return 가게 목록 (페이징)
     */
    public Page<StoreResponseDto> getMyStores(Long ownerId, Pageable pageable) {
        log.info("내 가게 목록 조회 시작 - 사장님 ID: {}, 페이지: {}, 사이즈: {}", 
                 ownerId, pageable.getPageNumber(), pageable.getPageSize());

        // 1. 사장님이 소유한 가게 목록 조회 (페이징)
        Page<Store> storePage = storeRepository.findByOwnerIdAndIsDeletedFalseOrderByCreatedAtDesc(ownerId, pageable);

        log.info("내 가게 목록 조회 완료 - 사장님 ID: {}, 총 {}개 가게 ({}페이지 중 {}페이지)", 
                 ownerId, storePage.getTotalElements(), storePage.getTotalPages(), pageable.getPageNumber() + 1);

        // 2. Store 엔티티를 StoreResponseDto로 변환
        return storePage.map(this::convertToResponseDto);
    }

    /**
     * 내 가게 상세 정보 조회
     * 특정 가게의 상세 정보를 조회하며, 소유권을 검증
     * 
     * @param storeId 가게 ID
     * @param ownerId 사장님 ID
     * @return 가게 상세 정보
     * @throws IllegalArgumentException 가게가 없거나 소유권이 없을 경우
     */
    public StoreResponseDto getStoreDetail(Long storeId, Long ownerId) {
        log.info("내 가게 상세 조회 시작 - 가게 ID: {}, 사장님 ID: {}", storeId, ownerId);

        // 1. 가게 조회 및 소유권 검증 (한 번의 쿼리로 처리)
        Store store = storeRepository.findByIdAndOwnerIdAndIsDeletedFalse(storeId, ownerId)
                .orElseThrow(() -> {
                    log.warn("가게 조회 실패 - 가게 ID: {}, 사장님 ID: {}", storeId, ownerId);
                    return new IllegalArgumentException("해당 가게를 찾을 수 없거나 접근 권한이 없습니다.");
                });

        log.info("내 가게 상세 조회 완료 - 가게 ID: {}, 가게명: {}", store.getId(), store.getName());

        // 2. 응답 DTO 변환 후 반환
        return convertToResponseDto(store);
    }

    /**
     * 가게 소유권 검증
     * 현재 인증된 사장님이 해당 가게의 소유자인지 확인
     * 
     * @param storeId 가게 ID
     * @param ownerId 사장님 ID
     * @throws IllegalArgumentException 소유권이 없을 경우
     */
    public void validateStoreOwnership(Long storeId, Long ownerId) {
        boolean isOwner = storeRepository.existsByIdAndOwnerIdAndIsDeletedFalse(storeId, ownerId);
        
        if (!isOwner) {
            log.warn("가게 소유권 검증 실패 - 가게 ID: {}, 사장님 ID: {}", storeId, ownerId);
            throw new IllegalArgumentException("해당 가게에 대한 권한이 없습니다.");
        }
        
        log.debug("가게 소유권 검증 성공 - 가게 ID: {}, 사장님 ID: {}", storeId, ownerId);
    }

    /**
     * 가게 정보 수정
     * 
     * @param ownerId 사장님 사용자 ID
     * @param storeId 수정할 가게 ID
     * @param request 수정할 가게 정보 (포함된 필드만 업데이트)
     * @return 수정된 가게 정보
     */
    @Transactional
    public StoreResponseDto updateStore(Long ownerId, Long storeId, StoreUpdateRequestDto request) {
        log.info("Updating store for owner: {}, storeId: {}", ownerId, storeId);
        
        // 가게 조회 및 소유권 검증
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("존재하지 않는 가게입니다."));
        
        validateStoreOwnership(store, ownerId);
        
        // 카테고리 변경 시 유효성 검증
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("존재하지 않는 카테고리입니다."));
            store.setCategoryId(request.getCategoryId());
        } else {
            // 카테고리 변경이 없는 경우 기존 카테고리 정보 조회
            category = categoryRepository.findById(store.getCategoryId()).orElse(null);
        }
        
        // JPA 변경 감지(Dirty Checking)를 활용한 필드별 업데이트
        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getZipcode() != null) {
            store.setZipcode(request.getZipcode());
        }
        if (request.getAddress1() != null) {
            store.setAddress1(request.getAddress1());
        }
        if (request.getAddress2() != null) {
            store.setAddress2(request.getAddress2());
        }
        if (request.getPhoneNumber() != null) {
            store.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDescription() != null) {
            store.setDescription(request.getDescription());
        }
        if (request.getStoreImageUrl() != null) {
            store.setStoreImageUrl(request.getStoreImageUrl());
        }
        if (request.getMinOrderAmount() != null) {
            store.setMinOrderAmount(request.getMinOrderAmount());
        }
        if (request.getDeliveryFee() != null) {
            store.setDeliveryFee(request.getDeliveryFee());
        }
        
        // @Transactional에 의해 자동으로 변경사항이 DB에 반영됨 (Dirty Checking)
        log.info("Store updated successfully: {}", storeId);
        
        return convertToResponseDto(store);
    }

    /**
     * 가게 운영 상태 토글
     * OPEN ↔ CLOSED 상태를 변경
     * 
     * @param ownerId 사장님 사용자 ID
     * @param storeId 상태를 변경할 가게 ID
     * @return 변경된 가게 정보
     */
    @Transactional
    public StoreResponseDto toggleStoreOperationStatus(Long ownerId, Long storeId) {
        log.info("Toggling store operation status for owner: {}, storeId: {}", ownerId, storeId);
        
        // 가게 조회 및 소유권 검증
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("존재하지 않는 가게입니다."));
        
        validateStoreOwnership(store, ownerId);
        
        // 현재 상태에 따라 토글
        StoreStatus newStatus = (store.getStatus() == StoreStatus.OPEN) 
                ? StoreStatus.CLOSED 
                : StoreStatus.OPEN;
        
        store.setStatus(newStatus);
        
        // @Transactional에 의해 자동으로 변경사항이 DB에 반영됨 (Dirty Checking)
        log.info("Store operation status changed from {} to {} for storeId: {}", 
                store.getStatus() == StoreStatus.OPEN ? StoreStatus.CLOSED : StoreStatus.OPEN, 
                newStatus, storeId);
        
        return convertToResponseDto(store);
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
     * 가게 소유권 검증
     */
    private void validateStoreOwnership(Store store, Long ownerId) {
        if (!store.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedAccessException("해당 가게에 대한 접근 권한이 없습니다.");
        }
    }
} 