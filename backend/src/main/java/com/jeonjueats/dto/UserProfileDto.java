package com.jeonjueats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    
    private Long userId;
    private String email;
    private String nickname;
    private String role;
    
    public static UserProfileDto of(Long userId, String email, String nickname, String role) {
        return new UserProfileDto(userId, email, nickname, role);
    }
} 