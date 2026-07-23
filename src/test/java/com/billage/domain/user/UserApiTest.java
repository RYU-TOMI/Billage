package com.billage.domain.user;

import com.billage.domain.user.entity.User;
import com.billage.domain.user.repository.UserRepository;
import com.billage.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("유저 API")
class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.create(
                "hyunjun@kakao.com",
                "kakao-1234",
                "현준",
                "https://.../profile.png",
                null                        // 온보딩 전
        ));

        accessToken = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());
    }

    @Test
    @DisplayName("내 정보 조회 — 온보딩 전이면 school 이 null 로 내려온다")
    void getMe_beforeOnboarding() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("현준"))
                .andExpect(jsonPath("$.data.school").doesNotExist())
                .andExpect(jsonPath("$.data.credit").value(0));
    }

    @Test
    @DisplayName("내 정보 조회 — 토큰이 없으면 401")
    void getMe_withoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("학교를 선택하면 저장되고, 이후 조회에도 반영된다")
    void updateSchool() throws Exception {
        mockMvc.perform(patch("/api/users/me/school")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"school\":\"홍익대학교\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.school").value("홍익대학교"));

        mockMvc.perform(get("/api/users/me").header("Authorization", accessToken))
                .andExpect(jsonPath("$.data.school").value("홍익대학교"));
    }

    @Test
    @DisplayName("목록에 없는 학교는 INVALID_SCHOOL 로 거부한다")
    void updateSchool_notInCatalog() throws Exception {
        mockMvc.perform(patch("/api/users/me/school")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"school\":\"없는대학교\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_SCHOOL"));
    }

    @Test
    @DisplayName("학교 값이 비어 있으면 INVALID_REQUEST 로 거부한다")
    void updateSchool_blank() throws Exception {
        mockMvc.perform(patch("/api/users/me/school")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"school\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("학교 목록을 keyword 로 검색한다")
    void searchSchools() throws Exception {
        mockMvc.perform(get("/api/schools").param("keyword", "여자")
                        .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/api/schools").header("Authorization", accessToken))
                .andExpect(jsonPath("$.data.length()").value(10));
    }
}
