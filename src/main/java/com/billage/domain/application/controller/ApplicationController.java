package com.billage.domain.application.controller;

import com.billage.domain.application.dto.request.JoinRequest;
import com.billage.domain.application.dto.response.ApplicationCancelResponse;
import com.billage.domain.application.dto.response.ApplicationResponse;
import com.billage.domain.application.service.ApplicationService;
import com.billage.global.common.response.ApiResponse;
import com.billage.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/api/posts/{postId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ApplicationResponse> join(@AuthenticationPrincipal AuthUser authUser,
                                                  @PathVariable Long postId,
                                                  @Valid @RequestBody JoinRequest request) {
        return ApiResponse.success(applicationService.join(postId, authUser.getUserId(), request));
    }

    @PatchMapping("/api/applications/{applicationId}/cancel")
    public ApiResponse<ApplicationCancelResponse> cancel(@AuthenticationPrincipal AuthUser authUser,
                                                          @PathVariable Long applicationId) {
        return ApiResponse.success(applicationService.cancel(applicationId, authUser.getUserId()));
    }
}
