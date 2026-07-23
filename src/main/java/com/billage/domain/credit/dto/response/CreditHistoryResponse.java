package com.billage.domain.credit.dto.response;

import com.billage.domain.credit.entity.CreditHistory;
import com.billage.domain.credit.entity.CreditReason;
import java.time.LocalDateTime;

/** GET /api/users/me/credit/history?page= */
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
}

