package com.billage.global.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 소셜 로그인 완료 후 프론트로 돌려보낼 주소.
 *
 * <p>성공: {@code {redirectUri}?accessToken=...&refreshToken=...&isNewUser=...}
 * <br>실패: {@code {redirectUri}?error=social_login_failed}
 *
 * <p>로그인 시작 시 {@code ?redirect_uri=} 로 다른 주소를 지정할 수 있습니다.
 * 단 {@code allowedRedirectOrigins} 에 등록된 출처만 허용합니다.
 * 검증하지 않으면 공격자가 임의 주소로 토큰을 빼돌릴 수 있습니다. (오픈 리다이렉트)
 *
 * @param redirectUri            기본 콜백 주소. redirect_uri 를 지정하지 않으면 이 주소를 씁니다.
 * @param allowedRedirectOrigins 콜백을 허용할 출처 목록 (scheme://host:port)
 */
@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2Properties(
        String redirectUri,
        List<String> allowedRedirectOrigins
) {
}
