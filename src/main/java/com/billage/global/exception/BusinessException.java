package com.billage.global.exception;

import lombok.Getter;

/**
 * 비즈니스 로직에서 던지는 예외.
 * 서비스 계층에서 {@code throw new BusinessException(ErrorCode.POST_NOT_FOUND)} 형태로 사용하면
 * {@link GlobalExceptionHandler} 가 공통 에러 응답으로 변환합니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** 기본 메시지 대신 상황에 맞는 메시지를 내려주고 싶을 때 사용합니다. */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
