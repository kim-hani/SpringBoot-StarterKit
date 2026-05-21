package com.example.starter.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 Entity의 공통 상위 클래스
 * - @EnableJpaAuditing(StarterApplication)이 활성화되어 있어야 createdAt / updatedAt 자동 주입됨
 * - deletedAt: 소프트 딜리트 용도. 실제 삭제 시 markDeleted() 호출
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * PK — GenerationType.IDENTITY: DB의 AUTO_INCREMENT(MySQL) 또는 SERIAL(PostgreSQL) 사용
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 레코드 최초 생성 시각 — JPA Auditing이 자동 주입 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 레코드 최종 수정 시각 — JPA Auditing이 자동 주입 */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 소프트 딜리트 시각
     * - null이면 활성 레코드, 값이 있으면 삭제된 레코드
     * - 실제 DELETE 쿼리 대신 markDeleted()로 처리
     */
    @Column
    private LocalDateTime deletedAt;

    /**
     * 소프트 딜리트 실행
     * - Repository 레벨의 @Query에서 deletedAt IS NULL 조건을 함께 사용해야 함
     */
    public void markDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /** 소프트 딜리트 여부 확인 */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
