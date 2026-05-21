package com.example.starter.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 공통 API 성공 응답 래퍼
 * - 모든 성공 응답은 이 클래스로 감싸서 반환한다.
 * - 실패 응답은 GlobalExceptionHandler가 ErrorResponse를 사용하므로 이 클래스와 역할이 겹치지 않는다.
 * - data가 null이면 JSON 직렬화 시 "data" 키 자체를 제거한다 (@JsonInclude(NON_NULL)).
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 성공 여부 — 성공 응답에서는 항상 true */
    private final boolean success;

    /** 응답 메시지 */
    private final String message;

    /** 응답 데이터 — 없는 경우 null (JSON에서 키 자체가 제거됨) */
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 데이터와 함께 반환하는 성공 응답
     * - 사용 예: GET 단건/목록 조회, POST 생성 후 결과 반환
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공했습니다.", data);
    }

    /**
     * 데이터와 커스텀 메시지를 함께 반환하는 성공 응답
     * - 사용 예: "회원가입이 완료되었습니다." 등 상황별 메시지가 필요한 경우
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * 데이터 없이 성공 여부만 반환하는 응답
     * - 사용 예: DELETE, 상태 변경 등 응답 바디에 데이터가 불필요한 경우
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, "요청이 성공했습니다.", null);
    }

    /**
     * 데이터 없이 커스텀 메시지만 반환하는 성공 응답
     * - 사용 예: "삭제되었습니다.", "비밀번호가 변경되었습니다." 등
     */
    public static ApiResponse<Void> successWithMessage(String message) {
        return new ApiResponse<>(true, message, null);
    }
}
