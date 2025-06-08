package com.jeonjueats.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장바구니 아이템 수량 변경 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CartItemUpdateRequestDto {
    
    /**
     * 변경할 수량 (1 이상)
     */
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Integer quantity;
} 