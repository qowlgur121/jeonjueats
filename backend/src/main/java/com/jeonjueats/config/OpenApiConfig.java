package com.jeonjueats.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI(Swagger) 설정 클래스
 * API 문서화 및 JWT 인증 설정을 위한 구성
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "전주이츠 API",
        version = "1.0.0",
        description = """
        전주 지역 배달 중개 플랫폼 전주이츠의 REST API 문서입니다.
        
        ## 인증 방법
        1. `/api/auth/login` 엔드포인트에서 로그인하여 JWT 토큰 획득
        2. 우측 상단 "Authorize" 버튼 클릭
        3. `Bearer {토큰값}` 형식으로 입력 (Bearer 뒤에 공백 필수)
        4. 인증이 필요한 API 테스트 가능
        
        ## 테스트 계정
        - **일반 사용자**: user1@example.com / password
        - **사장님**: owner1@example.com / password
        """,
        contact = @Contact(
            name = "전주이츠 개발팀",
            email = "qowlgur121@gmail.com"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "로컬 개발 서버"),
        @Server(url = "https://jeonjueats.me", description = "운영 서버")
    },
    tags = {
        @Tag(name = "인증 API", description = "회원가입, 로그인, JWT 토큰 관리"),
        @Tag(name = "가게 조회 API", description = "가게 목록 및 상세 정보 조회"),
        @Tag(name = "검색 API", description = "가게 및 메뉴 통합 검색"),
        @Tag(name = "장바구니 API", description = "장바구니 메뉴 관리"),
        @Tag(name = "주문 API", description = "주문 생성 및 내역 조회"),
        @Tag(name = "찜하기 API", description = "관심 가게 저장 및 관리"),
        @Tag(name = "마이페이지 API", description = "사용자 프로필 및 주문 내역"),
        @Tag(name = "사장님 가게 관리", description = "가게 정보 등록, 수정, 운영 상태 관리"),
        @Tag(name = "사장님 메뉴 관리", description = "메뉴 CRUD 및 품절 상태 관리"),
        @Tag(name = "사장님 주문 관리", description = "가게 주문 조회 및 상태 변경"),
        @Tag(name = "이미지 업로드 API", description = "가게 및 메뉴 이미지 파일 업로드")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT 토큰을 입력하세요. 'Bearer' 접두사는 자동으로 추가됩니다."
)
public class OpenApiConfig {

}