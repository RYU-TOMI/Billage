package com.billage.domain.image.service;

import com.billage.global.exception.BusinessException;
import com.billage.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드된 이미지를 {@code upload.dir} 경로에 저장합니다. 저장된 파일은
 * {@link com.billage.global.config.WebConfig} 가 {@code /images/**} 로 정적 서빙합니다.
 */
@Slf4j
@Service
public class ImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final Path uploadDir;

    public ImageService(@Value("${upload.dir}") String uploadDir) {
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
}
