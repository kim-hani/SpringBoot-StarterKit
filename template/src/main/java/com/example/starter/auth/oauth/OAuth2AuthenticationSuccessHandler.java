package com.example.starter.auth.oauth;

import com.example.starter.auth.dto.TokenResponse;
import com.example.starter.auth.entity.RefreshToken;
import com.example.starter.auth.jwt.JwtProvider;
import com.example.starter.auth.repository.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * OAuth2 로그인 성공 후 JWT 토큰을 발급하여 JSON으로 응답하는 핸들러
 * - AccessToken + RefreshToken 생성 후 RefreshToken을 DB에 저장/갱신
 *
 * [실제 프로젝트 SPA/모바일 적용 방법]
 * 프론트엔드가 별도 도메인인 경우 JSON 응답 대신 redirect-uri에 토큰을 쿼리 파라미터로 담아
 * 리다이렉트하는 방식을 사용하는 것이 일반적
 * 예시: getRedirectStrategy().sendRedirect(request, response, frontendUri + "?token=" + accessToken);
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String subject = extractSubject(oAuth2User);

        String accessToken  = jwtProvider.generateAccessToken(subject);
        String refreshToken = jwtProvider.generateRefreshToken(subject);

        // RefreshToken DB 저장 (기존 레코드 있으면 갱신, 없으면 신규 저장)
        refreshTokenRepository.findBySubject(subject)
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder().subject(subject).token(refreshToken).build()
                        )
                );

        log.debug("OAuth2 로그인 성공 — subject: {}", subject);

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(accessTokenExpiration)
                .build();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
    }

    /**
     * 제공자별 응답 구조에서 사용자 식별자(이메일) 추출
     * - Google: 최상위 email
     * - Naver: response.email
     * - Kakao: kakao_account.email, 없으면 id
     */
    @SuppressWarnings("unchecked")
    private String extractSubject(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }
        if (attributes.containsKey("response")) {
            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
            if (naverResponse.containsKey("email")) {
                return (String) naverResponse.get("email");
            }
        }
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                return (String) kakaoAccount.get("email");
            }
        }
        // 이메일을 가져올 수 없는 경우 id를 subject로 사용
        return attributes.getOrDefault("sub", attributes.get("id")).toString();
    }
}
