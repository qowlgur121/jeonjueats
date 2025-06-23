# 전주이츠 (JeonjuEats)

전주 지역 음식 배달 중개 플랫폼

## 프로젝트 개요

전주이츠는 전주 지역의 소상공인과 고객을 연결하는 배달 중개 서비스입니다. 

## 서비스 URL

- **고객용 앱**: https://jeonjueats.me 
- **사장님 앱**: https://jeonjueats.me/owner 
- **API 문서**: https://jeonjueats.me/swagger-ui/index.html
- **Android 앱**: https://drive.google.com/file/d/1I3SoxbWQLEiibWzFm954TBqQ2SuTxlq2/view?usp=drive_link

 

## 시스템 아키텍처

<div align="center">
  <img src="https://github.com/user-attachments/assets/504f08e4-c1ee-4fe6-a912-b93829447be1" width="1000" alt="시스템 아키텍처">
</div>

### Nginx 통합 라우터 구조

현재 시스템은 Nginx를 중앙 라우터로 사용하여 단일 도메인에서 모든 서비스를 제공합니다

```
https://jeonjueats.me/
├── / (고객용 앱 - Vue.js PWA)
├── /owner (사장님 앱 - Vue.js PWA)
├── /api (백엔드 API - Spring Boot)
└── nginx (통합 라우터 및 프록시)
```

**장점:**
- 단일 HTTPS 인증서로 모든 서비스 보안 연결
- CORS 문제 완전 해결
- PWA 매니페스트 정상 작동
- 배포 및 관리 단순화

### 주요 기술 스택

#### 프론트엔드
- **Vue.js 3**
- **TypeScript**
- **Pinia**
- **Vue Router 4**
- **Tailwind CSS**
- **Vite**
- **Axios**

#### 백엔드
- **Spring Boot 3.5**
- **Java 17**
- **Spring Security**
- **Spring Data JPA**
- **Hibernate**
- **Springdoc OpenAPI**
- **Gradle**

#### 데이터베이스
- **MySQL 8.0**
- **HikariCP**

#### 인프라 및 배포
- **Docker** 
- **Docker Compose**
- **Nginx** 
- **AWS EC2(Ubuntu 24.04 LTS)**
- **Cloudflare**
- **GitHub Actions**
- **Docker Hub**

#### 모바일
- **VitePWA**
- **PWA Manifest**
- **Bubblewrap CLI**

## 데이터베이스 설계 (ERD)

<div align="center">
  <img src="https://github.com/user-attachments/assets/80046381-de07-4420-b975-6a8947955492" width="700" alt="데이터베이스 ERD">
</div>

## API 문서

전주이츠의 모든 REST API는 Swagger UI를 통해 상세하게 문서화되어 있습니다.

Swagger: [https://jeonjueats.me/swagger-ui/index.html](https://jeonjueats.me/swagger-ui/index.html)

### 주요 API 그룹
- **인증 API** - 회원가입, 로그인, JWT 토큰 관리
- **가게 조회 API** - 가게 목록 및 상세 정보 조회
- **검색 API** - 가게 및 메뉴 통합 검색
- **장바구니 API** - 장바구니 메뉴 관리
- **주문 API** - 주문 생성 및 내역 조회
- **찜하기 API** - 관심 가게 저장 및 관리
- **마이페이지 API** - 사용자 프로필 및 주문 내역
- **사장님 가게 관리** - 가게 정보 등록, 수정, 운영 상태 관리
- **사장님 메뉴 관리** - 메뉴 CRUD 및 품절 상태 관리
- **사장님 주문 관리** - 가게 주문 조회 및 상태 변경
- **이미지 업로드 API** - 가게 및 메뉴 이미지 파일 업로드

## 주요 비즈니스 로직

### 장바구니 제약 사항
- 사용자당 하나의 장바구니만 존재
- 한 번에 한 가게의 메뉴만 담기 가능
- 다른 가게 메뉴 추가 시 기존 장바구니 삭제 후 진행

### 영업 상태 관리
- 사장님 수동 설정 우선 (OPEN/CLOSED)
- 운영시간 기반 자동 상태 계산
- 실시간 UI 업데이트

## 보안 및 인증

### JWT 인증 시스템
- 액세스 토큰 기반 무상태 인증
- Role 기반 접근 제어 (ROLE_USER, ROLE_OWNER)
- Spring Security 필터 체인 통합

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
- 사장님 앱: http://localhost/owner 
- API 문서: http://localhost/swagger-ui/index.html

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
# 초기 데이터
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

## 프로젝트 현황

### 완성된 기능 
- **웹 플랫폼**: 고객용/사장님용 PWA 구현
- **모바일 앱**: TWA Android 앱 (Android Studio + Bubblewrap CLI 방식)
- **백엔드 API**: Spring Boot 기반 REST API
- **인프라**: AWS EC2 + Docker + CI/CD 자동 배포
- **도메인 및 HTTPS**: jeonjueats.me 도메인 적용

---
