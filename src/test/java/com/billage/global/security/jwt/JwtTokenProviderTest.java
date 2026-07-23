package com.billage.global.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-billage-must-be-at-least-32-bytes";
    private static final long ONE_HOUR = 3_600_000L;

    private final JwtTokenProvider provider =
            new JwtTokenProvider(new JwtProperties(SECRET, ONE_HOUR, ONE_HOUR));

    @Test
    @DisplayName("액세스 토큰에서 userId 를 그대로 꺼낼 수 있다")
    void createAccessToken_thenGetUserId() {
        String token = provider.createAccessToken(42L);

        assertThat(provider.validate(token)).isTrue();
        assertThat(provider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰이 구분된다")
    void distinguishTokenType() {
        assertThat(provider.isRefreshToken(provider.createAccessToken(1L))).isFalse();
        assertThat(provider.isRefreshToken(provider.createRefreshToken(1L))).isTrue();
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 유효하지 않다")
    void validate_malformedToken() {
        assertThat(provider.validate("not.a.token")).isFalse();
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 유효하지 않다")
    void validate_wrongSignature() {
        JwtTokenProvider other = new JwtTokenProvider(
                new JwtProperties("another-secret-key-that-is-also-long-enough-32", ONE_HOUR, ONE_HOUR));

        assertThat(provider.validate(other.createAccessToken(1L))).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 유효하지 않다")
    void validate_expiredToken() {
        JwtTokenProvider expiring = new JwtTokenProvider(new JwtProperties(SECRET, -1000L, -1000L));

        assertThat(provider.validate(expiring.createAccessToken(1L))).isFalse();
    }
}
