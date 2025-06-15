package com.jeonjueats.service;

import com.jeonjueats.dto.OwnerOrderListResponseDto;
import com.jeonjueats.dto.OrderResponseDto;
import com.jeonjueats.dto.OrderStatusUpdateRequestDto;
import com.jeonjueats.entity.*;
import com.jeonjueats.exception.OrderNotFoundException;
import com.jeonjueats.exception.StoreNotFoundException;
import com.jeonjueats.exception.UnauthorizedAccessException;
import com.jeonjueats.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 사장님용 주문 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OwnerOrderService {

    private final OrdersRepository ordersRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;

    /**
     * 가게별 주문 목록 조회 (상태별 필터링 지원)
     */
    public Page<OwnerOrderListResponseDto> getStoreOrders(Long storeId, OrderStatus status, Long ownerId, Pageable pageable) {
        // 가게 소유권 검증
        validateStoreOwnership(storeId, ownerId);

        // 상태별 필터링하여 주문 조회
        Page<Orders> orders;
        if (status != null) {
            orders = ordersRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(storeId, status, pageable);
        } else {
            orders = ordersRepository.findByStoreIdOrderByCreatedAtDesc(storeId, pageable);
        }

        // 사용자 정보 배치 조회
        List<Long> userIds = orders.getContent().stream()
                .map(Orders::getUserId)
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 각 주문별 주문 아이템 및 메뉴 정보 조회 (간단한 요약 정보만)
        return orders.map(order -> buildOwnerOrderListResponseDto(order, userMap.get(order.getUserId())));
    }

    /**
     * 가게별 특정 주문 상세 조회
     */
    public OrderResponseDto getStoreOrderDetail(Long storeId, Long orderId, Long ownerId) {
        // 가게 소유권 검증
        validateStoreOwnership(storeId, ownerId);

        // 주문 조회 (가게 ID로 권한 체크)
        Orders order = ordersRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없거나 접근 권한이 없습니다."));

        // 기존 OrderService의 buildOrderResponseDto 메서드 재사용
        return buildOrderResponseDto(order);
    }

    /**
     * 가게 소유권 검증
     */
    private void validateStoreOwnership(Long storeId, Long ownerId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("가게를 찾을 수 없습니다."));

        if (!store.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedAccessException("해당 가게에 대한 접근 권한이 없습니다.");
        }
    }

    /**
     * 사장님용 주문 목록 응답 DTO 빌드
     */
    private OwnerOrderListResponseDto buildOwnerOrderListResponseDto(Orders order, User user) {
        // 주문 아이템들 조회하여 대표 메뉴명과 총 개수 계산
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());
        
        // 대표 메뉴명 생성 (첫 번째 메뉴명 + "외 N건" 형태)
        String representativeMenuName = "";
        int totalMenuCount = orderItems.size();
        int totalQuantity = orderItems.stream().mapToInt(OrderItem::getQuantity).sum();

        if (!orderItems.isEmpty()) {
            OrderItem firstItem = orderItems.get(0);
            Menu firstMenu = menuRepository.findById(firstItem.getMenuId())
                    .orElse(null);
            
            if (firstMenu != null) {
                if (totalMenuCount > 1) {
                    representativeMenuName = firstMenu.getName() + " 외 " + (totalMenuCount - 1) + "건";
                } else {
                    representativeMenuName = firstMenu.getName();
                }
            }
        }

        return OwnerOrderListResponseDto.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .userNickname(user != null ? user.getNickname() : "알 수 없음")
                .status(order.getStatus())
                .statusDisplayName(order.getStatus().getDescription())
                .totalPrice(order.getTotalPrice())
                .subtotalAmount(order.getSubtotalAmount())
                .deliveryFeeAtOrder(order.getDeliveryFeeAtOrder())
                .representativeMenuName(representativeMenuName)
                .totalMenuCount(totalMenuCount)
                .totalQuantity(totalQuantity)
                .deliveryAddress1(order.getDeliveryAddress1())
                .deliveryAddress2(order.getDeliveryAddress2())
                .orderedAt(order.getCreatedAt())
                .requests(order.getRequests())
                .build();
    }

    /**
     * 주문 상세 응답 DTO 빌드 (기존 OrderService와 동일한 로직)
     */
    private OrderResponseDto buildOrderResponseDto(Orders order) {
        // 가게 정보 조회
        Store store = storeRepository.findById(order.getStoreId())
                .orElseThrow(() -> new StoreNotFoundException("가게 정보를 찾을 수 없습니다."));
        
        // 사용자 정보 조회
        User user = userRepository.findById(order.getUserId())
                .orElse(null);

        // 주문 아이템들 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());

        // 메뉴 정보 배치 조회
        List<Long> menuIds = orderItems.stream()
                .map(OrderItem::getMenuId)
                .distinct()
                .toList();

        Map<Long, Menu> menuMap = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        // 주문 아이템 DTO 변환
        List<OrderResponseDto.OrderItemResponseDto> orderItemDtos = orderItems.stream()
                .map(item -> {
                    Menu menu = menuMap.get(item.getMenuId());
                    return OrderResponseDto.OrderItemResponseDto.builder()
                            .orderItemId(item.getId())
                            .menuId(item.getMenuId())
                            .menuName(menu != null ? menu.getName() : "알 수 없는 메뉴")
                            .menuDescription(menu != null ? menu.getDescription() : "")
                            .menuImageUrl(menu != null ? menu.getMenuImageUrl() : "")
                            .quantity(item.getQuantity())
                            .priceAtOrder(item.getPriceAtOrder())
                            .itemTotalPrice(item.getPriceAtOrder().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                            .build();
                })
                .toList();

        // 배달 주소 합치기
        String fullDeliveryAddress = order.getDeliveryAddress1();
        if (order.getDeliveryAddress2() != null && !order.getDeliveryAddress2().trim().isEmpty()) {
            fullDeliveryAddress += " " + order.getDeliveryAddress2();
        }

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .userNickname(user != null ? user.getNickname() : "탈퇴한 사용자")
                .storeId(order.getStoreId())
                .storeName(store.getName())
                .storeImageUrl(store.getStoreImageUrl())
                .status(order.getStatus())
                .statusDisplayName(order.getStatus().getDescription())
                .subtotalAmount(order.getSubtotalAmount())
                .deliveryFee(order.getDeliveryFeeAtOrder())
                .totalPrice(order.getTotalPrice())
                .deliveryZipcode(order.getDeliveryZipcode())
                .deliveryAddress1(order.getDeliveryAddress1())
                .deliveryAddress2(order.getDeliveryAddress2())
                .fullDeliveryAddress(fullDeliveryAddress)
                .requests(order.getRequests())
                .paymentMethod(order.getPaymentMethod())
                .paymentTransactionId(order.getPaymentTransactionId())
                .orderItems(orderItemDtos)
                .totalItemCount(orderItemDtos.size())
                .totalQuantity(orderItemDtos.stream().mapToInt(OrderResponseDto.OrderItemResponseDto::getQuantity).sum())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * 사장님용 주문 상태 변경
     */
    @Transactional
    public OrderResponseDto updateOrderStatus(Long storeId, Long orderId, Long ownerId, 
                                            OrderStatusUpdateRequestDto request) {
        // 가게 소유권 검증
        validateStoreOwnership(storeId, ownerId);

        // 주문 조회 (가게 ID로 권한 체크)
        Orders order = ordersRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없거나 접근 권한이 없습니다."));

        // 상태 전이 검증
        validateStatusTransition(order.getStatus(), request.getNewStatus());

        // 상태 변경
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getNewStatus());
        ordersRepository.save(order);

        log.info("주문 상태 변경 완료 - orderId: {}, oldStatus: {}, newStatus: {}", 
                orderId, oldStatus, request.getNewStatus());

        // 변경된 주문 상세 정보 반환
        return getStoreOrderDetail(storeId, orderId, ownerId);
    }

    /**
     * 주문 상태 전이 검증
     * 잘못된 상태 변경 요청을 차단
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // 동일한 상태로 변경 시도
        if (currentStatus == newStatus) {
            throw new IllegalArgumentException("현재 상태와 동일한 상태로 변경할 수 없습니다.");
        }

        // 상태별 가능한 전이 검증
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.ACCEPTED && 
                    newStatus != OrderStatus.REJECTED) {
                    throw new IllegalArgumentException("대기 중 상태에서는 수락 또는 거절만 가능합니다.");
                }
                break;
            case ACCEPTED:
                if (newStatus != OrderStatus.DELIVERING) {
                    throw new IllegalArgumentException("수락 상태에서는 배달 중으로만 변경 가능합니다.");
                }
                break;
            case DELIVERING:
                if (newStatus != OrderStatus.COMPLETED) {
                    throw new IllegalArgumentException("배달 중 상태에서는 배달 완료로만 변경 가능합니다.");
                }
                break;
            case COMPLETED:
            case REJECTED:
                throw new IllegalArgumentException("완료된 주문의 상태는 변경할 수 없습니다.");
            default:
                throw new IllegalArgumentException("지원되지 않는 주문 상태입니다.");
        }
    }
} 