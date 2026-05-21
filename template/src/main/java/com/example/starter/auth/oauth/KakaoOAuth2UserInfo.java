package com.example.starter.auth.oauth;

import java.util.Map;

/**
 * Kakao OAuth2 사용자 정보
 * - 응답 구조: { id, kakao_account: { email, profile: { nickname, profile_image_url, ... } } }
 * - application-*.yml에서 user-name-attribute: id 로 설정했기 때문에
 *   attributes는 응답 전체 객체를 가리킴
 */
public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) return null;
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) return null;
        return (String) profile.get("nickname");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) return null;
        return (String) kakaoAccount.get("email");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getProfileImageUrl() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) return null;
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) return null;
        return (String) profile.get("profile_image_url");
    }
}
