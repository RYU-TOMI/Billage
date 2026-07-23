package com.billage.global.security.jwt;

import com.billage.global.security.AuthUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 요청에서 Authorization 헤더의 JWT 를 검사해 SecurityContext 에 인증 정보를 채웁니다.
 *
 * <p>토큰이 없거나 유효하지 않으면 인증 없이 통과시키고, 실제 차단은 SecurityConfig 의
 * 경로 규칙과 {@link com.billage.global.security.handler.JwtAuthenticationEntryPoint} 가 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validate(token)) {
            // API 인증에는 액세스 토큰만 허용합니다.
            // 리프레시 토큰(유효기간이 훨씬 김)이 여기를 통과하면 짧은 액세스 토큰을 둔 의미가 없어집니다.
            if (jwtTokenProvider.isAccessToken(token)) {
                AuthUser authUser = new AuthUser(jwtTokenProvider.getUserId(token));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("액세스 토큰이 아닌 토큰으로 인증을 시도했습니다: {} {}",
                        request.getMethod(), request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    /** "Authorization: Bearer {token}" 에서 토큰만 잘라냅니다. */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
