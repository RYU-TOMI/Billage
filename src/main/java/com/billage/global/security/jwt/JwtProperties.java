package com.billage.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml 의 jwt.* 설정값.
 *
 * @param secret                HS256 서명 키. <b>운영에서는 반드시 환경변수 JWT_SECRET 로 주입하세요.</b> (32바이트 이상)
 * @param accessTokenValidity   액세스 토큰 유효시간 (ms)
 * @param refreshTokenValidity  리프레시 토큰 유효시간 (ms)
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenValidity,
        long refreshTokenValidity
) {
}
