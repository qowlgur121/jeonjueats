package com.jeonjueats.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장바구니 아이템 추가 요청 DTO
 * 사용자가 장바구니에 메뉴를 담을 때 사용
 */
@Getter
@Setter
@NoArgsConstructor
public class CartItemAddRequestDto {

    @NotNull(message = "메뉴 ID는 필수입니다.")
    private Long menuId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
    private Integer quantity;
} 