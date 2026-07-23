package com.billage.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(

        @NotBlank(message = "리프레시 토큰이 필요합니다.")
        String refreshToken
) {
}
