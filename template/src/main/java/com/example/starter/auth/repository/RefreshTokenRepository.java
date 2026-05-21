package com.example.starter.auth.repository;

import com.example.starter.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * RefreshToken 레포지토리
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findBySubject(String subject);

    Optional<RefreshToken> findByToken(String token);

    void deleteBySubject(String subject);
}
