package com.example.starter.auth.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RefreshToken 엔티티
 * - 사용자별 RefreshToken을 DB에 저장하여 토큰 재발급/로그아웃 시 검증에 활용
 * - BaseEntity를 상속하지 않는 이유: JPA Auditing 감사 필드가 불필요한 단순 Key-Value 구조
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 식별자 (이메일 또는 소셜 provider+providerId) */
    @Column(nullable = false, unique = true)
    private String subject;

    /** RefreshToken 값 */
    @Column(nullable = false, length = 512)
    private String token;

    @Builder
    public RefreshToken(String subject, String token) {
        this.subject = subject;
        this.token = token;
    }

    /**
     * RefreshToken 갱신
     * - 로그인 재시도 시 기존 레코드를 업데이트하여 중복 저장 방지
     */
    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
