package com.billage.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SchoolUpdateRequest(

        @NotBlank(message = "학교를 선택해 주세요.")
        String school
) {
}
