# 전주이츠 (JeonjuEats)

전주 지역 음식 배달 중개 플랫폼

## 프로젝트 개요

전주이츠는 전주 지역의 소상공인과 고객을 연결하는 배달 중개 서비스입니다. Spring Boot와 Vue.js 기반의 웹 애플리케이션으로 개발하였습니다.

## 서비스 URL

- **고객용 앱**: https://jeonjueats.me
- **사장님 앱**: http://3.35.145.18:81
- **API 문서**: http://3.35.145.18:8080/swagger-ui/index.html

## 기술 스택

### Backend
- **Java 17** + **Spring Boot 3.5.0**
- **Spring Security** 
- **Spring Data JPA** + **Hibernate**
- **MySQL 8.0**
- **Gradle** 
- **Springdoc OpenAPI** 

### Frontend
- **Vue.js 3** 
- **TypeScript**
- **Vue Router 4** + **Pinia**
- **Tailwind CSS**
- **Vite** 
- **Axios** 

### Infrastructure
- **Docker** + **Docker Compose**
- **AWS EC2** (Ubuntu 24.04 LTS)
- **Nginx** 
- **GitHub Actions** (CI/CD)
- **Docker Hub** 

## 시스템 아키텍처

<div align="center">
  <img src="https://github.com/user-attachments/assets/278300f1-d349-499f-9611-e81f5f5a3f62" width="1000" alt="시스템 아키텍처">
</div>

### 주요 기술 스택

#### 프론트엔드
- **Vue.js 3** (Composition API) - 사용자 인터페이스 프레임워크
- **TypeScript** - 타입 안전성과 개발 생산성 향상
- **Pinia** - Vue.js 상태 관리 라이브러리
- **Vue Router 4** - SPA 클라이언트 사이드 라우팅
- **Tailwind CSS** - 유틸리티 기반 CSS 프레임워크
- **Vite** - 빠른 개발 서버 및 빌드 도구
- **Axios** - HTTP 클라이언트 라이브러리

#### 백엔드
- **Spring Boot 3.5** - Java 기반 웹 애플리케이션 프레임워크
- **Java 17** - LTS 버전 JDK
- **Spring Security** - JWT 기반 인증 및 권한 관리
- **Spring Data JPA** - 객체 관계 매핑 및 데이터 접근 추상화
- **Hibernate** - JPA 구현체
- **Springdoc OpenAPI** - API 문서 자동 생성
- **Gradle** - 빌드 및 의존성 관리 도구

#### 데이터베이스
- **MySQL 8.0** - 관계형 데이터베이스 관리 시스템
- **HikariCP** - 고성능 JDBC 커넥션 풀

#### 인프라 및 배포
- **Docker** - 애플리케이션 컨테이너화
- **Docker Compose** - 멀티 컨테이너 오케스트레이션
- **Nginx** - 웹 서버 및 리버스 프록시
- **AWS EC2** - 클라우드 가상 서버 (Ubuntu 24.04 LTS)
- **Cloudflare** - DNS, SSL, CDN 서비스
- **GitHub Actions** - CI/CD 자동화 파이프라인
- **Docker Hub** - 컨테이너 이미지 레지스트리

## 데이터베이스 설계 (ERD)

<div align="center">
  <img src="https://github.com/user-attachments/assets/80046381-de07-4420-b975-6a8947955492" width="700" alt="데이터베이스 ERD">
</div>

## API 명세서

### 1. 인증 관련 API
```
POST /api/auth/signup - 회원가입
POST /api/auth/login - 로그인
```

### 2. 사용자 관리 API
```
GET /api/users/me - 내 프로필 조회
PUT /api/users/me - 내 프로필 수정
```

### 3. 가게 관리 API (일반 사용자)
```
GET /api/stores - 가게 목록 조회 (카테고리 필터링, 페이징)
GET /api/stores/{storeId} - 가게 상세 정보 및 메뉴 목록 조회
```

### 4. 가게 관리 API (사장님)
```
POST /api/owner/stores - 새 가게 등록
GET /api/owner/stores - 내 가게 목록 조회
GET /api/owner/stores/{storeId} - 내 가게 상세 정보 조회
PUT /api/owner/stores/{storeId} - 내 가게 정보 수정
POST /api/owner/stores/{storeId}/toggle-operation - 가게 운영 상태 토글
DELETE /api/owner/stores/{storeId} - 가게 삭제
```

### 5. 메뉴 관리 API (사장님)
```
POST /api/owner/stores/{storeId}/menus - 메뉴 등록
GET /api/owner/stores/{storeId}/menus - 가게 메뉴 목록 조회
PUT /api/owner/stores/{storeId}/menus/{menuId} - 메뉴 정보 수정
DELETE /api/owner/stores/{storeId}/menus/{menuId} - 메뉴 삭제
POST /api/owner/stores/{storeId}/menus/{menuId}/toggle-availability - 메뉴 판매 상태 토글
```

### 6. 장바구니 관리 API
```
PUT /api/carts/items - 장바구니 메뉴 추가/수량 변경 통합 API
GET /api/carts - 내 장바구니 조회
DELETE /api/carts/items/{cartItemId} - 장바구니 아이템 삭제
DELETE /api/carts - 전체 장바구니 비우기
```

### 7. 주문 관리 API (일반 사용자)
```
POST /api/orders - 주문 생성
GET /api/orders - 내 주문 목록 조회 (페이징)
GET /api/orders/{orderId} - 주문 상세 조회
```

### 8. 주문 관리 API (사장님)
```
GET /api/owner/stores/{storeId}/orders - 가게별 주문 목록 조회 (상태별 필터링)
GET /api/owner/stores/{storeId}/orders/{orderId} - 가게별 특정 주문 상세 조회
PUT /api/owner/stores/{storeId}/orders/{orderId}/status - 주문 상태 변경
```

### 9. 찜하기 관리 API
```
POST /api/wishes/stores/{storeId}/toggle - 가게 찜 상태 토글
GET /api/wishes/stores - 내 찜 목록 조회 (페이징)
GET /api/wishes/stores/{storeId}/status - 특정 가게 찜 여부 확인
```

### 10. 검색 및 기타 API
```
GET /api/search - 가게/메뉴 통합 검색
POST /api/upload/upload - 이미지 파일 업로드
GET /.well-known/assetlinks.json - TWA Digital Asset Links 파일 제공
```

## 주요 비즈니스 로직

### 장바구니 제약 사항
- 사용자당 하나의 장바구니만 존재
- 한 번에 한 가게의 메뉴만 담기 가능
- 다른 가게 메뉴 추가 시 기존 장바구니 삭제 후 진행

### 영업 상태 관리
- 사장님 수동 설정 우선 (OPEN/CLOSED)
- 운영시간 기반 자동 상태 계산
- 실시간 UI 업데이트

### 논리적 삭제
- 가게, 메뉴는 `is_deleted` 플래그로 논리 삭제
- 데이터 무결성 유지 및 주문 내역 보존

## 보안 및 인증

### JWT 인증 시스템
- 액세스 토큰 기반 무상태 인증
- Role 기반 접근 제어 (ROLE_USER, ROLE_OWNER)
- Spring Security 필터 체인 통합

### 파일 업로드 보안
- UUID + 타임스탬프 기반 파일명 생성
- 허용된 이미지 확장자만 업로드 가능
- 서버 로컬 저장 + Nginx 정적 서빙

## 로컬 개발 환경

### 필수 요구사항
- Docker Desktop
- Git

### 빠른 시작
```bash
git clone https://github.com/username/jeonjueats.git
cd jeonjueats
docker compose up -d
```

### 접속 주소
- 고객용 앱: http://localhost
- 사장님 앱: http://localhost:81
- API 문서: http://localhost:8080/swagger-ui/index.html

### 개별 개발 모드
```bash
# 백엔드 개발
cd backend
./gradlew bootRun

# 프론트엔드 개발 (고객용)
cd frontend/jeonjueats-user-app
npm install
npm run dev

# 프론트엔드 개발 (사장님용)
cd frontend/jeonjueats-owner-app
npm install
npm run dev
```

### 데이터베이스 설정
```yaml
# MySQL 초기 데이터
- 10개 카테고리 (치킨, 피자, 중식, 한식, 일식, 양식, 분식, 카페, 족발/보쌈, 야식)
- 25개 테스트 가게
- 100여 개 메뉴
- 5명 일반 사용자 + 7명 사장님 계정
```

## 배포 환경

### AWS EC2 인프라
- **인스턴스**: t2.micro (Ubuntu 24.04 LTS)
- **IP**: 3.35.145.18
- **도메인**: jeonjueats.me (Cloudflare DNS)
- **HTTPS**: Cloudflare SSL 인증서

### CI/CD 파이프라인

<div align="center">
  <img src="https://github.com/user-attachments/assets/63f62f31-cb6c-4318-95ad-b19f7ba1840f" width="900" alt="CI/CD 파이프라인">
</div>

1. **GitHub Actions**: 소스 코드 체크아웃 및 Docker 이미지 빌드
2. **Docker Hub**: 자동 이미지 푸시 
3. **EC2 배포**: SSH 접속 → 이미지 Pull → 컨테이너 재시작

### 컨테이너 구성
- **jeonjueats-backend**: Spring Boot API 서버
- **jeonjueats-user**: Vue.js 고객용 앱
- **jeonjueats-owner**: Vue.js 사장님 앱
- **jeonjueats-db**: MySQL 8.0 데이터베이스


## 기술적 의사결정

### 프레임워크 선택
- **Spring Boot**: 안정성과 확장성을 고려한 엔터프라이즈급 프레임워크
- **Vue.js**: React 대비 낮은 러닝 커브와 한국어 문서 지원
- **TypeScript**: 런타임 오류 방지 및 개발 생산성 향상

### 아키텍처 설계
- **모놀리식 백엔드**: MVP 단계에서 개발/운영 복잡도 최소화
- **SPA 분리**: 고객용/사장님용 UI/UX 최적화
- **JWT 무상태**: 확장성과 성능을 고려한 인증 방식

### 데이터베이스 설계
- **정규화**: 데이터 일관성과 무결성 보장
- **논리 삭제**: 비즈니스 데이터 보존 필요성
- **인덱스 최적화**: 조회 성능 향상

## 성능 최적화

### 백엔드 최적화
- **JPA 지연 로딩**: N+1 문제 방지
- **페이징 처리**: 대용량 데이터 조회 성능
- **캐싱 전략**: 정적 데이터 메모리 캐싱

### 프론트엔드 최적화
- **컴포넌트 분할**: 재사용성과 유지보수성
- **이미지 최적화**: Nginx 정적 서빙
- **번들 최적화**: Vite 트리 셰이킹

### 인프라 최적화
- **Docker 멀티스테이지**: 이미지 크기 최소화
- **Nginx 프록시**: 로드 밸런싱 및 정적 파일 서빙
- **EC2 스왑**: t2.micro 메모리 제약 해결

## 향후 개선 계획

### Phase 1: 서비스 안정화
- 실제 결제 연동 (토스페이먼츠, 카카오페이)
- 리뷰 및 평점 시스템
- 푸시 알림 시스템

### Phase 2: 기능 확장
- 실시간 주문 알림 (WebSocket)
- 배달 추적 시스템
- 쿠폰 및 프로모션 관리

### Phase 3: 성능 개선
- Redis 캐싱 도입
- CDN 이미지 서빙
- 데이터베이스 최적화
- 모니터링 시스템 구축

### Phase 4: 모바일 확장
- React Native 모바일 앱
- PWA 기능 강화
- 앱스토어 배포

---
