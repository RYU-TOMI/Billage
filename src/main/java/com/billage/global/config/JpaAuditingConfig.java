package com.billage.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * {@link com.billage.global.common.entity.BaseTimeEntity} 의 @CreatedDate / @LastModifiedDate 를
 * 동작시키기 위한 설정. 이 설정이 없으면 created_at, updated_at 이 null 로 들어갑니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
