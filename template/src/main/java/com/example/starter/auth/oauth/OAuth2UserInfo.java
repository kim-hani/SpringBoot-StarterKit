package com.example.starter.auth.oauth;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보 추상화
 * - Google, Naver, Kakao 각각의 응답 구조가 다르기 때문에 공통 인터페이스로 캡슐화
 * - 신규 제공자 추가 시 이 클래스를 상속하여 구현하고 OAuth2UserInfoFactory에 case를 추가
 */
public abstract class OAuth2UserInfo {

    protected final Map<String, Object> attributes;

    protected OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /** OAuth2 제공자의 사용자 고유 식별자 */
    public abstract String getProviderId();

    /** 사용자 이름 (닉네임) */
    public abstract String getName();

    /** 이메일 */
    public abstract String getEmail();

    /** 프로필 이미지 URL */
    public abstract String getProfileImageUrl();
}
