package com.billage.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OAuth2 콜백 주소 결정")
class OAuth2RedirectUriResolverTest {

    private static final String DEFAULT_URI = "https://billage.site/oauth/callback";

    private final OAuth2RedirectUriResolver resolver = new OAuth2RedirectUriResolver(
            new OAuth2Properties(DEFAULT_URI, List.of(
                    "http://localhost:5173",
                    "https://billage.site"
            ))
    );

    private HttpServletRequest startRequest(String redirectUri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorization/kakao");
        if (redirectUri != null) {
            request.setParameter(OAuth2RedirectUriResolver.REDIRECT_URI_PARAM, redirectUri);
        }
        return request;
    }

    @Test
    @DisplayName("redirect_uri 를 지정하지 않으면 기본 주소를 쓴다")
    void resolve_default() {
        MockHttpServletRequest request = (MockHttpServletRequest) startRequest(null);
        resolver.capture(request);

        assertThat(resolver.resolve(request)).isEqualTo(DEFAULT_URI);
    }

    @Test
    @DisplayName("허용된 출처면 지정한 주소로 돌려보낸다")
    void resolve_allowedOrigin() {
        String local = "http://localhost:5173/oauth/callback";
        MockHttpServletRequest request = (MockHttpServletRequest) startRequest(local);
        resolver.capture(request);

        assertThat(resolver.resolve(request)).isEqualTo(local);
    }

    @Test
    @DisplayName("허용 출처면 경로가 달라도 받아준다")
    void resolve_allowedOrigin_differentPath() {
        String local = "http://localhost:5173/login/success";
        MockHttpServletRequest request = (MockHttpServletRequest) startRequest(local);
        resolver.capture(request);

        assertThat(resolver.resolve(request)).isEqualTo(local);
    }

    @Test
    @DisplayName("허용되지 않은 출처는 무시하고 기본 주소로 돌려보낸다 — 토큰 탈취 방지")
    void resolve_deniesUnknownOrigin() {
        MockHttpServletRequest request = (MockHttpServletRequest) startRequest("https://evil.example.com/steal");
        resolver.capture(request);

        assertThat(resolver.resolve(request)).isEqualTo(DEFAULT_URI);
    }

    @Test
    @DisplayName("포트가 다르면 다른 출처로 본다")
    void resolve_deniesDifferentPort() {
        MockHttpServletRequest request = (MockHttpServletRequest) startRequest("http://localhost:9999/oauth/callback");
        resolver.capture(request);

        assertThat(resolver.resolve(request)).isEqualTo(DEFAULT_URI);
    }

    @Test
    @DisplayName("형식이 잘못된 주소는 무시한다")
    void resolve_deniesMalformed() {
        MockHttpServletRequest request = (MockHttpServletRequest) startRequest("not a uri");
        resolver.capture(request);

        assertThat(resolver.resolve(request)).isEqualTo(DEFAULT_URI);
    }

    @Test
    @DisplayName("한 번 사용하면 세션에서 지워져 다음 로그인에 영향을 주지 않는다")
    void resolve_consumesOnce() {
        String local = "http://localhost:5173/oauth/callback";
        MockHttpServletRequest request = (MockHttpServletRequest) startRequest(local);
        resolver.capture(request);

        assertThat(resolver.resolve(request)).isEqualTo(local);
        assertThat(resolver.resolve(request)).isEqualTo(DEFAULT_URI);
    }
}
