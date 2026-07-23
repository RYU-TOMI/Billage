package com.billage.domain.application;

import com.billage.domain.application.entity.Application;
import com.billage.domain.application.repository.ApplicationRepository;
import com.billage.domain.post.entity.Post;
import com.billage.domain.post.entity.PostCategory;
import com.billage.domain.post.entity.PostDirection;
import com.billage.domain.post.entity.PostStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("참여 신청/취소 API")
class ApplicationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User writer;
    private User applicant;
    private String writerToken;
    private String applicantToken;

    @BeforeEach
    void setUp() {
        writer = userRepository.save(User.create("writer@kakao.com", "kakao-writer", "글쓴이", null, "홍익대학교"));
        applicant = userRepository.save(User.create("applicant@kakao.com", "kakao-applicant", "신청자", null, "홍익대학교"));
        applicant.increaseCredit(100_000);

        writerToken = "Bearer " + jwtTokenProvider.createAccessToken(writer.getId());
        applicantToken = "Bearer " + jwtTokenProvider.createAccessToken(applicant.getId());
    }

    private Post rentalPost(int price, int capacity) {
        return postRepository.save(Post.create(
                writer, PostCategory.TOOL, PostType.RENTAL, PostDirection.LEND,
                "전동 드릴", "설명", "홍익대학교 정문", price, capacity, null, "https://example.com/drill.jpg"
        ));
    }

    private Application findApplication(Long postId, Long applicantId) {
        return applicationRepository.findAll().stream()
                .filter(a -> a.getPost().getId().equals(postId) && a.getApplicant().getId().equals(applicantId))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("RENTAL 참여 — 결제액은 price * rentalDays, 크레딧이 그만큼 차감된다")
    void join_rental_success() throws Exception {
        Post post = rentalPost(1000, 1);

        mockMvc.perform(post("/api/posts/{postId}/applications", post.getId())
                        .header("Authorization", applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentalDays\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.rentalDays").value(3))
                .andExpect(jsonPath("$.data.totalPrice").value(3000));

        User reloaded = userRepository.findById(applicant.getId()).orElseThrow();
        assertThat(reloaded.getCredit()).isEqualTo(100_000 - 3000);
    }

    @Test
    @DisplayName("RENTAL 참여 — rentalDays 없으면 RENTAL_DAYS_REQUIRED")
    void join_rental_withoutRentalDays_rejected() throws Exception {
        Post post = rentalPost(1000, 1);

        mockMvc.perform(post("/api/posts/{postId}/applications", post.getId())
                        .header("Authorization", applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("RENTAL_DAYS_REQUIRED"));
    }

    @Test
    @DisplayName("작성자는 본인 게시글에 참여할 수 없다")
    void join_writerCannotApplyToOwnPost() throws Exception {
        Post post = rentalPost(1000, 1);

        mockMvc.perform(post("/api/posts/{postId}/applications", post.getId())
                        .header("Authorization", writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentalDays\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("WRITER_CANNOT_APPLY"));
    }

    @Test
    @DisplayName("같은 게시글에 두 번 신청하면 ALREADY_APPLIED")
    void join_duplicateApplication_rejected() throws Exception {
        Post post = rentalPost(1000, 5);

        mockMvc.perform(post("/api/posts/{postId}/applications", post.getId())
                        .header("Authorization", applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentalDays\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/posts/{postId}/applications", post.getId())
                        .header("Authorization", applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentalDays\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ALREADY_APPLIED"));
    }

    @Test
    @DisplayName("크레딧이 부족하면 INSUFFICIENT_CREDIT")
    void join_insufficientCredit_rejected() throws Exception {
        Post post = rentalPost(1000, 1);
        User poorApplicant = userRepository.save(User.create("poor@kakao.com", "kakao-poor", "빈털터리", null, "홍익대학교"));
        String poorToken = "Bearer " + jwtTokenProvider.createAccessToken(poorApplicant.getId());

        mockMvc.perform(post("/api/posts/{postId}/applications", post.getId())
                        .header("Authorization", poorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentalDays\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_CREDIT"));
    }

    @Test
    @DisplayName("정원이 차면 CAPACITY_EXCEEDED, 취소하면 환불되고 게시글이 다시 열린다")
    void join_thenCancel_refundsAndReopensPost() throws Exception {
        Post post = rentalPost(1000, 1);

        mockMvc.perform(post("/api/posts/{postId}/applications", post.getId())
                        .header("Authorization", applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentalDays\":2}"))
                .andExpect(status().isCreated());

        Post closedPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(closedPost.getStatus()).isEqualTo(PostStatus.CLOSED);

        User secondApplicant = userRepository.save(User.create("second@kakao.com", "kakao-second", "두번째", null, "홍익대학교"));
        secondApplicant.increaseCredit(100_000);
        String secondToken = "Bearer " + jwtTokenProvider.createAccessToken(secondApplicant.getId());

        mockMvc.perform(post("/api/posts/{postId}/applications", post.getId())
                        .header("Authorization", secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rentalDays\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CAPACITY_EXCEEDED"));

        Long applicationId = findApplication(post.getId(), applicant.getId()).getId();

        mockMvc.perform(patch("/api/applications/{applicationId}/cancel", applicationId)
                        .header("Authorization", applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"))
                .andExpect(jsonPath("$.data.refundedCredit").value(2000));

        User reloadedApplicant = userRepository.findById(applicant.getId()).orElseThrow();
        assertThat(reloadedApplicant.getCredit()).isEqualTo(100_000);

        Post reopenedPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(reopenedPost.getStatus()).isEqualTo(PostStatus.OPEN);
    }
}
