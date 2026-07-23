package com.billage.global.security.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 로그인 시작 요청({@code /oauth2/authorization/**})에서 {@code redirect_uri} 를 꺼내 보관합니다.
 *
 * <p>카카오로 리다이렉트되고 나면 이 파라미터가 사라지기 때문에,
 * 스프링이 카카오로 보내기 전에 미리 챙겨둬야 합니다.
 */
@RequiredArgsConstructor
public class OAuth2RedirectUriCaptureFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_BASE_URI = "/oauth2/authorization/";

    private final OAuth2RedirectUriResolver redirectUriResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith(AUTHORIZATION_BASE_URI)) {
            redirectUriResolver.capture(request);
        }

        filterChain.doFilter(request, response);
    }
}
