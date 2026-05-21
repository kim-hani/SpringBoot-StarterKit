package com.example.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot Starter Kit 메인 진입점
 * - @EnableJpaAuditing: BaseEntity의 createdAt / updatedAt 자동 주입 활성화
 */
@EnableJpaAuditing
@SpringBootApplication
public class StarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
    }
}
