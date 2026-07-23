package com.billage.global.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 소셜 로그인 완료 후 프론트로 돌려보낼 주소.
 *
 * <p>성공: {@code {redirectUri}?accessToken=...}
 * <br>실패: {@code {redirectUri}?error=social_login_failed}
 *
 * @param redirectUri 프론트엔드 콜백 주소. 배포 시 환경변수 OAUTH2_REDIRECT_URI 로 주입하세요.
 */
@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2Properties(String redirectUri) {
}
