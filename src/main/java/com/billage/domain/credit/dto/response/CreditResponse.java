package com.billage.domain.credit.dto.response;

/**
 * GET /api/users/me/credit  -> { "credit": 10000 }
 * POST /api/users/me/credit/charge -> { "credit": 15000 }
 * 두 응답이 동일한 형태라 하나의 DTO로 공용 사용.
 */
public record CreditResponse(Integer credit) {

    public static CreditResponse from(int credit) {
        return new CreditResponse(credit);
    }
}
