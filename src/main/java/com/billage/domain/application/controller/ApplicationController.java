package com.billage.domain.application.controller;


import com.billage.domain.application.dto.response.ApplicationCancelResponse;
import com.billage.domain.application.service.ApplicationService;
import com.billage.global.common.response.ApiResponse;
import com.billage.global.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PatchMapping("/{applicationId}/cancel")
    public ApiResponse<ApplicationCancelResponse> cancel(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ApiResponse.success(applicationService.cancel(applicationId, authUser.userId()));
    }
}
