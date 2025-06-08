package com.jeonjueats.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장바구니 메뉴 추가/수량 변경 통합 요청 DTO
 * 단일 API 통합 처리
 */
@Getter
@Setter
@NoArgsConstructor
public class CartItemRequestDto {
    
    /**
     * 수량 변경 시 사용할 기존 장바구니 아이템 ID (선택적)
     * 존재하면 수량 변경, 없으면 메뉴 추가로 처리
     */
    private Long cartItemId;
    
    /**
     * 메뉴 추가 시 필요한 가게 ID (cartItemId가 없을 때 필수)
     */
    private Long storeId;
    
    /**
     * 메뉴 추가 시 필요한 메뉴 ID (cartItemId가 없을 때 필수)
     */
    private Long menuId;
    
    /**
     * 설정할 수량 (1 이상, 덮어쓰기 방식)
     */
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Integer quantity;
    
    /**
     * 메뉴 추가 요청인지 확인
     */
    public boolean isAddMenuRequest() {
        return cartItemId == null;
    }
    
    /**
     * 수량 변경 요청인지 확인
     */
    public boolean isUpdateQuantityRequest() {
        return cartItemId != null;
    }
} 