# 개인 MySQL 데이터베이스 연결 가이드

## 개요
팀원이 설정한 공용 MySQL 대신 자신의 로컬 MySQL 데이터베이스를 사용하는 방법입니다.

## 방법 1: 로컬 프로파일 사용 (권장)

### 1단계: MySQL 데이터베이스 준비

1. **MySQL 설치 확인**
   - MySQL이 설치되어 있고 실행 중인지 확인
   - MySQL Workbench 또는 터미널에서 접속 가능한지 확인

2. **데이터베이스 생성**
   ```sql
   CREATE DATABASE nav CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   > 참고: 기존 팀원의 데이터베이스 이름이 `nav`이므로 동일한 이름을 사용하거나, 다른 이름을 사용할 수 있습니다.

3. **사용자 생성 및 권한 부여 (선택사항)**
   ```sql
   CREATE USER 'your_username'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON nav.* TO 'your_username'@'localhost';
   FLUSH PRIVILEGES;
   ```

### 2단계: application-local.properties 파일 수정

`src/main/resources/application-local.properties` 파일을 열고 다음 정보를 수정하세요:

```properties
# 데이터베이스 URL (로컬 MySQL인 경우)
spring.datasource.url=jdbc:mysql://localhost:3306/nav?serverTimezone=Asia/Seoul&characterEncoding=UTF-8

# 본인의 MySQL 사용자명
spring.datasource.username=root

# 본인의 MySQL 비밀번호
spring.datasource.password=your_password
```

**주요 수정 사항:**
- `localhost`: 로컬 MySQL인 경우 그대로 유지
- `3306`: MySQL 기본 포트 (다른 포트 사용 시 변경)
- `nav`: 데이터베이스 이름 (다른 이름 사용 시 변경)
- `username`: 본인의 MySQL 사용자명
- `password`: 본인의 MySQL 비밀번호

### 3단계: 애플리케이션 실행 시 프로파일 지정

#### IntelliJ IDEA에서 실행하는 경우:
1. Run/Debug Configurations 열기
2. Active profiles에 `local` 추가
3. 실행

#### 터미널에서 실행하는 경우:
```bash
# Gradle 사용
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 JAR 파일 실행
java -jar build/libs/HowAboutThisWay-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

#### 환경 변수로 설정:
```bash
# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="local"

# Windows (CMD)
set SPRING_PROFILES_ACTIVE=local

# Linux/Mac
export SPRING_PROFILES_ACTIVE=local
```

## 방법 2: 환경 변수 사용

### 1단계: 환경 변수 설정

#### Windows (PowerShell):
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/nav?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
```

#### Windows (CMD):
```cmd
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/nav?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=your_password
```

#### Linux/Mac:
```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/nav?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="your_password"
```

### 2단계: 애플리케이션 실행
환경 변수를 설정한 후 애플리케이션을 실행하면 환경 변수 값이 우선적으로 사용됩니다.

## 방법 3: application.properties 직접 수정 (비권장)

> ⚠️ 주의: 이 방법은 Git에 커밋되면 다른 팀원에게 영향을 줄 수 있습니다.

`src/main/resources/application.properties` 파일을 직접 수정할 수 있지만, **Git에 커밋하지 않도록 주의**하세요.

## 데이터베이스 스키마 생성

기존 팀원의 데이터베이스 스키마를 복사하거나, 다음 SQL을 실행하여 기본 테이블을 생성할 수 있습니다:

```sql
-- users 테이블
CREATE TABLE users (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) UNIQUE NOT NULL,
    ssn1 VARCHAR(6),
    ssn2 VARCHAR(7),
    name VARCHAR(100),
    passwordHash VARCHAR(255),
    passwordSalt VARCHAR(255),
    phone VARCHAR(20),
    isActive BOOLEAN DEFAULT TRUE,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- bookmarks 테이블
CREATE TABLE bookmarks (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    label VARCHAR(100),
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

-- reports 테이블
CREATE TABLE reports (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    type INT,
    lon DOUBLE,
    lat DOUBLE,
    comment TEXT,
    imageUrl VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    weight INT,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

-- markers 테이블
CREATE TABLE markers (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    type INT,
    lon DOUBLE,
    lat DOUBLE,
    address VARCHAR(255),
    comment TEXT,
    weight INT
);
```

> 참고: 실제 스키마는 프로젝트의 MyBatis XML 매퍼 파일을 참고하여 정확히 확인하세요.

## 연결 테스트

애플리케이션을 실행한 후 다음을 확인하세요:

1. **콘솔 로그 확인**
   - 데이터베이스 연결 성공 메시지 확인
   - 에러가 발생하면 설정값을 다시 확인

2. **API 테스트**
   - 회원가입 API 호출하여 데이터베이스에 저장되는지 확인
   - 로그인 API 호출하여 정상 작동하는지 확인

## 문제 해결

### 연결 실패 시 확인 사항:

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

3. **방화벽 확인**
   - 로컬 MySQL인 경우 방화벽 문제는 없지만, 원격 MySQL인 경우 포트 개방 확인

4. **사용자 권한 확인**
   ```sql
   SHOW GRANTS FOR 'your_username'@'localhost';
   ```

5. **데이터베이스 존재 확인**
   ```sql
   SHOW DATABASES;
   USE nav;
   SHOW TABLES;
   ```

## 보안 주의사항

1. **application-local.properties는 Git에 커밋하지 마세요**
   - `.gitignore`에 추가되어 있는지 확인
   - 민감한 정보(비밀번호 등)가 포함되어 있습니다

2. **비밀번호 관리**
   - 강력한 비밀번호 사용
   - 프로덕션 환경에서는 환경 변수나 시크릿 관리 도구 사용

## 추가 팁

- **데이터베이스 이름 변경**: 다른 이름을 사용하고 싶다면 `nav` 대신 원하는 이름으로 변경
- **원격 MySQL 사용**: 로컬이 아닌 원격 MySQL을 사용하는 경우, URL의 `localhost`를 IP 주소로 변경
- **다중 프로파일**: 개발/테스트/프로덕션 환경별로 여러 프로파일 파일 생성 가능

