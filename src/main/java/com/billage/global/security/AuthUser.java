package com.billage.global.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 인증된 사용자 정보. JWT 에서 꺼낸 userId 만 담습니다. (DB 조회 없음)
 *
 * <p>컨트롤러에서 로그인한 유저 id 를 꺼내는 방법:
 * <pre>{@code
 * @GetMapping("/posts/mine")
 * public ApiResponse<...> myPosts(@AuthenticationPrincipal AuthUser authUser) {
 *     Long userId = authUser.getUserId();
 *     ...
 * }
 * }</pre>
 */
public record AuthUser(Long userId) implements UserDetails {

    /** record 접근자와 별개로, getUserId() 형태를 선호하는 경우를 위해 함께 제공합니다. */
    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    /** 비밀번호는 토큰 인증에 쓰이지 않습니다. */
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }
}
