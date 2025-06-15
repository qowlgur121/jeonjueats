package com.jeonjueats.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 가게 정보 수정 요청 DTO
 * 사장님이 등록한 가게의 정보를 수정할 때 사용
 * 모든 필드는 선택적이며, 포함된 필드만 업데이트됨
 */
@Data
@Builder
public class StoreUpdateRequestDto {
    
    /**
     * 가게명 (선택적)
     */
    @Size(min = 2, max = 100, message = "가게명은 2자 이상 100자 이하로 입력해주세요.")
    private String name;
    
    /**
     * 우편번호 (선택적)
     */
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자로 입력해주세요.")
    private String zipcode;
    
    /**
     * 기본 주소 (선택적)
     */
    @Size(max = 255, message = "기본 주소는 255자 이하로 입력해주세요.")
    private String address1;
    
    /**
     * 상세 주소 (선택적)
     */
    @Size(max = 255, message = "상세 주소는 255자 이하로 입력해주세요.")
    private String address2;
    
    /**
     * 전화번호 (선택적)
     */
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호는 000-0000-0000 형식으로 입력해주세요.")
    private String phoneNumber;
    
    /**
     * 가게 설명 (선택적)
     */
    @Size(max = 1000, message = "가게 설명은 1000자 이하로 입력해주세요.")
    private String description;
    
    /**
     * 가게 대표 이미지 URL (선택적)
     */
    @Size(max = 255, message = "이미지 URL은 255자 이하로 입력해주세요.")
    private String storeImageUrl;
    
    /**
     * 대표 카테고리 ID (선택적)
     */
    private Long categoryId;
    
    /**
     * 최소 주문 금액 (선택적)
     */
    @DecimalMin(value = "0", message = "최소 주문 금액은 0원 이상이어야 합니다.")
    private BigDecimal minOrderAmount;
    
    /**
     * 배달팁 (선택적)
     */
    @DecimalMin(value = "0", message = "배달팁은 0원 이상이어야 합니다.")
    private BigDecimal deliveryFee;
    
    /**
     * 운영시간 (선택적)
     */
    @Size(max = 1000, message = "운영시간은 1000자 이하로 입력해주세요.")
    private String operatingHours;
} 