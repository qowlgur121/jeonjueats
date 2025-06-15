package com.jeonjueats.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 가게 등록 API 요청 DTO
 * 사장님이 새로운 가게를 시스템에 등록할 때 사용
 */
@Getter
@Setter
@NoArgsConstructor
public class StoreCreateRequestDto {

    @NotBlank(message = "가게명은 필수입니다")
    @Size(max = 100, message = "가게명은 100자 이하여야 합니다")
    private String name;

    @NotBlank(message = "우편번호는 필수입니다")
    @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다")
    private String zipcode;

    @NotBlank(message = "기본 주소는 필수입니다")
    @Size(max = 255, message = "기본 주소는 255자 이하여야 합니다")
    private String address1;

    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다")
    private String address2;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "\\d{2,3}-\\d{3,4}-\\d{4}", message = "전화번호 형식이 올바르지 않습니다 (예: 02-1234-5678)")
    private String phoneNumber;

    @Size(max = 1000, message = "가게 설명은 1000자 이하여야 합니다")
    private String description;

    @Size(max = 255, message = "이미지 URL은 255자 이하여야 합니다")
    private String storeImageUrl;

    @NotNull(message = "카테고리는 필수입니다")
    @Positive(message = "카테고리 ID는 양수여야 합니다")
    private Long categoryId;

    @NotNull(message = "최소 주문 금액은 필수입니다")
    @DecimalMin(value = "0", message = "최소 주문 금액은 0 이상이어야 합니다")
    private BigDecimal minOrderAmount;

    @NotNull(message = "배달비는 필수입니다")
    @DecimalMin(value = "0", message = "배달비는 0 이상이어야 합니다")
    private BigDecimal deliveryFee;

    @Size(max = 1000, message = "운영시간은 1000자 이하여야 합니다")
    private String operatingHours;
} 