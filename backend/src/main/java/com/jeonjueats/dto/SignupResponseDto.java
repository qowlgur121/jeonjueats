package com.jeonjueats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDto {
    
    private String message;
    private Long id;
    private String email;
    private String nickname;
    private String role;
    
    public static SignupResponseDto of(Long id, String email, String nickname, String role) {
        return new SignupResponseDto(
            "회원가입이 성공적으로 완료되었습니다.",
            id,
            email,
            nickname,
            role
        );
    }
} 