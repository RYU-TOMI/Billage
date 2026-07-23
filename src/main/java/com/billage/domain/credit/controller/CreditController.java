package com.billage.domain.credit.controller;

import com.billage.domain.credit.dto.request.CreditChargeRequest;
import com.billage.domain.credit.dto.response.CreditHistoryResponse;
import com.billage.domain.credit.dto.response.CreditResponse;
import com.billage.domain.credit.service.CreditService;
import com.billage.global.common.response.ApiResponse;
import com.billage.global.common.response.PageResponse;
import com.billage.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/credit")
public class CreditController {

    private final CreditService creditService;

    @GetMapping
    public ApiResponse<CreditResponse> getBalance(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(creditService.getBalance(authUser.userId()));
    }

    @PostMapping("/charge")
    public ApiResponse<CreditResponse> charge(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreditChargeRequest request
    ) {
        return ApiResponse.success(creditService.charge(authUser.userId(), request));
    }

    @GetMapping("/history")
    public ApiResponse<PageResponse<CreditHistoryResponse>> getHistory(
            @AuthenticationPrincipal AuthUser authUser,
            Pageable pageable
    ) {
        return ApiResponse.success(creditService.getHistory(authUser.userId(), pageable));
    }
}
