package com.example.starter.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AccessToken 재발급 요청 DTO
 */
@Getter
@NoArgsConstructor
public class TokenRefreshRequest {

    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    private String refreshToken;
}
