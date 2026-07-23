package com.billage.global.security.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 카카오 인증 직후에만 쓰이는 principal.
 * {@link OAuth2SuccessHandler} 가 여기서 userId 를 꺼내 JWT 를 발급합니다.
 *
 * <p>이후 API 요청은 JWT 로 인증되므로
 * {@link com.billage.global.security.AuthUser} 가 principal 이 됩니다.
 */
public record CustomOAuth2User(
        Long userId,
        boolean newUser,
        Map<String, Object> attributes
) implements OAuth2User {

    public Long getUserId() {
        return userId;
    }

    /** 이번 로그인에서 새로 가입한 회원인지 여부. 프론트의 온보딩 분기에 쓰입니다. */
    public boolean isNewUser() {
        return newUser;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
