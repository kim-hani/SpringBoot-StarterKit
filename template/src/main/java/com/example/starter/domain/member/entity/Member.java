package com.example.starter.domain.member.entity;

import com.example.starter.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 엔티티
 * - BaseEntity 상속으로 id, createdAt, updatedAt, deletedAt 자동 포함
 * - 이메일/비밀번호 기반 로컬 로그인과 OAuth2 소셜 로그인 모두 지원
 */
@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    /** 로그인 식별자 — 이메일 중복 불허 */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * BCrypt 암호화된 비밀번호
     * - OAuth2 전용 회원은 실제 비밀번호 없이 임의 값을 저장
     */
    @Column(nullable = false)
    private String password;

    /** 회원 권한 (ROLE_USER / ROLE_ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder
    public Member(String email, String password, Role role) {
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    /**
     * 비밀번호 변경
     * - PasswordEncoder로 암호화된 값을 전달받아 갱신
     * - 직접 평문을 전달하지 않도록 서비스 레이어에서 암호화 후 호출
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 회원 권한 enum */
    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }
}
