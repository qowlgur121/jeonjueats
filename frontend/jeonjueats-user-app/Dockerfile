# 개발용 Dockerfile
FROM node:20-alpine

WORKDIR /app

# 패키지 파일들 복사
COPY package*.json ./

# 의존성 설치 (cypress 충돌 방지)
RUN npm ci --ignore-scripts

# 소스 파일들 복사
COPY . .

# 포트 3000 노출
EXPOSE 3000

# 개발 서버 실행
CMD ["npm", "run", "dev"] 