package com.jeonjueats.service;

import com.jeonjueats.dto.OrderCreateRequestDto;
import com.jeonjueats.dto.OrderItemResponseDto;
import com.jeonjueats.dto.OrderListResponseDto;
import com.jeonjueats.dto.OrderResponseDto;
import com.jeonjueats.entity.*;
import com.jeonjueats.exception.CartNotFoundException;
import com.jeonjueats.exception.InvalidCartOperationException;
import com.jeonjueats.exception.MenuNotFoundException;
import com.jeonjueats.exception.OrderNotFoundException;
import com.jeonjueats.exception.StoreNotFoundException;
import com.jeonjueats.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 서비스
 * 주문 생성, 조회 등 주문 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    // MVP 고정값: 배달비
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("3000");

    /**
     * 주문 생성
     * 장바구니 내용을 기반으로 주문을 생성하고, 주문 완료 후 장바구니를 비웁니다.
     * 
     * @param userId 사용자 ID
     * @param request 주문 생성 요청 정보 (배달 주소, 요청사항)
     * @return 생성된 주문 정보
     */
    @Transactional
    public OrderResponseDto createOrder(Long userId, OrderCreateRequestDto request) {
        log.info("주문 생성 시작 - 사용자 ID: {}", userId);

        // 1. 사용자 장바구니 조회
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("장바구니를 찾을 수 없습니다."));

        if (cart.isEmpty()) {
            throw new InvalidCartOperationException("장바구니가 비어있어 주문할 수 없습니다.");
        }

        // 2. 장바구니 아이템들 조회
        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByCreatedAtDesc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new InvalidCartOperationException("장바구니가 비어있어 주문할 수 없습니다.");
        }

        // 3. 가게 정보 조회
        Store store = storeRepository.findByIdAndIsDeletedFalse(cart.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("가게를 찾을 수 없습니다."));

        // 4. 주문 금액 계산
        OrderCalculation calculation = calculateOrderTotals(cartItems);

        log.info("주문 금액 계산 완료 - 상품 총액: {}, 배달비: {}, 총 금액: {}", 
                calculation.subtotalAmount, DELIVERY_FEE, calculation.totalPrice);

        // 5. 주문 엔티티 생성
        Orders order = new Orders(
                userId,
                store.getId(),
                calculation.subtotalAmount,
                DELIVERY_FEE,
                request.getDeliveryZipcode(),
                request.getDeliveryAddress1(),
                request.getDeliveryAddress2()
        );

        if (request.getRequests() != null && !request.getRequests().trim().isEmpty()) {
            order.updateRequests(request.getRequests().trim());
        }

        order = ordersRepository.save(order);
        log.info("주문 생성 완료 - 주문 ID: {}, 총 금액: {}", order.getId(), order.getTotalPrice());

        // 6. 주문 아이템들 생성
        List<OrderItem> orderItems = createOrderItems(order.getId(), cartItems);
        log.info("주문 아이템 생성 완료 - 아이템 수: {}", orderItems.size());

        // 7. 장바구니 비우기 (트랜잭션으로 보장)
        clearUserCart(userId, cart);
        log.info("장바구니 정리 완료 - 사용자 ID: {}", userId);

        // 8. 응답 DTO 생성
        OrderResponseDto response = convertToOrderResponseDto(order, store, orderItems);
        
        log.info("주문 생성 프로세스 완료 - 주문 ID: {}, 사용자 ID: {}, 가게: {}", 
                order.getId(), userId, store.getName());

        return response;
    }

    /**
     * 장바구니 아이템들의 총 금액 계산
     */
    private OrderCalculation calculateOrderTotals(List<CartItem> cartItems) {
        BigDecimal subtotalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (CartItem cartItem : cartItems) {
            Menu menu = menuRepository.findByIdAndIsDeletedFalse(cartItem.getMenuId())
                    .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다. ID: " + cartItem.getMenuId()));

            BigDecimal itemTotal = menu.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotalAmount = subtotalAmount.add(itemTotal);
            totalQuantity += cartItem.getQuantity();
        }

        BigDecimal totalPrice = subtotalAmount.add(DELIVERY_FEE);

        return new OrderCalculation(subtotalAmount, DELIVERY_FEE, totalPrice, totalQuantity);
    }

    /**
     * 주문 아이템들 생성
     */
    private List<OrderItem> createOrderItems(Long orderId, List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> {
                    Menu menu = menuRepository.findByIdAndIsDeletedFalse(cartItem.getMenuId())
                            .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다. ID: " + cartItem.getMenuId()));

                    OrderItem orderItem = new OrderItem(
                            orderId,
                            menu.getId(),
                            cartItem.getQuantity(),
                            menu.getPrice() // 주문 당시 가격으로 기록
                    );

                    return orderItemRepository.save(orderItem);
                })
                .collect(Collectors.toList());
    }

    /**
     * 사용자 장바구니 완전 정리
     */
    private void clearUserCart(Long userId, Cart cart) {
        // 1. 장바구니 아이템들 삭제
        cartItemRepository.deleteByCartId(cart.getId());
        
        // 2. 장바구니 storeId 초기화
        cart.setStoreId(null);
        cartRepository.save(cart);
        
        log.info("장바구니 정리 완료 - 사용자 ID: {}, 장바구니 ID: {}", userId, cart.getId());
    }

    /**
     * 주문 정보를 응답 DTO로 변환
     */
    private OrderResponseDto convertToOrderResponseDto(Orders order, Store store, List<OrderItem> orderItems) {
        List<OrderResponseDto.OrderItemResponseDto> orderItemDtos = orderItems.stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList());

        int totalQuantity = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        String fullAddress = buildFullAddress(order.getDeliveryZipcode(), 
                order.getDeliveryAddress1(), order.getDeliveryAddress2());

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .storeId(store.getId())
                .storeName(store.getName())
                .storeImageUrl(store.getStoreImageUrl())
                .status(order.getStatus())
                .statusDisplayName(getStatusDisplayName(order.getStatus()))
                .subtotalAmount(order.getSubtotalAmount())
                .deliveryFee(order.getDeliveryFeeAtOrder())
                .totalPrice(order.getTotalPrice())
                .deliveryZipcode(order.getDeliveryZipcode())
                .deliveryAddress1(order.getDeliveryAddress1())
                .deliveryAddress2(order.getDeliveryAddress2())
                .fullDeliveryAddress(fullAddress)
                .requests(order.getRequests())
                .paymentMethod(order.getPaymentMethod())
                .paymentTransactionId(order.getPaymentTransactionId())
                .orderItems(orderItemDtos)
                .totalItemCount(orderItemDtos.size())
                .totalQuantity(totalQuantity)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * 주문 아이템을 DTO로 변환
     */
    private OrderResponseDto.OrderItemResponseDto convertToOrderItemDto(OrderItem orderItem) {
        Menu menu = menuRepository.findByIdAndIsDeletedFalse(orderItem.getMenuId())
                .orElseThrow(() -> new MenuNotFoundException("메뉴를 찾을 수 없습니다. ID: " + orderItem.getMenuId()));

        return OrderResponseDto.OrderItemResponseDto.builder()
                .orderItemId(orderItem.getId())
                .menuId(menu.getId())
                .menuName(menu.getName())
                .menuDescription(menu.getDescription())
                .menuImageUrl(menu.getMenuImageUrl())
                .quantity(orderItem.getQuantity())
                .priceAtOrder(orderItem.getPriceAtOrder())
                .itemTotalPrice(orderItem.getTotalPrice())
                .build();
    }

    /**
     * 사용자의 주문 목록 조회 (페이징)
     */
    public Page<OrderListResponseDto> getMyOrders(Long userId, Pageable pageable) {
        log.info("사용자 주문 목록 조회 - 사용자 ID: {}, 페이지: {}", userId, pageable.getPageNumber());
        
        Page<Orders> ordersPage = ordersRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return ordersPage.map(this::convertToListResponseDto);
    }
    
    /**
     * 주문 상세 조회
     * 사용자의 소유권을 검증하여 자신의 주문만 조회 가능
     */
    public OrderResponseDto getMyOrderDetail(Long userId, Long orderId) {
        log.info("주문 상세 조회 - 사용자 ID: {}, 주문 ID: {}", userId, orderId);
        
        Orders order = ordersRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없거나 접근 권한이 없습니다."));
        
        return buildOrderResponseDto(order);
    }
    
    /**
     * Orders 엔티티를 OrderListResponseDto로 변환
     */
    private OrderListResponseDto convertToListResponseDto(Orders order) {
        // 주문 아이템 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());
        
        // 가게 정보 조회
        Store store = storeRepository.findById(order.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("가게를 찾을 수 없습니다."));
        
        // 대표 메뉴 이름 구성 (첫 번째 메뉴 + 외 X건)
        String representativeMenuName = "";
        int totalMenuCount = orderItems.size();
        
        if (!orderItems.isEmpty()) {
            // 첫 번째 메뉴 정보 조회
            Menu firstMenu = menuRepository.findById(orderItems.get(0).getMenuId())
                    .orElse(null);
            String firstMenuName = (firstMenu != null) ? firstMenu.getName() : "메뉴";
            
            if (totalMenuCount == 1) {
                representativeMenuName = firstMenuName;
            } else {
                representativeMenuName = firstMenuName + " 외 " + (totalMenuCount - 1) + "건";
            }
        }
        
        // 총 수량 계산
        int totalQuantity = orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
        
        return OrderListResponseDto.builder()
                .orderId(order.getId())
                .storeId(order.getStoreId())
                .storeName(store.getName())
                .storeImageUrl(store.getStoreImageUrl())
                .status(order.getStatus())
                .statusDisplayName(getStatusDisplayName(order.getStatus()))
                .totalPrice(order.getTotalPrice())
                .representativeMenuName(representativeMenuName)
                .totalMenuCount(totalMenuCount)
                .orderedAt(order.getCreatedAt())
                .totalQuantity(totalQuantity)
                .build();
    }
    
    /**
     * Orders 엔티티를 OrderResponseDto로 변환 (상세 조회용)
     */
    private OrderResponseDto buildOrderResponseDto(Orders order) {
        // 주문 아이템 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());
        
        // 가게 정보 조회
        Store store = storeRepository.findById(order.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("가게를 찾을 수 없습니다."));
        
        // 주문 아이템 DTO 변환
        List<OrderResponseDto.OrderItemResponseDto> orderItemDtos = orderItems.stream()
                .map(orderItem -> {
                    // 메뉴 정보 조회
                    Menu menu = menuRepository.findById(orderItem.getMenuId())
                            .orElse(null);
                    String menuName = (menu != null) ? menu.getName() : "메뉴";
                    String menuImageUrl = (menu != null) ? menu.getMenuImageUrl() : null;
                    String menuDescription = (menu != null) ? menu.getDescription() : null;
                    
                    return OrderResponseDto.OrderItemResponseDto.builder()
                            .orderItemId(orderItem.getId())
                            .menuId(orderItem.getMenuId())
                            .menuName(menuName)
                            .menuDescription(menuDescription)
                            .menuImageUrl(menuImageUrl)
                            .quantity(orderItem.getQuantity())
                            .priceAtOrder(orderItem.getPriceAtOrder())
                            .itemTotalPrice(orderItem.getTotalPrice())
                            .build();
                })
                .collect(Collectors.toList());
        
        // 전체 배달 주소 조합
        String fullDeliveryAddress = order.getDeliveryAddress1();
        if (order.getDeliveryAddress2() != null && !order.getDeliveryAddress2().trim().isEmpty()) {
            fullDeliveryAddress += " " + order.getDeliveryAddress2();
        }
        
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .storeId(order.getStoreId())
                .storeName(store.getName())
                .storeImageUrl(store.getStoreImageUrl())
                .status(order.getStatus())
                .statusDisplayName(getStatusDisplayName(order.getStatus()))
                .subtotalAmount(order.getSubtotalAmount())
                .deliveryFee(order.getDeliveryFeeAtOrder())
                .totalPrice(order.getTotalPrice())
                .deliveryZipcode(order.getDeliveryZipcode())
                .deliveryAddress1(order.getDeliveryAddress1())
                .deliveryAddress2(order.getDeliveryAddress2())
                .fullDeliveryAddress(fullDeliveryAddress)
                .requests(order.getRequests())
                .paymentMethod(order.getPaymentMethod())
                .orderItems(orderItemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * 주문 상태 표시명 반환
     */
    private String getStatusDisplayName(OrderStatus status) {
        switch (status) {
            case PENDING: return "주문 대기중";
            case ACCEPTED: return "주문 접수";
            case DELIVERING: return "배달중";
            case COMPLETED: return "배달 완료";
            case REJECTED: return "주문 거절";
            default: return status.name();
        }
    }

    /**
     * 전체 배달 주소 문자열 생성
     */
    private String buildFullAddress(String zipcode, String address1, String address2) {
        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append("(").append(zipcode).append(") ");
        fullAddress.append(address1);
        
        if (address2 != null && !address2.trim().isEmpty()) {
            fullAddress.append(" ").append(address2.trim());
        }
        
        return fullAddress.toString();
    }

    /**
     * 주문 계산 결과를 담는 내부 클래스
     */
    private static class OrderCalculation {
        final BigDecimal subtotalAmount;
        final BigDecimal deliveryFee;
        final BigDecimal totalPrice;
        final int totalQuantity;

        OrderCalculation(BigDecimal subtotalAmount, BigDecimal deliveryFee, BigDecimal totalPrice, int totalQuantity) {
            this.subtotalAmount = subtotalAmount;
            this.deliveryFee = deliveryFee;
            this.totalPrice = totalPrice;
            this.totalQuantity = totalQuantity;
        }
    }
} 