package com.example.starter.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3.0 설정
 * - Bearer JWT 인증 스키마 등록 (Authorization 헤더에 'Bearer {token}' 형식으로 전달)
 * - 전역 SecurityRequirement 적용 — 모든 API에 자물쇠 아이콘 표시
 * - Swagger UI는 로컬 환경에서만 활성화 (application-local.yml 참고)
 *
 * [접근 경로]
 * Swagger UI : http://localhost:8080/swagger-ui/index.html
 * API Docs   : http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_TOKEN_PREFIX = "bearer-jwt";

    /**
     * OpenAPI 메타정보와 Bearer JWT 보안 스키마를 등록한다.
     * - SecurityScheme: HTTP Bearer 방식, JWT 포맷 명시
     * - 전역 SecurityRequirement: 인증이 필요한 API에 자물쇠 아이콘 자동 적용
     */
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("AccessToken을 입력하세요. 예시: Bearer {accessToken}");

        SecurityRequirement globalSecurityRequirement = new SecurityRequirement()
                .addList(BEARER_TOKEN_PREFIX);

        return new OpenAPI()
                .info(apiInfo())
                .components(new Components()
                        .addSecuritySchemes(BEARER_TOKEN_PREFIX, bearerAuthScheme))
                .addSecurityItem(globalSecurityRequirement);
    }

    /**
     * API 문서 기본 정보를 설정한다.
     * init 스크립트로 새 프로젝트 생성 시 title·description·version을 프로젝트에 맞게 수정하세요.
     */
    private Info apiInfo() {
        return new Info()
                .title("Spring Boot Starter Kit API")
                .description("""
                        Spring Boot Starter Kit 기반 프로젝트 API 문서입니다.

                        **인증 방법**
                        1. `POST /auth/login` 으로 로그인하여 AccessToken을 발급받으세요.
                        2. 우측 상단 **Authorize** 버튼 클릭 후 `Bearer {accessToken}` 형식으로 입력하세요.
                        3. 이후 인증이 필요한 API를 직접 테스트할 수 있습니다.
                        """)
                .version("v1.0.0");
    }
}
