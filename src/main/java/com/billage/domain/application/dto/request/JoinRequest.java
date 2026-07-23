package com.billage.domain.application.dto.request;

import java.time.LocalDate;

/**
 * GROUP_BUY 참여 시에는 rentalDays 는 null 또는 생략, pickupDate/reason/message 도 생략 가능합니다.
 * rentalDays 는 RENTAL 타입일 때만 서비스 계층에서 필수로 검증합니다.
 */
public record JoinRequest(
        Integer rentalDays,
        LocalDate pickupDate,
        String reason,
        String message
) {
}
