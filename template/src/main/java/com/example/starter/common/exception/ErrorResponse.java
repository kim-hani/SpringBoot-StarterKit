package com.example.starter.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jakarta.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

/**
 * API 에러 응답 통합 DTO
 * - 단순 에러: code + message
 * - Validation 실패: code + message + errors(field, rejectedValue, reason)
 * - null 필드는 JSON에서 제외 (@JsonInclude)
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<FieldErrorDetail> errors;

    @Builder(access = AccessLevel.PRIVATE)
    private ErrorResponse(String code, String message, List<FieldErrorDetail> errors) {
        this.code = code;
        this.message = message;
        this.errors = (errors != null && !errors.isEmpty()) ? errors : null;
    }

    // ────────── 팩토리 메서드 ──────────

    /** BusinessException / 일반 예외 처리용 */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /** 커스텀 메시지 재정의가 필요한 경우 */
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .build();
    }

    /** @Valid / @Validated — MethodArgumentNotValidException 처리용 */
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        List<FieldErrorDetail> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(FieldErrorDetail::from)
                .toList();
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(fieldErrors)
                .build();
    }

    /** @Validated on class-level — ConstraintViolationException 처리용 */
    public static ErrorResponse of(ErrorCode errorCode, Set<ConstraintViolation<?>> violations) {
        List<FieldErrorDetail> fieldErrors = violations.stream()
                .map(FieldErrorDetail::from)
                .toList();
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(fieldErrors)
                .build();
    }

    // ────────── 내부 DTO ──────────

    /**
     * Validation 실패 항목 하나를 담는 DTO
     * - field: 오류 발생 필드명
     * - rejectedValue: 거부된 입력값
     * - reason: 검증 실패 메시지
     */
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldErrorDetail {

        private final String field;
        private final String rejectedValue;
        private final String reason;

        private FieldErrorDetail(String field, String rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        static FieldErrorDetail from(FieldError fieldError) {
            String rejected = fieldError.getRejectedValue() != null
                    ? fieldError.getRejectedValue().toString()
                    : null;
            return new FieldErrorDetail(
                    fieldError.getField(),
                    rejected,
                    fieldError.getDefaultMessage()
            );
        }

        static FieldErrorDetail from(ConstraintViolation<?> violation) {
            // 프로퍼티 경로의 마지막 노드만 필드명으로 사용
            String propertyPath = violation.getPropertyPath().toString();
            String field = propertyPath.contains(".")
                    ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                    : propertyPath;
            String rejected = violation.getInvalidValue() != null
                    ? violation.getInvalidValue().toString()
                    : null;
            return new FieldErrorDetail(field, rejected, violation.getMessage());
        }
    }
}
