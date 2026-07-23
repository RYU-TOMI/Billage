package com.billage.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 로그인 완료 후 돌아갈 프론트 주소를 결정합니다.
 *
 * <p>프론트가 로그인을 시작할 때 콜백 주소를 지정할 수 있습니다.
 * <pre>
 * GET /oauth2/authorization/kakao?redirect_uri=http://localhost:5173/oauth/callback
 * </pre>
 *
 * <p>배포된 백엔드 하나로 운영·로컬 개발을 모두 받기 위한 장치입니다.
 * 지정하지 않으면 {@code app.oauth2.redirect-uri} 기본값을 씁니다.
 *
 * <p><b>허용 출처 검증은 필수입니다.</b> 검증 없이 받으면
 * {@code ?redirect_uri=https://evil.com} 으로 유도해 액세스 토큰을 그대로 탈취할 수 있습니다.
 * 허용되지 않은 주소는 무시하고 기본값으로 되돌립니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2RedirectUriResolver {

    /** 로그인 시작 시 콜백 주소를 지정하는 쿼리 파라미터 이름 */
    public static final String REDIRECT_URI_PARAM = "redirect_uri";

    private static final String SESSION_KEY = "BILLAGE_OAUTH2_REDIRECT_URI";

    private final OAuth2Properties oAuth2Properties;

    /**
     * 로그인 시작 요청에서 콜백 주소를 꺼내 보관합니다.
     * 카카오를 다녀오는 사이 값이 유지돼야 하므로 세션에 담습니다.
     */
    public void capture(HttpServletRequest request) {
        String requested = request.getParameter(REDIRECT_URI_PARAM);
        if (requested == null || requested.isBlank()) {
            return;
        }

        if (!isAllowed(requested)) {
            log.warn("허용되지 않은 redirect_uri 요청이라 무시합니다: {} (허용 출처: {})",
                    requested, oAuth2Properties.allowedRedirectOrigins());
            return;
        }

        request.getSession(true).setAttribute(SESSION_KEY, requested);
        log.debug("콜백 주소를 지정받았습니다: {}", requested);
    }

    /**
     * 보관해둔 콜백 주소를 꺼냅니다. 없으면 기본값을 돌려줍니다.
     * 한 번 쓰면 세션에서 지웁니다.
     */
    public String resolve(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object stored = session.getAttribute(SESSION_KEY);
            session.removeAttribute(SESSION_KEY);
            if (stored instanceof String uri && !uri.isBlank()) {
                return uri;
            }
        }
        return oAuth2Properties.redirectUri();
    }

    /** 요청된 주소의 출처(scheme://host:port)가 허용 목록에 있는지 확인합니다. */
    private boolean isAllowed(String requestedUri) {
        String origin = toOrigin(requestedUri);
        if (origin == null) {
            return false;
        }
        return oAuth2Properties.allowedRedirectOrigins().stream()
                .map(this::toOrigin)
                .anyMatch(origin::equals);
    }

    /** "http://localhost:5173/oauth/callback" → "http://localhost:5173" */
    private String toOrigin(String uri) {
        try {
            URI parsed = new URI(uri);
            if (parsed.getScheme() == null || parsed.getHost() == null) {
                return null;
            }
            int port = parsed.getPort();
            return port == -1
                    ? parsed.getScheme() + "://" + parsed.getHost()
                    : parsed.getScheme() + "://" + parsed.getHost() + ":" + port;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
