package com.billage.domain.credit.dto.response;


import com.billage.domain.credit.entity.CreditHistory;
import com.billage.domain.credit.entity.CreditReason;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /api/users/me/credit/history?page=
 * -> { "content": [ { "id", "amount", "reason", "relatedPostId", "createdAt" } ], "totalPages": 1 }
 */
public record CreditHistoryResponse(
        Long id,
        Integer amount,
        CreditReason reason,
        Long relatedPostId,
        LocalDateTime createdAt
) {

    public static CreditHistoryResponse from(CreditHistory history) {
        Long relatedPostId = history.getRelatedPost() != null
                ? history.getRelatedPost().getId()
                : null;

        return new CreditHistoryResponse(
                history.getId(),
                history.getAmount(),
                history.getReason(),
                relatedPostId,
                history.getCreatedAt()
        );
    }

    /** 페이지 응답 래퍼. 팀 공통 PageResponse가 있다면 그걸 사용. */
    public record PageResponse(
            List<CreditHistoryResponse> content,
            int totalPages
    ) {
        public static PageResponse from(Page<CreditHistory> page) {
            List<CreditHistoryResponse> content = page.getContent().stream()
                    .map(CreditHistoryResponse::from)
                    .toList();
            return new PageResponse(content, page.getTotalPages());
        }
    }
}

