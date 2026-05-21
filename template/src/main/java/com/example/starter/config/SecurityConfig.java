package com.example.starter.config;

import com.example.starter.auth.jwt.JwtAuthenticationFilter;
import com.example.starter.auth.jwt.JwtProvider;
import com.example.starter.auth.oauth.CustomOAuth2UserService;
import com.example.starter.auth.oauth.OAuth2AuthenticationFailureHandler;
import com.example.starter.auth.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 * - 세션 정책: Stateless (JWT 기반, 서버에 세션 저장하지 않음)
 * - JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
 * - OAuth2 소셜 로그인 (Google, Naver, Kakao) 설정
 *
 * [UserDetailsService 구현 안내]
 * 실제 프로젝트에서는 UserRepository를 사용하여 UserDetailsService를 구현하고
 * @Service로 등록하면 Spring Security가 자동으로 주입합니다.
 * 예시: MemberDetailsService implements UserDetailsService
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // JWT 사용 시 CSRF 불필요 (세션 없음)
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless — JWT 기반이므로 서버에 세션 저장하지 않음
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 요청별 인가 규칙
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 엔드포인트
                .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/h2-console/**").permitAll()   // 로컬 H2 콘솔 (프로덕션에서 제거)
                // Swagger / OpenAPI — 로컬 환경에서만 활성화되므로 경로는 항상 허용
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs").permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // H2 콘솔 iframe 허용 (로컬 개발 전용)
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin())
            )

            // OAuth2 소셜 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(customOAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )

            // JWT 인증 필터 등록 — UsernamePasswordAuthenticationFilter 앞에서 실행
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
