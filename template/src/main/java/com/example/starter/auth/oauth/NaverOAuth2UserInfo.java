package com.example.starter.auth.oauth;

import java.util.Map;

/**
 * Naver OAuth2 사용자 정보
 * - 응답 구조: { resultcode, message, response: { id, name, email, profile_image, ... } }
 * - application-*.yml에서 user-name-attribute: response 로 설정했기 때문에
 *   attributes 자체가 response 하위 객체를 가리킴
 */
public class NaverOAuth2UserInfo extends OAuth2UserInfo {

    @SuppressWarnings("unchecked")
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        // Spring Security가 user-name-attribute(response) 키에 해당하는 값을 전달
        super((Map<String, Object>) attributes.get("response"));
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("profile_image");
    }
}
