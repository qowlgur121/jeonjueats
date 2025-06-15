package com.jeonjueats.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequestDto {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상이어야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", 
             message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;
    
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다")
    private String nickname;
    
    @NotBlank(message = "역할은 필수입니다")
    @Pattern(regexp = "ROLE_USER|ROLE_OWNER", message = "역할은 ROLE_USER 또는 ROLE_OWNER여야 합니다")
    private String role;
    
    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phoneNumber;
    
    @NotBlank(message = "우편번호는 필수입니다")
    private String zipcode;
    
    @NotBlank(message = "기본 주소는 필수입니다")
    private String address1;
    
    @NotBlank(message = "상세 주소는 필수입니다")
    private String address2;
} 