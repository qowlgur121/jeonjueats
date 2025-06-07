package com.jeonjueats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    
    private String message;
    private String accessToken;
    private Long userId;
    private String email;
    private String nickname;
    private String role;
    
    public static LoginResponseDto of(String accessToken, Long userId, String email, String nickname, String role) {
        return new LoginResponseDto(
            "로그인이 성공적으로 완료되었습니다.",
            accessToken,
            userId,
            email,
            nickname,
            role
        );
    }
} 