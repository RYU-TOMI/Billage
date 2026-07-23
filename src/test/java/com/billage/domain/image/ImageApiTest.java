package com.billage.domain.image;

import com.billage.domain.user.entity.User;
import com.billage.domain.user.repository.UserRepository;
import com.billage.global.security.jwt.JwtTokenProvider;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("이미지 업로드 API")
class ImageApiTest {

    /** 실제 JPEG 파일 시그니처(FF D8 FF)로 시작하는 최소 바이트. 매직 바이트 검증 통과용. */
    private static final byte[] FAKE_JPEG_BYTES =
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.create("uploader@kakao.com", "kakao-uploader", "업로더", null, "홍익대학교"));
        accessToken = "Bearer " + jwtTokenProvider.createAccessToken(user.getId());
    }

    @Test
    @DisplayName("jpg 파일을 업로드하면 /images/ 로 시작하는 URL을 돌려준다")
    void upload_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "drill.jpg", "image/jpeg", FAKE_JPEG_BYTES);

        mockMvc.perform(multipart("/api/images").file(file).header("Authorization", accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imageUrl").value(containsString("/images/")))
                .andExpect(jsonPath("$.data.imageUrl").value(endsWith(".jpg")));
    }

    @Test
    @DisplayName("토큰 없이 업로드하면 401")
    void upload_withoutToken_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "drill.jpg", "image/jpeg", FAKE_JPEG_BYTES);

        mockMvc.perform(multipart("/api/images").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("허용하지 않는 확장자는 INVALID_IMAGE_FILE로 거부한다")
    void upload_disallowedExtension_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "malware.html", "text/html", "<script>alert(1)</script>".getBytes());

        mockMvc.perform(multipart("/api/images").file(file).header("Authorization", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_IMAGE_FILE"));
    }

    @Test
    @DisplayName("확장자는 jpg인데 실제 내용이 이미지가 아니면 INVALID_IMAGE_FILE로 거부한다")
    void upload_extensionSpoofed_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "malware.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes());

        mockMvc.perform(multipart("/api/images").file(file).header("Authorization", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_IMAGE_FILE"));
    }

    @Test
    @DisplayName("빈 파일은 INVALID_IMAGE_FILE로 거부한다")
    void upload_emptyFile_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/images").file(file).header("Authorization", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_IMAGE_FILE"));
    }

    @Test
    @DisplayName("5MB(멀티파트 상한)를 넘으면 INVALID_IMAGE_FILE로 거부한다")
    void upload_tooLarge_rejected() throws Exception {
        byte[] tooLarge = new byte[6 * 1024 * 1024];
        System.arraycopy(FAKE_JPEG_BYTES, 0, tooLarge, 0, FAKE_JPEG_BYTES.length);
        Arrays.fill(tooLarge, FAKE_JPEG_BYTES.length, tooLarge.length, (byte) 1);

        MockMultipartFile file = new MockMultipartFile("image", "big.jpg", "image/jpeg", tooLarge);

        mockMvc.perform(multipart("/api/images").file(file).header("Authorization", accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_IMAGE_FILE"));
    }
}
