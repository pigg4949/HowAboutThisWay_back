# 기존 nav 데이터베이스 연결 가이드

## 개요

이미 MySQL에 `nav` 스키마(데이터베이스)가 있는 경우, 해당 데이터베이스에 연결하는 방법입니다.

## 1단계: application-local.properties 설정

`src/main/resources/application-local.properties` 파일을 열고 다음 정보를 수정하세요:

```properties
# 로컬 MySQL nav 데이터베이스 설정
spring.datasource.url=jdbc:mysql://localhost:3306/nav?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

**수정 사항:**

- `localhost`: 로컬 MySQL인 경우 그대로 유지 (원격 서버인 경우 IP 주소로 변경)
- `3306`: MySQL 포트 (다른 포트 사용 시 변경)
- `nav`: 기존 데이터베이스 이름 (이미 존재)
- `username`: 본인의 MySQL 사용자명 (예: root)
- `password`: 본인의 MySQL 비밀번호

## 2단계: 기존 테이블 구조 확인

MySQL Workbench 또는 터미널에서 다음 SQL을 실행하여 기존 테이블 구조를 확인하세요:

```sql
USE nav;

-- 모든 테이블 목록 확인
SHOW TABLES;

-- users 테이블 구조 확인
DESCRIBE users;
-- 또는
SHOW CREATE TABLE users;

-- bookmarks 테이블 구조 확인
DESCRIBE bookmarks;
SHOW CREATE TABLE bookmarks;

-- reports 테이블 구조 확인
DESCRIBE reports;
SHOW CREATE TABLE reports;

-- markers 테이블 구조 확인
DESCRIBE markers;
SHOW CREATE TABLE markers;

-- inquiries 테이블 구조 확인
DESCRIBE inquiries;
SHOW CREATE TABLE inquiries;
```

## 3단계: 필요한 컬럼 확인 및 추가

### users 테이블 확인

다음 컬럼들이 있는지 확인:

- `idx` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `userId` (VARCHAR, UNIQUE, NOT NULL)
- `passwordHash` (VARCHAR)
- `passwordSalt` (VARCHAR)
- `isAdmin` (BOOLEAN) ← **새로 추가된 필드**

**isAdmin 컬럼이 없으면 추가:**

```sql
ALTER TABLE users
ADD COLUMN isAdmin BOOLEAN DEFAULT FALSE COMMENT '관리자 여부(0 = 일반, 1 = 관리자)'
AFTER isActive;
```

### markers 테이블 확인

다음 컬럼들이 있는지 확인:

- `idx` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `type` (INT)
- `lon` (DOUBLE)
- `lat` (DOUBLE)
- `address` (VARCHAR)
- `comment` (TEXT)
- `weight` (INT)
- `stationName` (VARCHAR) ← **새로 추가된 필드**

**stationName 컬럼이 없으면 추가:**

```sql
ALTER TABLE markers
ADD COLUMN stationName VARCHAR(100) COMMENT '역명 (지하철 역 마커용)'
AFTER weight;
```

### 다른 테이블들 확인

다른 테이블들(bookmarks, reports, inquiries)도 위와 같이 구조를 확인하고, 필요한 컬럼이 없으면 추가하세요.

## 4단계: 전체 테이블 구조 확인 SQL

다음 SQL을 실행하여 모든 테이블의 컬럼을 한 번에 확인할 수 있습니다:

```sql
USE nav;

-- users 테이블 컬럼 확인
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'nav'
  AND TABLE_NAME = 'users'
ORDER BY ORDINAL_POSITION;

-- bookmarks 테이블 컬럼 확인
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'nav'
  AND TABLE_NAME = 'bookmarks'
ORDER BY ORDINAL_POSITION;

-- reports 테이블 컬럼 확인
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'nav'
  AND TABLE_NAME = 'reports'
ORDER BY ORDINAL_POSITION;

-- markers 테이블 컬럼 확인
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'nav'
  AND TABLE_NAME = 'markers'
ORDER BY ORDINAL_POSITION;

-- inquiries 테이블 컬럼 확인
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'nav'
  AND TABLE_NAME = 'inquiries'
ORDER BY ORDINAL_POSITION;
```

## 5단계: 애플리케이션 실행

### 프로파일 지정하여 실행

#### IntelliJ IDEA에서:

1. Run/Debug Configurations 열기
2. Active profiles에 `local` 추가
3. 실행

#### 터미널에서:

```bash
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="local"
./gradlew bootRun

# Windows CMD
set SPRING_PROFILES_ACTIVE=local
gradlew.bat bootRun

# Linux/Mac
export SPRING_PROFILES_ACTIVE=local
./gradlew bootRun
```

#### 또는 JAR 실행 시:

```bash
java -jar build/libs/HowAboutThisWay-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

## 6단계: 연결 테스트

### 애플리케이션 실행 후 확인:

1. **콘솔 로그 확인**

   - 데이터베이스 연결 성공 메시지 확인
   - 에러가 없으면 정상 연결

2. **API 테스트**

   ```bash
   # 회원가입 테스트
   curl -X POST http://localhost:8080/api/users/register \
     -H "Content-Type: application/json" \
     -d '{"userId":"testuser","password":"test123","name":"테스트","phone":"01012345678"}'

   # 로그인 테스트
   curl -X POST http://localhost:8080/api/users/login \
     -H "Content-Type: application/json" \
     -d '{"userId":"testuser","password":"test123"}'
   ```

## 문제 해결

### 연결 실패 시

1. **MySQL 서비스 실행 확인**

   ```bash
   # Windows
   net start MySQL80

   # Linux/Mac
   sudo systemctl status mysql
   ```

2. **포트 확인**

   - MySQL이 3306 포트에서 실행 중인지 확인
   - 다른 포트 사용 시 URL에 포트 번호 수정

3. **사용자 권한 확인**

   ```sql
   SHOW GRANTS FOR 'root'@'localhost';
   ```

4. **데이터베이스 존재 확인**
   ```sql
   SHOW DATABASES;
   USE nav;
   SHOW TABLES;
   ```

### 테이블이 없다는 오류

기존 테이블이 없다면 다음 SQL로 생성:

```sql
USE nav;

-- users 테이블이 없으면 생성
CREATE TABLE IF NOT EXISTS users (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) UNIQUE NOT NULL,
    ssn1 VARCHAR(6),
    ssn2 VARCHAR(7),
    name VARCHAR(100),
    passwordHash VARCHAR(255),
    passwordSalt VARCHAR(255),
    phone VARCHAR(20),
    isActive BOOLEAN DEFAULT TRUE,
    isAdmin BOOLEAN DEFAULT FALSE COMMENT '관리자 여부(0 = 일반, 1 = 관리자)',
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- bookmarks 테이블이 없으면 생성
CREATE TABLE IF NOT EXISTS bookmarks (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    label VARCHAR(100),
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- reports 테이블이 없으면 생성
CREATE TABLE IF NOT EXISTS reports (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    type INT COMMENT '제보 타입 (단차, 보도 폭 좁음 등)',
    lon DOUBLE COMMENT '경도',
    lat DOUBLE COMMENT '위도',
    comment TEXT COMMENT '제보 내용',
    imageUrl VARCHAR(500) COMMENT '업로드된 이미지 URL',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '처리 상태 (PENDING, APPROVED, REJECTED)',
    weight INT COMMENT '가중치 (알고리즘용)',
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- markers 테이블이 없으면 생성
CREATE TABLE IF NOT EXISTS markers (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    type INT COMMENT '마커 타입',
    lon DOUBLE COMMENT '경도',
    lat DOUBLE COMMENT '위도',
    address VARCHAR(255) COMMENT '주소(지도 표시용)',
    comment TEXT COMMENT '설명(지도 표시용)',
    weight INT COMMENT '가중치(알고리즘용)',
    stationName VARCHAR(100) COMMENT '역명 (지하철 역 마커용)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- inquiries 테이블이 없으면 생성
CREATE TABLE IF NOT EXISTS inquiries (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    content TEXT NOT NULL COMMENT '문의 내용',
    adminResponses TEXT COMMENT '관리자 답변',
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 빠른 체크리스트

- [ ] `application-local.properties` 파일에서 데이터베이스 URL, 사용자명, 비밀번호 설정
- [ ] MySQL Workbench에서 `nav` 데이터베이스 접속 확인
- [ ] `SHOW TABLES;` 실행하여 필요한 테이블 존재 확인
- [ ] `users` 테이블에 `isAdmin` 컬럼 확인/추가
- [ ] `markers` 테이블에 `stationName` 컬럼 확인/추가
- [ ] 애플리케이션을 `local` 프로파일로 실행
- [ ] 콘솔 로그에서 데이터베이스 연결 성공 확인

## 참고

- 기존 데이터가 있는 경우, ALTER TABLE로 컬럼을 추가해도 기존 데이터는 유지됩니다.
- 외래 키 제약 조건이 있는 경우, 참조하는 테이블(users)이 먼저 생성되어 있어야 합니다.
- 데이터베이스 이름이 `nav`가 아닌 경우, `application-local.properties`의 URL에서 데이터베이스 이름을 변경하세요.
