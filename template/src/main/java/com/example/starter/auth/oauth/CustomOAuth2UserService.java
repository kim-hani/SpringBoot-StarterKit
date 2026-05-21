package com.example.starter.auth.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2 로그인 시 소셜 제공자로부터 사용자 정보를 가져와 처리하는 서비스
 * - DefaultOAuth2UserService로 제공자 API를 호출한 후 제공자별 OAuth2UserInfo로 변환
 *
 * [실제 프로젝트 적용 방법]
 * 1. UserRepository를 주입하여 소셜 회원 가입/로그인 처리를 이곳에 구현
 * 2. 반환 타입을 CustomUserDetails 등 UserDetails 구현체로 래핑하면
 *    OAuth2AuthenticationSuccessHandler에서 바로 subject 추출 가능
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());

        log.debug("OAuth2 사용자 로드 완료 — provider: {}, email: {}", registrationId, userInfo.getEmail());

        // TODO: UserRepository 주입 후 소셜 회원 가입/로그인 처리 구현
        // 예시) Member member = memberService.findOrCreateByOAuth2(userInfo, registrationId);
        //       return new CustomUserDetails(member, oAuth2User.getAttributes());

        return oAuth2User;
    }
}
