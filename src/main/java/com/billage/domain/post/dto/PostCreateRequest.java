package com.billage.domain.post.dto;

import com.billage.domain.post.entity.PostCategory;
import com.billage.domain.post.entity.PostDirection;
import com.billage.domain.post.entity.PostType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * deadline 은 GROUP_BUY 일 때 필수입니다. (교차 검증은 서비스 계층에서 수행)
 * capacity 는 RENTAL 이면 서비스에서 항상 1로 덮어씁니다.
 *
 * <p>price 상한은 참여 신청 시 price * rentalDays 계산에서 오버플로가 나지 않도록
 * 하기 위한 것이기도 합니다. (JoinRequest.rentalDays 상한과 함께 확인하세요)
 */
public record PostCreateRequest(
        @NotNull PostCategory category,

        @NotNull PostType type,

        @NotNull PostDirection direction,

        @NotBlank String title,

        @NotBlank String content,

        @NotBlank String location,

        @NotNull @Min(0) @Max(1_000_000) Integer price,

        @NotNull @Min(1) Integer capacity,

        LocalDateTime deadline,

        @NotBlank String imageUrl
) {
}
