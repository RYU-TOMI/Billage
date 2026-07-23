package com.billage.domain.image.service;

import com.billage.global.exception.BusinessException;
import com.billage.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드된 이미지를 {@code app.upload.dir} 경로에 저장합니다. 저장된 파일은
 * {@link com.billage.global.config.StaticResourceConfig} 가 {@code /images/**} 로 정적 서빙합니다.
 */
@Slf4j
@Service
public class ImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    /** 이미지 파일 시그니처(매직 바이트). 확장자만 바꿔 올리는 걸 막습니다. */
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private final Path uploadDir;

    public ImageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir);
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉터리를 만들 수 없습니다: " + uploadDir, e);
        }
    }

    /** 파일을 저장하고, 정적 서빙 경로에서 쓸 파일명(디렉터리 없이)을 돌려줍니다. */
    public String store(MultipartFile file) {
        String extension = validateAndExtractExtension(file);
        validateActualImageContent(file);

        String filename = UUID.randomUUID() + "." + extension;

        try {
            file.transferTo(uploadDir.resolve(filename));
        } catch (IOException e) {
            log.error("이미지 저장 실패: {}", filename, e);
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return filename;
    }

    private String validateAndExtractExtension(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE, "빈 파일입니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE, "파일 크기는 5MB를 넘을 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        int dotIndex = originalFilename == null ? -1 : originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE, "파일 확장자를 확인할 수 없습니다.");
        }

        String extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE,
                    "jpg, jpeg, png, webp 파일만 업로드할 수 있습니다.");
        }

        return extension;
    }

    /**
     * 확장자만으로는 파일명만 바꾼 HTML/스크립트를 걸러낼 수 없어, 파일 시그니처(매직 바이트)로
     * 실제 이미지 포맷인지 한 번 더 확인합니다.
     *
     * <p>{@code javax.imageio.ImageIO} 로 디코딩을 시도하는 방법도 있지만, JDK 기본 ImageIO는
     * WEBP 를 지원하지 않아(별도 플러그인 필요) 정상적인 webp 업로드까지 막게 됩니다.
     * 그래서 포맷별 매직 바이트만 직접 확인합니다.
     */
    private void validateActualImageContent(MultipartFile file) {
        byte[] header;
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(12);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE, "이미지 파일을 읽을 수 없습니다.");
        }

        boolean isJpeg = startsWith(header, JPEG_MAGIC);
        boolean isPng = startsWith(header, PNG_MAGIC);
        boolean isWebp = header.length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';

        if (!isJpeg && !isPng && !isWebp) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE, "올바른 이미지 파일이 아닙니다.");
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
