package com.jeonjueats.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * 사용자 프로필 수정 요청 DTO
 * 닉네임과 기본 배달 주소를 수정할 때 사용
 */
@Getter
@Setter
public class UserProfileUpdateRequestDto {

    @Nullable
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    private String nickname;

    @Nullable
    @Size(max = 10, message = "우편번호는 최대 10자까지 입력 가능합니다.")
    private String defaultZipcode;

    @Nullable
    @Size(max = 255, message = "기본 주소는 최대 255자까지 입력 가능합니다.")
    private String defaultAddress1;

    @Nullable
    @Size(max = 255, message = "상세 주소는 최대 255자까지 입력 가능합니다.")
    private String defaultAddress2;

    @Nullable
    @Size(max = 20, message = "전화번호는 최대 20자까지 입력 가능합니다.")
    private String phoneNumber;
} 