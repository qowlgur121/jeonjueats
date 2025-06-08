package com.jeonjueats.service;

import com.jeonjueats.dto.MenuCreateRequestDto;
import com.jeonjueats.dto.MenuUpdateRequestDto;
import com.jeonjueats.entity.Menu;
import com.jeonjueats.entity.MenuStatus;
import com.jeonjueats.entity.Store;
import com.jeonjueats.exception.StoreNotFoundException;
import com.jeonjueats.exception.UnauthorizedAccessException;
import com.jeonjueats.exception.MenuNotFoundException;
import com.jeonjueats.repository.MenuRepository;
import com.jeonjueats.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 사장님 메뉴 관리 서비스
 * 사장님(ROLE_OWNER)의 메뉴 등록, 조회, 수정, 판매 상태 변경 등을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerMenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    /**
     * 새로운 메뉴 등록
     * 초기 판매 상태는 AVAILABLE로 설정
     * 
     * @param ownerId 가게 소유자 ID (현재 인증된 사장님)
     * @param storeId 메뉴를 등록할 가게 ID
     * @param requestDto 메뉴 등록 요청 정보
     * @return 등록된 메뉴 정보
     */
    @Transactional
    public Menu createMenu(Long ownerId, Long storeId, MenuCreateRequestDto requestDto) {
        log.info("메뉴 등록 시작 - 사장님 ID: {}, 가게 ID: {}, 메뉴명: {}", 
                ownerId, storeId, requestDto.getMenuName());

        // 1. 가게 조회 및 소유권 검증
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("존재하지 않는 가게입니다."));
        
        validateStoreOwnership(store, ownerId);

        // 2. Menu 엔티티 생성 (초기 상태: AVAILABLE)
        Menu menu = new Menu(
                storeId,
                requestDto.getMenuName(),
                requestDto.getDescription(),
                requestDto.getPrice()
        );

        // 3. 선택적 필드 설정
        if (requestDto.getMenuImageUrl() != null) {
            menu.setMenuImageUrl(requestDto.getMenuImageUrl());
        }

        // 4. 메뉴 저장
        Menu savedMenu = menuRepository.save(menu);

        log.info("메뉴 등록 완료 - 메뉴 ID: {}, 메뉴명: {}", savedMenu.getId(), savedMenu.getName());

        return savedMenu;
    }

    /**
     * 가게 소유권 검증
     * 현재 인증된 사장님이 해당 가게의 소유자인지 확인
     * 
     * @param store 가게 엔티티
     * @param ownerId 사장님 ID
     * @throws UnauthorizedAccessException 소유권이 없을 경우
     */
    private void validateStoreOwnership(Store store, Long ownerId) {
        if (!store.getOwnerId().equals(ownerId)) {
            log.warn("가게 소유권 검증 실패 - 가게 ID: {}, 사장님 ID: {}", store.getId(), ownerId);
            throw new UnauthorizedAccessException("해당 가게에 대한 권한이 없습니다.");
        }
        
        log.debug("가게 소유권 검증 성공 - 가게 ID: {}, 사장님 ID: {}", store.getId(), ownerId);
    }

    /**
     * 가게 메뉴 목록 조회 (페이징)
     * 소유권 검증 후, 해당 가게의 모든 메뉴를 페이징하여 반환
     * 논리적으로 삭제된 메뉴는 제외
     * 
     * @param ownerId 가게 소유자 ID (현재 인증된 사장님)
     * @param storeId 조회할 가게 ID
     * @param pageable 페이징 정보
     * @return 메뉴 목록 (페이징)
     */
    public Page<Menu> getMenus(Long ownerId, Long storeId, Pageable pageable) {
        log.info("메뉴 목록 조회 시작 - 사장님 ID: {}, 가게 ID: {}", ownerId, storeId);

        // 1. 가게 조회 및 소유권 검증
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("존재하지 않는 가게입니다."));
        
        validateStoreOwnership(store, ownerId);

        // 2. 메뉴 목록 조회 (논리적 삭제 제외)
        Page<Menu> menus = menuRepository.findByStoreIdAndIsDeletedFalseOrderByCreatedAtDesc(storeId, pageable);

        log.info("메뉴 목록 조회 완료 - 가게 ID: {}, 메뉴 수: {}/{}", 
                storeId, menus.getContent().size(), menus.getTotalElements());

        return menus;
    }

    /**
     * 메뉴 정보 수정
     * 소유권 검증 후, 제공된 필드만 업데이트 (Dirty Checking 활용)
     * 
     * @param ownerId 가게 소유자 ID (현재 인증된 사장님)
     * @param storeId 가게 ID
     * @param menuId 수정할 메뉴 ID
     * @param requestDto 수정할 메뉴 정보
     * @return 수정된 메뉴 정보
     */
    @Transactional
    public Menu updateMenu(Long ownerId, Long storeId, Long menuId, MenuUpdateRequestDto requestDto) {
        log.info("메뉴 수정 시작 - 사장님 ID: {}, 가게 ID: {}, 메뉴 ID: {}", 
                ownerId, storeId, menuId);

        // 1. 가게 조회 및 소유권 검증
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("존재하지 않는 가게입니다."));
        
        validateStoreOwnership(store, ownerId);

        // 2. 메뉴 조회 (해당 가게의 메뉴인지도 함께 검증)
        Menu menu = menuRepository.findByIdAndStoreIdAndIsDeletedFalse(menuId, storeId)
                .orElseThrow(() -> new MenuNotFoundException("존재하지 않는 메뉴입니다."));

        // 3. 제공된 필드만 업데이트 (Dirty Checking)
        if (requestDto.getMenuName() != null) {
            menu.setName(requestDto.getMenuName());
        }
        if (requestDto.getDescription() != null) {
            menu.setDescription(requestDto.getDescription());
        }
        if (requestDto.getPrice() != null) {
            menu.setPrice(requestDto.getPrice());
        }
        if (requestDto.getMenuImageUrl() != null) {
            menu.setMenuImageUrl(requestDto.getMenuImageUrl());
        }

        // @Transactional에 의해 자동으로 DB에 반영됨

        log.info("메뉴 수정 완료 - 메뉴 ID: {}, 메뉴명: {}", menu.getId(), menu.getName());

        return menu;
    }

    /**
     * 메뉴 삭제 (논리적)
     * 소유권 검증 후, 메뉴를 논리적으로 삭제 처리
     * 실제로는 is_deleted 플래그만 true로 변경
     * 
     * @param ownerId 가게 소유자 ID (현재 인증된 사장님)
     * @param storeId 가게 ID
     * @param menuId 삭제할 메뉴 ID
     */
    @Transactional
    public void deleteMenu(Long ownerId, Long storeId, Long menuId) {
        log.info("메뉴 삭제 시작 - 사장님 ID: {}, 가게 ID: {}, 메뉴 ID: {}", 
                ownerId, storeId, menuId);

        // 1. 가게 조회 및 소유권 검증
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("존재하지 않는 가게입니다."));
        
        validateStoreOwnership(store, ownerId);

        // 2. 메뉴 조회 (해당 가게의 메뉴인지도 함께 검증)
        Menu menu = menuRepository.findByIdAndStoreIdAndIsDeletedFalse(menuId, storeId)
                .orElseThrow(() -> new MenuNotFoundException("존재하지 않는 메뉴입니다."));

        // 3. 메뉴 논리적 삭제 (@SQLDelete 애노테이션에 의해 UPDATE 쿼리 실행)
        menuRepository.delete(menu);

        log.info("메뉴 논리적 삭제 완료 - 메뉴 ID: {}, 메뉴명: {}", menu.getId(), menu.getName());
    }

    /**
     * 메뉴 판매 상태 토글
     * 소유권 검증 후, 메뉴의 판매 상태를 AVAILABLE ↔ SOLD_OUT으로 토글
     * 
     * @param ownerId 가게 소유자 ID (현재 인증된 사장님)
     * @param storeId 가게 ID
     * @param menuId 상태 변경할 메뉴 ID
     * @return 상태가 변경된 메뉴 엔티티
     */
    @Transactional
    public Menu toggleMenuAvailability(Long ownerId, Long storeId, Long menuId) {
        log.info("메뉴 판매 상태 토글 시작 - 사장님 ID: {}, 가게 ID: {}, 메뉴 ID: {}", 
                ownerId, storeId, menuId);

        // 1. 가게 조회 및 소유권 검증
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("존재하지 않는 가게입니다."));
        
        validateStoreOwnership(store, ownerId);

        // 2. 메뉴 조회
        Menu menu = menuRepository.findByIdAndStoreIdAndIsDeletedFalse(menuId, storeId)
                .orElseThrow(() -> new MenuNotFoundException("존재하지 않는 메뉴입니다."));

        // 3. 판매 상태 토글
        if (menu.getStatus() == MenuStatus.AVAILABLE) {
            menu.makeSoldOut();
            log.info("메뉴 상태 변경: AVAILABLE → SOLD_OUT - 메뉴 ID: {}", menuId);
        } else {
            menu.makeAvailable();
            log.info("메뉴 상태 변경: SOLD_OUT → AVAILABLE - 메뉴 ID: {}", menuId);
        }

        // 4. JPA Dirty Checking으로 자동 저장
        log.info("메뉴 판매 상태 토글 완료 - 메뉴 ID: {}, 최종 상태: {}", 
                menu.getId(), menu.getStatus());

        return menu;
    }
} 