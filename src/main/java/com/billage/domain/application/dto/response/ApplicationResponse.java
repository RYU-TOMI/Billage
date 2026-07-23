package com.billage.domain.application.dto.response;

import com.billage.domain.application.entity.Application;
import com.billage.domain.application.entity.ApplicationStatus;

/**
 * POST /api/posts/{postId}/applications
 * -> { "applicationId": 40, "status": "CONFIRMED", "rentalDays": 3, "totalPrice": 3000 }
 */
public record ApplicationResponse(
        Long applicationId,
        ApplicationStatus status,
        Integer rentalDays,
        Integer totalPrice
) {

    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getStatus(),
                application.getRentalDays(),
                application.getTotalPrice()
        );
    }
}
