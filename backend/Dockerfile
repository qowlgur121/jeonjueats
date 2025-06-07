# Java 17 베이스 이미지 사용
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper와 빌드 파일들 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐시 효율성을 위해)
COPY src src
RUN chmod +x gradlew
RUN ./gradlew build -x test

# 애플리케이션 실행
EXPOSE 8080
CMD ["./gradlew", "bootRun"] 