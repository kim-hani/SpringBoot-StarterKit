package com.example.starter.auth.service;

import com.example.starter.auth.dto.LoginRequest;
import com.example.starter.auth.dto.TokenRefreshRequest;
import com.example.starter.auth.dto.TokenResponse;
import com.example.starter.auth.entity.RefreshToken;
import com.example.starter.auth.jwt.JwtProvider;
import com.example.starter.auth.repository.RefreshTokenRepository;
import com.example.starter.common.exception.BusinessException;
import com.example.starter.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 * - 로그인: 이메일/비밀번호 검증 후 AccessToken + RefreshToken 발급
 * - 토큰 재발급: RefreshToken 유효성 검증 후 AccessToken 재발급
 * - 로그아웃: DB에서 RefreshToken 삭제하여 재발급 불가 처리
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * 이메일/비밀번호 로그인
     * - AuthenticationManager → UserDetailsService → 비밀번호 검증
     * - 성공 시 AccessToken + RefreshToken 발급, RefreshToken DB 저장
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String subject = authentication.getName();
        String accessToken  = jwtProvider.generateAccessToken(subject);
        String refreshToken = jwtProvider.generateRefreshToken(subject);

        refreshTokenRepository.findBySubject(subject)
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder().subject(subject).token(refreshToken).build()
                        )
                );

        return buildTokenResponse(accessToken, refreshToken);
    }

    /**
     * AccessToken 재발급
     * - RefreshToken 서명/만료 검증 → DB 일치 여부 확인 → 새 AccessToken 발급
     * - RefreshToken 자체는 교체하지 않음 (만료 시간 연장 미적용)
     */
    @Transactional
    public TokenResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        jwtProvider.validateToken(refreshToken);
        String subject = jwtProvider.getSubject(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));

        // subject 일치 여부 확인 — 토큰 탈취 후 다른 사용자로 재발급 시도 방어
        if (!storedToken.getSubject().equals(subject)) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtProvider.generateAccessToken(subject);
        return buildTokenResponse(newAccessToken, refreshToken);
    }

    /**
     * 로그아웃
     * - DB에서 RefreshToken 삭제하여 해당 사용자의 재발급 요청을 차단
     */
    @Transactional
    public void logout(String subject) {
        refreshTokenRepository.deleteBySubject(subject);
    }

    private TokenResponse buildTokenResponse(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(accessTokenExpiration)
                .build();
    }
}
