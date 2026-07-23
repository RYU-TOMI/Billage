package com.billage.global.common.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 목록 API 공통 페이지 응답. {@code Page<T>} 를 그대로 반환하면 Spring 기본 직렬화가 나가
 * API마다 형식이 달라지므로, 목록을 응답할 땐 항상 이 타입으로 감싸서 반환합니다.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isLast
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
