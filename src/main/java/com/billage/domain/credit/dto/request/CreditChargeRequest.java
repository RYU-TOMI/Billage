package com.billage.domain.credit.dto.request;

import jakarta.validation.constraints.Positive;

/** POST /api/users/me/credit/charge  요청 바디: { "amount": 10000 } */

public record CreditChargeRequest(
        @Positive Integer amount
) {
}
