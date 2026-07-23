package com.billage.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전역 에러 코드.
 * 코드 문자열은 enum 이름을 그대로 사용합니다. (예: POST_NOT_FOUND)
 * 새 도메인을 만들면 아래 구분선에 맞춰 자기 도메인 블록에 추가하세요.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ===== 공통 =====
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),

    // ===== Auth =====
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),

    // ===== User =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INSUFFICIENT_CREDIT(HttpStatus.BAD_REQUEST, "크레딧 잔액이 부족합니다."),
    INVALID_CREDIT_AMOUNT(HttpStatus.BAD_REQUEST, "크레딧 금액은 0보다 커야 합니다."),

    // ===== Post (대여 · 공동구매 공통) =====
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    NOT_POST_WRITER(HttpStatus.FORBIDDEN, "게시글 작성자만 가능한 작업입니다."),
    POST_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "모집 중인 게시글이 아닙니다."),

    // ===== Application (참여) =====
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "참여 내역을 찾을 수 없습니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 참여한 게시글입니다."),
    CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "모집 인원이 마감되었습니다."),
    WRITER_CANNOT_APPLY(HttpStatus.BAD_REQUEST, "작성자는 본인 게시글에 참여할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
