package com.example.starter.auth.jwt;

import com.example.starter.common.exception.BusinessException;
import com.example.starter.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 컴포넌트
 * - AccessToken: 짧은 만료 시간(기본 30분), API 요청 인증에 사용
 * - RefreshToken: 긴 만료 시간(기본 14일), AccessToken 재발급에만 사용
 * - 서명 알고리즘: HS512 (최소 512비트 키 필요)
 */
@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        // Base64 디코딩 후 HMAC-SHA 키 생성 (HS512: 최소 512비트)
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * AccessToken 생성
     * @param subject 사용자 식별자 (이메일)
     */
    public String generateAccessToken(String subject) {
        return buildToken(subject, accessTokenExpiration);
    }

    /**
     * RefreshToken 생성
     * @param subject 사용자 식별자 (이메일)
     */
    public String generateRefreshToken(String subject) {
        return buildToken(subject, refreshTokenExpiration);
    }

    private String buildToken(String subject, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 토큰에서 subject(사용자 식별자) 추출
     */
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰 유효성 검증
     * - 만료 시 AUTH_EXPIRED_TOKEN, 그 외 형식/서명 오류 시 AUTH_INVALID_TOKEN 발생
     */
    public void validateToken(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
