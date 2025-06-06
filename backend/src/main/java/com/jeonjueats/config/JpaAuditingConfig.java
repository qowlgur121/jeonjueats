package com.jeonjueats.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 * - @CreatedDate, @LastModifiedDate 어노테이션을 사용하여 
 *   엔티티의 생성시간, 수정시간을 자동으로 관리
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // JPA Auditing 활성화를 위한 설정 클래스
} 