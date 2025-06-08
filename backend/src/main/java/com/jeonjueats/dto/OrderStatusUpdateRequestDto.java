package com.jeonjueats.dto;

import com.jeonjueats.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 상태 변경 요청 DTO
 * 사장님이 주문 상태를 변경할 때 사용
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusUpdateRequestDto {

    @NotNull(message = "변경할 주문 상태는 필수입니다.")
    private OrderStatus newStatus;

    public OrderStatusUpdateRequestDto(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }
} 