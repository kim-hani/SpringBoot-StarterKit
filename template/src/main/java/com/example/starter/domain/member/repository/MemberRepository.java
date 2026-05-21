package com.example.starter.domain.member.repository;

import com.example.starter.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 Repository
 * - CustomUserDetailsService에서 이메일 기반 조회에 사용
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    /** 이메일로 회원 조회 — 존재하지 않으면 Optional.empty() 반환 */
    Optional<Member> findByEmail(String email);
}
