package com.billage.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 발급 · 검증 담당.
 *
 * <p>로그인 담당자는 인증에 성공한 뒤 {@code createAccessToken(userId)} 만 호출하면 됩니다.
 * 토큰 검증은 {@link JwtAuthenticationFilter} 가 모든 요청에서 자동으로 수행합니다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    private final SecretKey key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = properties.accessTokenValidity();
        this.refreshTokenValidity = properties.refreshTokenValidity();
    }

    /** 액세스 토큰 발급. subject 에 userId 가 들어갑니다. */
    public String createAccessToken(Long userId) {
        return createToken(userId, ACCESS_TOKEN, accessTokenValidity);
    }

    /** 리프레시 토큰 발급. */
    public String createRefreshToken(Long userId) {
        return createToken(userId, REFRESH_TOKEN, refreshTokenValidity);
    }

    private String createToken(Long userId, String tokenType, long validityMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 토큰에서 userId 를 꺼냅니다. 유효하지 않으면 예외가 납니다. */
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * 액세스 토큰인지 확인합니다. API 인증에는 액세스 토큰만 허용해야 합니다.
     * type 클레임이 없는 토큰도 액세스 토큰으로 인정하지 않습니다.
     */
    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN.equals(getTokenType(token));
    }

    /** 리프레시 토큰인지 확인합니다. 토큰 재발급 API 에서 사용하세요. */
    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN.equals(getTokenType(token));
    }

    /** 유효하지 않은 토큰이면 null 을 돌려줍니다. (예외를 던지지 않습니다) */
    private String getTokenType(String token) {
        try {
            return parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 토큰이 유효한지 확인합니다.
     * 만료 · 서명 불일치 · 형식 오류를 모두 잡아 false 로 돌려줍니다.
     */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
