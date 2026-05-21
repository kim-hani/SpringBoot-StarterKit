package com.example.starter.auth.service;

import com.example.starter.common.exception.ErrorCode;
import com.example.starter.domain.member.entity.Member;
import com.example.starter.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 구현체
 * - SecurityConfig의 @RequiredArgsConstructor가 이 Bean을 UserDetailsService 타입으로 자동 주입
 * - JwtAuthenticationFilter와 AuthenticationManager 모두 이 서비스를 통해 사용자 정보를 조회
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 이메일(JWT subject)로 회원을 조회하여 UserDetails 반환
     * - JWT 필터: 토큰 subject(이메일)로 인증 객체 생성 시 호출
     * - 로그인: AuthenticationManager.authenticate() 내부에서 비밀번호 검증 시 호출
     *
     * @throws UsernameNotFoundException 이메일에 해당하는 회원이 없을 때
     *         — Spring Security가 내부적으로 BadCredentialsException으로 변환하여 로그인 실패 처리
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        ErrorCode.AUTH_NOT_FOUND_MEMBER.getMessage()
                ));

        // authorities(): Enum 값(ROLE_USER, ROLE_ADMIN)을 그대로 사용
        // roles() 대신 사용하는 이유 — roles()는 "ROLE_" 접두사를 자동으로 추가하므로
        // 이미 "ROLE_" 가 붙은 Enum 값을 넘기면 "ROLE_ROLE_USER"가 되는 버그 발생
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .authorities(member.getRole().name())
                .build();
    }
}
