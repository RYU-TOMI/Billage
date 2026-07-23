package com.billage.domain.user.controller;

import com.billage.domain.user.dto.SchoolUpdateRequest;
import com.billage.domain.user.dto.SchoolUpdateResponse;
import com.billage.domain.user.dto.UserMeResponse;
import com.billage.domain.user.service.UserService;
import com.billage.global.common.response.ApiResponse;
import com.billage.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User", description = "회원 정보 및 학교 온보딩")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회",
            description = "school 이 null 이면 온보딩(학교 선택)을 마치지 않은 회원입니다.")
    @GetMapping("/users/me")
    public ApiResponse<UserMeResponse> getMe(@AuthenticationPrincipal AuthUser authUser) {
        return ApiResponse.success(userService.getMe(authUser.getUserId()));
    }

    @Operation(summary = "학교 선택", description = "지원 목록에 없는 학교면 INVALID_SCHOOL 로 거부합니다.")
    @PatchMapping("/users/me/school")
    public ApiResponse<SchoolUpdateResponse> updateSchool(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SchoolUpdateRequest request) {

        return ApiResponse.success(userService.updateSchool(authUser.getUserId(), request.school()));
    }

    @Operation(summary = "학교 목록 조회", description = "keyword 로 검색합니다. 비우면 전체 목록을 돌려줍니다.")
    @GetMapping("/schools")
    public ApiResponse<List<String>> searchSchools(
            @RequestParam(required = false) String keyword) {

        return ApiResponse.success(userService.searchSchools(keyword));
    }
}
