package com.example.starter.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 발급/재발급 응답 DTO
 * - 로그인 성공 또는 AccessToken 재발급 시 반환
 */
@Getter
@Builder
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;

    /** 토큰 타입 — 항상 "Bearer" */
    private final String tokenType;

    /** AccessToken 만료 시간 (ms) */
    private final long accessTokenExpiresIn;
}
