package com.jeonjueats.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI 설정 클래스
 * API 문서화를 위한 OpenAPI 3.0 설정
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 설정 Bean
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 기본 정보 설정
                .info(new Info()
                        .title("전주잇츠 (JeonjuEats) API")
                        .description("전주 지역 배달 중개 플랫폼 MVP의 RESTful API 문서입니다.\n\n" +
                                    "## 주요 기능\n" +
                                    "- 사용자 인증 (회원가입, 로그인)\n" +
                                    "- 가게 조회 및 검색\n" +
                                    "- 메뉴 관리\n" +
                                    "- 장바구니 기능\n" +
                                    "- 주문 처리 (가상 결제)\n" +
                                    "- 사장님 가게/메뉴 관리")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("전주잇츠 개발자")
                                .email("qowlgur121@gmail.com")))
                
                // JWT 인증 스키마 설정
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다.")))
                
                // 전역 보안 요구사항 추가 (필요한 API에만 적용됨)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
} 