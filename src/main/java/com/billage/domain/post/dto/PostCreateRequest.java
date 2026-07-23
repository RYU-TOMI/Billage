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
 * <p>price 상한(1,000,000)은 등록 가능한 가격에 대한 서비스 정책입니다.
 * 참여 신청 시 price * rentalDays 계산의 오버플로 방지는 이 상한만으로는 충분하지
 * 않으므로(rentalDays 가 커지면 여전히 넘칠 수 있음), ApplicationService.join() 에서
 * long 연산 + Math.toIntExact 로 별도로 막습니다. JoinRequest.rentalDays 상한(365)도
 * 함께 확인하세요.
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
