package com.jeonjueats.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 * 엔티티의 생성시간, 수정시간을 자동으로 관리하기 위한 설정
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing 기능을 활성화하는 설정 클래스
} 