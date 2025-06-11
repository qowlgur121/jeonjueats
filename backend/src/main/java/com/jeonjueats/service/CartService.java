package com.jeonjueats.service;

import com.jeonjueats.dto.CartItemAddRequestDto;
import com.jeonjueats.dto.CartItemRequestDto;
import com.jeonjueats.dto.CartItemResponseDto;
import com.jeonjueats.dto.CartResponseDto;
import com.jeonjueats.entity.Cart;
import com.jeonjueats.entity.CartItem;
import com.jeonjueats.entity.Menu;
import com.jeonjueats.entity.Store;
import com.jeonjueats.exception.CartNotFoundException;
import com.jeonjueats.exception.InvalidCartOperationException;
import com.jeonjueats.exception.MenuNotFoundException;
import com.jeonjueats.exception.StoreNotFoundException;
import com.jeonjueats.repository.CartItemRepository;
import com.jeonjueats.repository.CartRepository;
import com.jeonjueats.repository.MenuRepository;
import com.jeonjueats.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 장바구니 서비스
 * 사용자의 장바구니 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    // MVP 고정값: 배달비
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("3000");

    /**
     * 장바구니에 메뉴 추가
     * MVP 핵심 비즈니스 로직: 한 번에 한 가게 메뉴만 담기 가능
     */
    @Transactional
    public CartItemResponseDto addMenuToCart(Long userId, CartItemAddRequestDto request) {
        log.info("장바구니 메뉴 추가 시작 - 사용자: {}, 메뉴 ID: {}, 수량: {}", 
                userId, request.getMenuId(), request.getQuantity());

        // 1. 메뉴 존재 확인 및 조회
        Menu menu = menuRepository.findByIdAndIsDeletedFalse(request.getMenuId())
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다. ID: " + request.getMenuId()));

        // 2. 메뉴가 속한 가게 조회
        Store store = storeRepository.findByIdAndIsDeletedFalse(menu.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("가게를 찾을 수 없습니다. ID: " + menu.getStoreId()));

        log.info("메뉴 정보 확인 완료 - 메뉴: {}, 가게: {}, 가격: {}", 
                menu.getName(), store.getName(), menu.getPrice());

        // 3. 사용자 장바구니 조회 또는 생성
        Cart cart = getOrCreateUserCart(userId);

        // 4. MVP 비즈니스 규칙: 한 번에 한 가게 메뉴만 담기 가능
        validateSingleStoreRule(cart, menu.getStoreId());

        // 5. 장바구니가 비어있다면 현재 가게로 설정
        if (cart.isEmpty()) {
            cart.setStoreId(menu.getStoreId());
            cartRepository.save(cart);
            log.info("빈 장바구니에 가게 설정 완료 - 가게 ID: {}", menu.getStoreId());
        }

        // 6. 이미 담긴 메뉴인지 확인 (수량 증가 vs 새로 추가)
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndMenuId(cart.getId(), menu.getId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // 기존 아이템 수량 증가
            cartItem = existingItem.get();
            cartItem.increaseQuantity(request.getQuantity());
            log.info("기존 메뉴 수량 증가 - 메뉴: {}, 기존 수량: {}, 추가 수량: {}, 총 수량: {}", 
                    menu.getName(), cartItem.getQuantity() - request.getQuantity(), 
                    request.getQuantity(), cartItem.getQuantity());
        } else {
            // 새 아이템 추가
            cartItem = new CartItem(cart.getId(), menu.getId(), request.getQuantity());
            log.info("새 메뉴 장바구니 추가 - 메뉴: {}, 수량: {}", menu.getName(), request.getQuantity());
        }

        cartItem = cartItemRepository.save(cartItem);

        // 7. 응답 DTO 생성
        CartItemResponseDto response = CartItemResponseDto.builder()
                .cartItemId(cartItem.getId())
                .menuId(menu.getId())
                .menuName(menu.getName())
                .menuDescription(menu.getDescription())
                .menuPrice(menu.getPrice())
                .menuImageUrl(menu.getMenuImageUrl())
                .quantity(cartItem.getQuantity())
                .itemTotalPrice(menu.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .addedAt(cartItem.getCreatedAt())
                .build();

        log.info("장바구니 메뉴 추가 완료 - 사용자: {}, 메뉴: {}, 최종 수량: {}", 
                userId, menu.getName(), cartItem.getQuantity());

        return response;
    }

    /**
     * 사용자 장바구니 조회 또는 생성
     */
    private Cart getOrCreateUserCart(Long userId) {
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        
        if (existingCart.isPresent()) {
            log.info("기존 장바구니 조회 완료 - 사용자: {}, 장바구니 ID: {}", userId, existingCart.get().getId());
            return existingCart.get();
        } else {
            Cart newCart = new Cart(userId);
            newCart = cartRepository.save(newCart);
            log.info("새 장바구니 생성 완료 - 사용자: {}, 장바구니 ID: {}", userId, newCart.getId());
            return newCart;
        }
    }

    /**
     * 사용자 장바구니 조회
     * 장바구니 내용과 주문 요약 정보를 함께 반환
     * 
     * @param userId 사용자 ID
     * @return 장바구니 전체 정보 (아이템 목록, 주문 요약 등)
     */
    @Transactional(readOnly = true)
    public CartResponseDto getCart(Long userId) {
        log.info("장바구니 조회 요청 - 사용자 ID: {}", userId);
        
        Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
        
        // 장바구니가 없거나 비어있는 경우
        if (cartOpt.isEmpty()) {
            log.info("장바구니가 존재하지 않음 - 사용자 ID: {}", userId);
            return createEmptyCartResponse();
        }
        
        Cart cart = cartOpt.get();
        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByCreatedAtDesc(cart.getId());
        
        // 장바구니는 있지만 아이템이 없는 경우
        if (cartItems.isEmpty()) {
            log.info("장바구니가 비어있음 - 사용자 ID: {}", userId);
            return createEmptyCartResponse();
        }
        
        // 가게 정보 조회
        Store store = storeRepository.findById(cart.getStoreId())
            .orElseThrow(() -> new StoreNotFoundException(cart.getStoreId()));
        
        // 장바구니 아이템 DTO 변환
        List<CartItemResponseDto> itemDtos = cartItems.stream()
            .map(this::convertToCartItemDto)
            .collect(Collectors.toList());
        
        // 주문 요약 정보 계산
        OrderSummary summary = calculateOrderSummary(cartItems, store);
        
        log.info("장바구니 조회 완료 - 사용자 ID: {}, 아이템 수: {}, 총 금액: {}", 
                userId, itemDtos.size(), summary.finalPrice);
        
        return CartResponseDto.builder()
            .cartId(cart.getId())
            .storeId(store.getId())
            .storeName(store.getName())
            .storeImageUrl(store.getStoreImageUrl())
            .items(itemDtos)
            .totalItemCount(itemDtos.size())
            .totalQuantity(summary.totalQuantity)
            .totalPrice(summary.totalPrice)
            .deliveryFee(summary.deliveryFee)
            .finalPrice(summary.finalPrice)
            .isEmpty(false)
            .build();
    }
    
    /**
     * 빈 장바구니 응답 생성
     */
    private CartResponseDto createEmptyCartResponse() {
        return CartResponseDto.builder()
            .cartId(null)
            .storeId(null)
            .storeName(null)
            .storeImageUrl(null)
            .items(Collections.emptyList())
            .totalItemCount(0)
            .totalQuantity(0)
            .totalPrice(BigDecimal.ZERO)
            .deliveryFee(BigDecimal.ZERO)
            .finalPrice(BigDecimal.ZERO)
            .isEmpty(true)
            .build();
    }
    
    /**
     * CartItem을 CartItemResponseDto로 변환
     */
    private CartItemResponseDto convertToCartItemDto(CartItem cartItem) {
        Menu menu = menuRepository.findByIdAndIsDeletedFalse(cartItem.getMenuId())
            .orElseThrow(() -> new MenuNotFoundException(cartItem.getMenuId()));
        
        BigDecimal itemTotalPrice = menu.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        
        return CartItemResponseDto.builder()
            .cartItemId(cartItem.getId())
            .menuId(menu.getId())
            .menuName(menu.getName())
            .menuDescription(menu.getDescription())
            .menuPrice(menu.getPrice())
            .menuImageUrl(menu.getMenuImageUrl())
            .quantity(cartItem.getQuantity())
            .itemTotalPrice(itemTotalPrice)
            .addedAt(cartItem.getCreatedAt())
            .build();
    }
    
    /**
     * 주문 요약 정보 계산
     */
    private OrderSummary calculateOrderSummary(List<CartItem> cartItems, Store store) {
        // 총 수량 계산
        int totalQuantity = cartItems.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
        
        // 총 상품 금액 계산
        BigDecimal totalPrice = cartItems.stream()
            .map(item -> {
                Menu menu = menuRepository.findByIdAndIsDeletedFalse(item.getMenuId())
                    .orElseThrow(() -> new MenuNotFoundException(item.getMenuId()));
                return menu.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 배달비 (가게별 고정)
        BigDecimal deliveryFee = store.getDeliveryFee();
        
        // 최종 결제 금액
        BigDecimal finalPrice = totalPrice.add(deliveryFee);
        
        return new OrderSummary(totalQuantity, totalPrice, deliveryFee, finalPrice);
    }
    
    /**
     * 주문 요약 정보를 담는 내부 클래스
     */
    private static class OrderSummary {
        final int totalQuantity;
        final BigDecimal totalPrice;
        final BigDecimal deliveryFee;
        final BigDecimal finalPrice;
        
        OrderSummary(int totalQuantity, BigDecimal totalPrice, BigDecimal deliveryFee, BigDecimal finalPrice) {
            this.totalQuantity = totalQuantity;
            this.totalPrice = totalPrice;
            this.deliveryFee = deliveryFee;
            this.finalPrice = finalPrice;
        }
    }

    /**
     * 장바구니 메뉴 추가/수량 변경 통합 처리 
     * cartItemId 존재 여부로 추가/변경 구분
     */
    @Transactional
    public CartItemResponseDto processCartItem(Long userId, CartItemRequestDto request) {
        log.info("장바구니 처리 요청 - 사용자: {}, 요청 타입: {}", 
                userId, request.isAddMenuRequest() ? "메뉴 추가" : "수량 변경");
        
        if (request.isAddMenuRequest()) {
            // 메뉴 추가 처리 (기존 로직 재사용)
            return addMenuToCartInternal(userId, request.getStoreId(), request.getMenuId(), request.getQuantity());
        } else {
            // 수량 변경 처리 (덮어쓰기)
            return updateCartItemQuantity(userId, request.getCartItemId(), request.getQuantity());
        }
    }
    
    /**
     * 메뉴 추가 내부 처리
     */
    private CartItemResponseDto addMenuToCartInternal(Long userId, Long storeId, Long menuId, Integer quantity) {
        log.info("메뉴 추가 내부 처리 - 사용자: {}, 가게: {}, 메뉴: {}, 수량: {}", 
                userId, storeId, menuId, quantity);

        // 1. 메뉴 존재 확인 및 조회
        Menu menu = menuRepository.findByIdAndIsDeletedFalse(menuId)
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다. ID: " + menuId));

        // 2. 메뉴가 속한 가게 확인
        if (!menu.getStoreId().equals(storeId)) {
            throw new InvalidCartOperationException("메뉴가 해당 가게에 속하지 않습니다.");
        }

        // 3. 가게 조회
        Store store = storeRepository.findByIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new StoreNotFoundException("가게를 찾을 수 없습니다. ID: " + storeId));

        // 4. 사용자 장바구니 조회 또는 생성
        Cart cart = getOrCreateUserCart(userId);

        // 5. MVP 비즈니스 규칙: 한 번에 한 가게 메뉴만 담기 가능
        validateSingleStoreRule(cart, storeId);

        // 6. 장바구니가 비어있다면 현재 가게로 설정
        if (cart.isEmpty()) {
            cart.setStoreId(storeId);
            cartRepository.save(cart);
        }

        // 7. 이미 담긴 메뉴인지 확인 
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndMenuId(cart.getId(), menuId);
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // 기존 아이템 수량 덮어쓰기 
            cartItem = existingItem.get();
            cartItem.setQuantity(quantity); // 덮어쓰기
            log.info("기존 메뉴 수량 덮어쓰기 - 메뉴: {}, 기존 수량: {} → 새 수량: {}", 
                    menu.getName(), cartItem.getQuantity(), quantity);
        } else {
            // 새 아이템 추가
            cartItem = new CartItem(cart.getId(), menuId, quantity);
            log.info("새 메뉴 장바구니 추가 - 메뉴: {}, 수량: {}", menu.getName(), quantity);
        }

        cartItem = cartItemRepository.save(cartItem);
        return convertToCartItemDto(cartItem);
    }
    
    /**
     * 장바구니 아이템 수량 변경 (덮어쓰기)
     */
    private CartItemResponseDto updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        log.info("장바구니 아이템 수량 변경 요청 - 사용자: {}, 아이템 ID: {}, 새 수량: {}", 
                userId, cartItemId, quantity);
        
        // 장바구니 아이템 조회 및 소유권 검증
        CartItem cartItem = findCartItemByIdAndUserId(cartItemId, userId);
        
        // 수량 덮어쓰기 (PRD 변경사항)
        cartItem.setQuantity(quantity);
        // JPA Dirty Checking으로 자동 저장
        
        log.info("장바구니 아이템 수량 변경 완료 - 아이템 ID: {}, 새 수량: {}", cartItemId, quantity);
        
        return convertToCartItemDto(cartItem);
    }

    /**
     * 장바구니 아이템 수량 변경 (기존 메서드, 호환성 유지)
     * @deprecated 새로운 통합 API processCartItem 사용 권장
     */
    @Deprecated
    @Transactional
    public CartItemResponseDto updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        return updateCartItemQuantity(userId, cartItemId, quantity);
    }
    
    /**
     * 특정 장바구니 아이템 삭제
     * 
     * @param userId 사용자 ID
     * @param cartItemId 삭제할 장바구니 아이템 ID
     */
    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        log.info("장바구니 아이템 삭제 요청 - 사용자: {}, 아이템 ID: {}", userId, cartItemId);
        CartItem cartItem = findCartItemByIdAndUserId(cartItemId, userId);
        cartItemRepository.delete(cartItem);
        log.info("장바구니 아이템 삭제 완료 - 아이템 ID: {}", cartItemId);
    }

    /**
     * 사용자 장바구니 전체 비우기
     * 
     * @param userId 사용자 ID
     */
    @Transactional
    public void clearCart(Long userId) {
        log.info("장바구니 전체 비우기 요청 - 사용자: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("장바구니를 찾을 수 없습니다."));
        
        // 1. 장바구니 아이템들 삭제
        cartItemRepository.deleteByCartId(cart.getId());
        log.info("장바구니 아이템 삭제 완료 - 사용자: {}", userId);
        
        // 2. 장바구니 상태 초기화 (storeId를 null로 설정)
        cart.clear();
        cartRepository.save(cart);
        log.info("장바구니 상태 초기화 완료 - 사용자: {}", userId);
        
        log.info("장바구니 전체 비우기 완료 - 사용자: {}", userId);
    }

    /**
     * cartItemId와 userId로 장바구니 아이템을 조회하고 소유권을 검증
     */
    private CartItem findCartItemByIdAndUserId(Long cartItemId, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartNotFoundException("장바구니 아이템을 찾을 수 없습니다."));

        // 소유권 검증: 해당 아이템이 현재 사용자의 장바구니에 속하는지 확인
        Cart cart = cartRepository.findById(cartItem.getCartId())
                .orElseThrow(() -> new CartNotFoundException("장바구니를 찾을 수 없습니다."));
        
        if (!cart.getUserId().equals(userId)) {
            throw new InvalidCartOperationException("해당 장바구니 아이템에 대한 접근 권한이 없습니다.");
        }
        return cartItem;
    }

    /**
     * MVP 핵심 비즈니스 규칙 검증: 한 번에 한 가게 메뉴만 담기 가능
     */
    private void validateSingleStoreRule(Cart cart, Long requestedStoreId) {
        if (!cart.canAddMenuFromStore(requestedStoreId)) {
            throw new InvalidCartOperationException(
                "다른 가게의 메뉴가 이미 장바구니에 담겨 있습니다. " +
                "새로운 가게의 메뉴를 담으려면 기존 장바구니를 비워주세요.");
        }
    }
} 