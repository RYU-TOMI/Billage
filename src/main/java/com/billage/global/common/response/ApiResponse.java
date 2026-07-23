package com.billage.global.common.response;

import com.billage.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 모든 API 응답의 공통 포맷.
 *
 * <pre>
 * 성공: { "success": true,  "data": { ... }, "error": null }
 * 실패: { "success": false, "data": null,    "error": { "code": "...", "message": "..." } }
 * </pre>
 */
@Getter
@JsonPropertyOrder({"success", "data", "error"})
@JsonInclude(JsonInclude.Include.ALWAYS)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorDetail error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, new ErrorDetail(errorCode.name(), errorCode.getMessage()));
    }

    /** 기본 메시지 대신 상황에 맞는 메시지를 내려주고 싶을 때 사용합니다. */
    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(errorCode.name(), message));
    }

    public record ErrorDetail(String code, String message) {
    }
}
