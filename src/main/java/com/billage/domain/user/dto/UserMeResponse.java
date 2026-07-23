package com.billage.domain.user.dto;

import com.billage.domain.user.entity.User;

/**
 * 내 정보 응답.
 *
 * <p>{@code school} 이 null 이면 온보딩(학교 선택)을 마치지 않은 회원입니다.
 * 프론트는 이 값으로 학교 선택 화면 라우팅 여부를 판단합니다.
 */
public record UserMeResponse(
        Long id,
        String nickname,
        String email,
        String profileImage,
        String school,
        Integer credit
) {

    public static UserMeResponse from(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage(),
                user.getSchool(),
                user.getCredit()
        );
    }
}
