package com.billage.domain.application.dto.response;

import com.billage.domain.application.entity.Application;
import com.billage.domain.application.entity.ApplicationStatus;

/**
 * PATCH /api/applications/{applicationId}/cancel
 * -> { "applicationId": 40, "status": "CANCELED", "refundedCredit": 12000 }
 */
public record ApplicationCancelResponse(
        Long applicationId,
        ApplicationStatus status,
        Integer refundedCredit
) {

    public static ApplicationCancelResponse of(Application application, int refundedCredit) {
        return new ApplicationCancelResponse(
                application.getId(),
                application.getStatus(),
                refundedCredit
        );
    }
}
