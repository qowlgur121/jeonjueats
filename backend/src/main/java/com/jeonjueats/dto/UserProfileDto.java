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
    private String defaultZipcode;
    private String defaultAddress1;
    private String defaultAddress2;
    
    public static UserProfileDto of(Long userId, String email, String nickname, String role,
                                    String defaultZipcode, String defaultAddress1, String defaultAddress2) {
        return new UserProfileDto(userId, email, nickname, role, defaultZipcode, defaultAddress1, defaultAddress2);
    }
} 