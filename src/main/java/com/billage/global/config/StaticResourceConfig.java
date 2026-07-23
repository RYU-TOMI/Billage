package com.billage.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 업로드된 이미지를 {@code /images/**} 로 서빙합니다.
 *
 * <pre>
 * 저장  : {app.upload.dir}/abc.jpg
 * 조회  : https://api.billage.site/images/abc.jpg
 * </pre>
 *
 * <p>운영에서는 docker named volume(`uploads-data`)이 {@code /data/uploads} 에 붙습니다.
 * 볼륨이 없으면 재배포로 컨테이너가 새로 뜰 때마다 업로드된 파일이 전부 사라집니다.
 *
 * <p>단일 인스턴스로 운영하므로 로컬 디스크 저장으로 충분합니다.
 * 서버를 여러 대로 늘리게 되면 S3 같은 공용 스토리지로 옮겨야 합니다.
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final Path uploadDir;

    public StaticResourceConfig(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadDir.toUri().toString())
                .setCachePeriod(3600);
    }
}
