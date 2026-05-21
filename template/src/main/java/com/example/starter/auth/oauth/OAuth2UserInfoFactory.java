package com.example.starter.auth.oauth;

import com.example.starter.common.exception.BusinessException;
import com.example.starter.common.exception.ErrorCode;

import java.util.Map;

/**
 * OAuth2 제공자 식별자(registrationId)에 따라 알맞은 OAuth2UserInfo 구현체를 반환하는 팩토리
 * - 신규 소셜 제공자 추가 시 이 클래스에 case 추가 + 구현 클래스 생성
 */
public class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {}

    /**
     * OAuth2 제공자별 사용자 정보 객체 생성
     * @param registrationId application-*.yml의 registration 키 (google, naver, kakao)
     * @param attributes     OAuth2 제공자로부터 수신한 사용자 정보 맵
     */
    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "naver"  -> new NaverOAuth2UserInfo(attributes);
            case "kakao"  -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new BusinessException(ErrorCode.AUTH_UNSUPPORTED_OAUTH_PROVIDER);
        };
    }
}
