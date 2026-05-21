package com.example.starter.common.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반 시 던지는 최상위 예외
 * - Controller의 try-catch 없이 GlobalExceptionHandler가 일괄 처리
 * - 도메인별 예외는 이 클래스를 상속하거나 직접 사용
 *   예) throw new BusinessException(ErrorCode.COMMON_INVALID_INPUT_VALUE)
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드 메시지를 커스텀 메시지로 재정의할 때 사용
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
