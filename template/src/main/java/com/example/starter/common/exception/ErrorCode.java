package com.example.starter.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 전역 에러 코드 정의
 * - HTTP 상태, 에러 식별 코드, 사용자 메시지를 하나의 enum으로 관리
 * - 신규 도메인 에러는 이 enum에 추가하거나 도메인별 하위 enum을 별도로 만들어 사용
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ==================== 공통 ====================
    COMMON_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 내부 오류가 발생했습니다."),
    COMMON_INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,            "COMMON-002", "입력값이 올바르지 않습니다."),
    COMMON_INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,             "COMMON-003", "요청 파라미터 타입이 올바르지 않습니다."),
    COMMON_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,      "COMMON-004", "지원하지 않는 HTTP 메서드입니다."),

    // ==================== 인증/인가 (AUTH) ====================
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED,              "AUTH-001", "인증이 필요합니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN,                    "AUTH-002", "접근 권한이 없습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED,             "AUTH-003", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,             "AUTH-004", "만료된 토큰입니다."),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,     "AUTH-005", "유효하지 않은 리프레시 토큰입니다."),
    AUTH_NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND,             "AUTH-006", "사용자를 찾을 수 없습니다."),
    AUTH_UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-007", "지원하지 않는 OAuth2 제공자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
