-- MySQL 권한 부여 스크립트
-- 이 스크립트는 root 사용자로 MySQL에 접속하여 실행해야 합니다.

-- 방법 1: pigg4949 사용자에게 모든 데이터베이스에 대한 모든 권한 부여 (개발 환경용)
-- 주의: 프로덕션 환경에서는 특정 데이터베이스에만 권한을 부여하는 것이 좋습니다.

-- 먼저 사용자가 존재하는지 확인하고, 없으면 생성
CREATE USER IF NOT EXISTS 'pigg4949'@'localhost' IDENTIFIED BY 'your_password_here';

-- 모든 데이터베이스에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'pigg4949'@'localhost';

-- 또는 방법 2: 특정 데이터베이스(hatw)에만 권한 부여 (더 안전함)
-- GRANT ALL PRIVILEGES ON hatw.* TO 'pigg4949'@'localhost';

-- 권한 변경사항 즉시 적용
FLUSH PRIVILEGES;

-- 권한 확인
SHOW GRANTS FOR 'pigg4949'@'localhost';

