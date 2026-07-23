package com.billage.global.config;

import com.billage.global.security.handler.JwtAccessDeniedHandler;
import com.billage.global.security.handler.JwtAuthenticationEntryPoint;
import com.billage.global.security.jwt.JwtAuthenticationFilter;
import com.billage.global.security.jwt.JwtProperties;
import com.billage.global.security.oauth2.CustomOAuth2UserService;
import com.billage.global.security.oauth2.OAuth2FailureHandler;
import com.billage.global.security.oauth2.OAuth2Properties;
import com.billage.global.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({JwtProperties.class, OAuth2Properties.class})
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * 인증 없이 접근할 수 있는 경로.
     * 로그인 · 회원가입 담당자가 경로를 추가해야 하면 여기에 넣으세요.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/health",
            "/api/auth/**",          // 로그아웃, 토큰 재발급
            "/oauth2/**",            // 카카오 로그인 시작 (/oauth2/authorization/kakao)
            "/login/oauth2/**",      // 카카오 콜백 (/login/oauth2/code/kakao)
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT 를 쓰므로 세션을 만들지 않습니다. 따라서 CSRF 토큰도 필요 없습니다.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 스프링 시큐리티 기본 로그인 화면 · 팝업을 끕니다.
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // H2 콘솔이 iframe 을 쓰기 때문에 필요합니다. (local 전용)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // CORS preflight
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )

                // 카카오 로그인 → CustomOAuth2UserService 로 회원 조회·생성 → 성공 핸들러가 JWT 발급
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)   // 401
                        .accessDeniedHandler(jwtAccessDeniedHandler)             // 403
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
