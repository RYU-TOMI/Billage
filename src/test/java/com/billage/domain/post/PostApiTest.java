package com.billage.domain.post;

import com.billage.domain.post.entity.Post;
import com.billage.domain.post.entity.PostCategory;
import com.billage.domain.post.entity.PostDirection;
import com.billage.domain.post.entity.PostType;
import com.billage.domain.post.repository.PostRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("게시글 API")
class PostApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String accessToken;
    private User writer;

    @BeforeEach
    void setUp() {
        writer = userRepository.save(User.create("hyunjun@kakao.com", "kakao-post-test", "현준", null, "홍익대학교"));
        accessToken = "Bearer " + jwtTokenProvider.createAccessToken(writer.getId());
    }

    @Test
    @DisplayName("RENTAL 등록 시 capacity 를 요청값과 무관하게 1로 강제한다")
    void createPost_rental_forcesCapacityToOne() throws Exception {
        String body = """
                {
                  "category": "TOOL",
                  "type": "RENTAL",
                  "direction": "LEND",
                  "title": "전동 드릴",
                  "content": "가볍고 사용하기 좋아요",
                  "location": "홍익대학교 정문",
                  "price": 1000,
                  "capacity": 5,
                  "imageUrl": "https://example.com/drill.jpg"
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.capacity").value(1))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @DisplayName("GROUP_BUY 인데 deadline 이 없으면 DEADLINE_REQUIRED 로 거부한다")
    void createPost_groupBuyWithoutDeadline_rejected() throws Exception {
        String body = """
                {
                  "category": "LIVING",
                  "type": "GROUP_BUY",
                  "direction": "BORROW",
                  "title": "휴지 공동구매",
                  "content": "10개 묶음 나눠요",
                  "location": "홍익대학교 후문",
                  "price": 3000,
                  "capacity": 4,
                  "imageUrl": "https://example.com/tissue.jpg"
                }
                """;

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("DEADLINE_REQUIRED"));
    }

    @Test
    @DisplayName("게시글 상세를 조회한다")
    void getPost_returnsDetail() throws Exception {
        Post post = postRepository.save(Post.create(
                writer, PostCategory.TOOL, PostType.RENTAL, PostDirection.LEND,
                "전동 드릴", "설명", "홍익대학교 정문", 1000, 1, null, "https://example.com/drill.jpg"
        ));

        mockMvc.perform(get("/api/posts/{postId}", post.getId()).header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("전동 드릴"))
                .andExpect(jsonPath("$.data.direction").value("LEND"))
                .andExpect(jsonPath("$.data.location").value("홍익대학교 정문"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회는 404를 반환한다")
    void getPost_notFound() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}", 999_999L).header("Authorization", accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("POST_NOT_FOUND"));
    }

    @Test
    @DisplayName("목록 조회는 공통 PageResponse 형식으로 내려온다")
    void getPosts_returnsPageResponse() throws Exception {
        postRepository.save(Post.create(
                writer, PostCategory.TOOL, PostType.RENTAL, PostDirection.LEND,
                "전동 드릴", "설명", "홍익대학교 정문", 1000, 1, null, "https://example.com/drill.jpg"
        ));

        mockMvc.perform(get("/api/posts").header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("제목 키워드로 검색한다")
    void searchPosts_byKeyword() throws Exception {
        postRepository.save(Post.create(
                writer, PostCategory.TOOL, PostType.RENTAL, PostDirection.LEND,
                "전동 드릴", "설명", "홍익대학교 정문", 1000, 1, null, "https://example.com/drill.jpg"
        ));

        mockMvc.perform(get("/api/posts/search").param("keyword", "드릴").header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/posts/search").param("keyword", "없는물건").header("Authorization", accessToken))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
