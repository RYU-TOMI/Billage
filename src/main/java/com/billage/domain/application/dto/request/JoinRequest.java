package com.billage.domain.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * GROUP_BUY 참여 시에는 rentalDays 는 null 또는 생략, pickupDate/reason/message 도 생략 가능합니다.
 * rentalDays 가 있는지(RENTAL 타입 필수 여부)는 서비스 계층에서 검증합니다.
 *
 * <p>rentalDays 상한(365)은 Post.price 상한과 함께 price * rentalDays 계산이
 * int 범위를 넘지 않도록 막기 위한 것이기도 합니다.
 */
public record JoinRequest(
        @Positive @Max(365) Integer rentalDays,
        LocalDate pickupDate,
        @Size(max = 200) String reason,
        @Size(max = 500) String message
) {
}
