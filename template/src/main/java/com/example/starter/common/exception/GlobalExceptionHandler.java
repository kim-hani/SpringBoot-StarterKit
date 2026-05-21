package com.example.starter.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리 핸들러
 * - Controller에서 try-catch를 사용하지 않아도 여기서 일괄 처리
 * - 예외 순서: 구체적 → 일반적 순으로 선언 (Spring이 가장 구체적인 핸들러를 우선 적용)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 규칙 위반 예외 처리
     * - Service 계층에서 throw new BusinessException(ErrorCode.XXX) 호출 시 진입
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("[BusinessException] code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    /**
     * @Valid / @Validated — @RequestBody, @ModelAttribute 유효성 검사 실패
     * - BindingResult 없이 @Valid를 사용하면 Spring이 자동으로 이 예외를 던짐
     * - errors 리스트에 field, rejectedValue, reason 포함
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("[MethodArgumentNotValidException] {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.COMMON_INVALID_INPUT_VALUE.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.COMMON_INVALID_INPUT_VALUE, e.getBindingResult()));
    }

    /**
     * @Validated 클래스 레벨 / 메서드 파라미터 제약 위반
     * - @RequestParam, @PathVariable 등 단순 파라미터 검증 실패 시 발생
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("[ConstraintViolationException] {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.COMMON_INVALID_INPUT_VALUE.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.COMMON_INVALID_INPUT_VALUE, e.getConstraintViolations()));
    }

    /**
     * 요청 파라미터 타입 불일치 예외
     * - 예: Long 타입 PathVariable에 문자열 "abc"가 넘어올 때
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("[MethodArgumentTypeMismatchException] param={}, value={}", e.getName(), e.getValue());
        return ResponseEntity
                .status(ErrorCode.COMMON_INVALID_TYPE_VALUE.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.COMMON_INVALID_TYPE_VALUE));
    }

    /**
     * 지원하지 않는 HTTP 메서드 요청
     * - 예: POST 전용 엔드포인트에 GET 요청
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("[HttpRequestMethodNotSupportedException] method={}", e.getMethod());
        return ResponseEntity
                .status(ErrorCode.COMMON_METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.COMMON_METHOD_NOT_ALLOWED));
    }

    /**
     * 그 외 모든 미처리 예외 — 최후의 보루
     * - 예상치 못한 예외가 사용자에게 스택 트레이스로 노출되는 것을 방지
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[Exception] 예상치 못한 서버 오류 발생", e);
        return ResponseEntity
                .status(ErrorCode.COMMON_INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.COMMON_INTERNAL_SERVER_ERROR));
    }
}
