package com.jeonjueats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderCreateRequestDto {

    @NotBlank(message = "우편번호는 필수입니다")
    @Size(max = 10, message = "우편번호는 10자 이내여야 합니다")
    private String deliveryZipcode;

    @NotBlank(message = "기본 주소는 필수입니다")
    @Size(max = 255, message = "기본 주소는 255자 이내여야 합니다")
    private String deliveryAddress1;

    @Size(max = 255, message = "상세 주소는 255자 이내여야 합니다")
    private String deliveryAddress2;

    @Size(max = 500, message = "요청사항은 500자 이내여야 합니다")
    private String requests;
} 