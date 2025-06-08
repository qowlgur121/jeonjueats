package com.jeonjueats.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 메뉴 수정 API 요청 DTO
 * 사장님이 자신의 가게 메뉴 정보를 수정할 때 사용
 * 모든 필드는 선택적이며, 제공된 필드만 업데이트됩니다
 */
@Getter
@Setter
@NoArgsConstructor
public class MenuUpdateRequestDto {

    @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다")
    private String menuName;

    @Size(max = 1000, message = "메뉴 설명은 1000자 이하여야 합니다")
    private String description;

    @DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;

    @Size(max = 255, message = "메뉴 이미지 URL은 255자 이하여야 합니다")
    private String menuImageUrl;
} 