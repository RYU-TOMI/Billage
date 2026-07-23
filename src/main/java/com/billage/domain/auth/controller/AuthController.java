package com.billage.domain.auth.controller;

import com.billage.domain.auth.dto.ReissueRequest;
import com.billage.domain.auth.dto.ReissueResponse;
import com.billage.domain.auth.service.AuthService;
import com.billage.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증. 로그인은 GET /oauth2/authorization/kakao 로 시작합니다.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰으로 액세스 토큰을 다시 발급합니다.")
    @PostMapping("/reissue")
    public ApiResponse<ReissueResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        return ApiResponse.success(authService.reissue(request.refreshToken()));
    }
}
