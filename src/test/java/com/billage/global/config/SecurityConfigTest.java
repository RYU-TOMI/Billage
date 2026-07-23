package com.billage.global.config;

import com.billage.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security 설정")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("공개 경로는 토큰 없이 접근할 수 있다")
    void publicEndpoint_withoutToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("보호 경로에 토큰 없이 접근하면 401 과 공통 에러 포맷을 돌려준다")
    void protectedEndpoint_withoutToken() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 401 을 돌려준다")
    void protectedEndpoint_withInvalidToken() throws Exception {
        mockMvc.perform(get("/api/posts").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("유효한 토큰이면 인증을 통과한다 (아직 핸들러가 없어 404 가 나오는 것이 정상)")
    void protectedEndpoint_withValidToken() throws Exception {
        String token = jwtTokenProvider.createAccessToken(1L);

        mockMvc.perform(get("/api/posts").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("리프레시 토큰으로는 API 인증을 통과할 수 없다")
    void protectedEndpoint_withRefreshToken() throws Exception {
        String refreshToken = jwtTokenProvider.createRefreshToken(1L);

        mockMvc.perform(get("/api/posts").header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Swagger 문서는 토큰 없이 열린다")
    void swagger_withoutToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
