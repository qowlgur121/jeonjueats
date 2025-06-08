package com.jeonjueats.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 메뉴 등록 API 요청 DTO
 * 사장님이 자신의 가게에 새로운 메뉴를 등록할 때 사용
 */
@Getter
@Setter
@NoArgsConstructor
public class MenuCreateRequestDto {

    @NotBlank(message = "메뉴명은 필수입니다")
    @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다")
    private String menuName;

    @Size(max = 1000, message = "메뉴 설명은 1000자 이하여야 합니다")
    private String description;

    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0", message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;

    @Size(max = 255, message = "메뉴 이미지 URL은 255자 이하여야 합니다")
    private String menuImageUrl;
} 