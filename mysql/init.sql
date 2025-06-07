-- MySQL 초기화 스크립트
-- root 사용자가 모든 호스트에서 접근할 수 있도록 설정

-- jeonjueats 데이터베이스 생성 (이미 환경변수로 생성되지만 확실히 하기 위해)
CREATE DATABASE IF NOT EXISTS jeonjueats CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- root 사용자가 모든 호스트에서 접근할 수 있도록 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- 테스트용 쿼리
SELECT 'MySQL 초기화 완료!' as message; 