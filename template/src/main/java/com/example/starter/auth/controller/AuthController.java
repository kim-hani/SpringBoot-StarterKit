package com.example.starter.auth.controller;

import com.example.starter.auth.dto.LoginRequest;
import com.example.starter.auth.dto.TokenRefreshRequest;
import com.example.starter.auth.dto.TokenResponse;
import com.example.starter.auth.service.AuthService;
import com.example.starter.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 컨트롤러
 * - POST /auth/login   — 이메일/비밀번호 로그인 (인증 불필요)
 * - POST /auth/refresh — AccessToken 재발급 (인증 불필요)
 * - POST /auth/logout  — 로그아웃 (인증 필요)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 이메일/비밀번호 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse, "로그인이 완료되었습니다."));
    }

    /**
     * AccessToken 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        TokenResponse tokenResponse = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse, "토큰이 재발급되었습니다."));
    }

    /**
     * 로그아웃 — RefreshToken DB 삭제
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.successWithMessage("로그아웃이 완료되었습니다."));
    }
}
