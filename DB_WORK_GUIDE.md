# 데이터베이스 작업 가이드

## 현재 상태 확인

코드 오류 수정이 완료되었습니다. 이제 데이터베이스 작업을 진행할 수 있습니다.

## 데이터베이스 작업 전 체크리스트

### 1. MySQL 데이터베이스 준비

#### 데이터베이스 생성

```sql
CREATE DATABASE hatw CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hatw;
```

#### 또는 기존 데이터베이스 사용

- 기존 팀원의 데이터베이스: `nav` (192.168.162.207:3306)
- 개인 로컬 데이터베이스: `hatw` 또는 원하는 이름

### 2. 필요한 테이블 생성

프로젝트에서 사용하는 주요 테이블들:

#### users 테이블

```sql
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
    isAdmin BOOLEAN DEFAULT FALSE COMMENT '관리자 여부(0 = 일반, 1 = 관리자)',
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### bookmarks 테이블

```sql
CREATE TABLE bookmarks (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    label VARCHAR(100),
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### reports 테이블

```sql
CREATE TABLE reports (
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
```

#### markers 테이블

```sql
CREATE TABLE markers (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    type INT COMMENT '마커 타입',
    lon DOUBLE COMMENT '경도',
    lat DOUBLE COMMENT '위도',
    address VARCHAR(255) COMMENT '주소(지도 표시용)',
    comment TEXT COMMENT '설명(지도 표시용)',
    weight INT COMMENT '가중치(알고리즘용)',
    stationName VARCHAR(100) COMMENT '역명 (지하철 역 마커용)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### inquiries 테이블

```sql
CREATE TABLE inquiries (
    idx INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) NOT NULL,
    content TEXT NOT NULL COMMENT '문의 내용',
    adminResponses TEXT COMMENT '관리자 답변',
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. application.properties 설정

#### 로컬 개발 환경 (application-local.properties)

```properties
# 로컬 MySQL 데이터베이스 설정
spring.datasource.url=jdbc:mysql://localhost:3306/hatw?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

#### 팀원의 공용 데이터베이스 사용 시

```properties
# 공용 MySQL 데이터베이스 설정
spring.datasource.url=jdbc:mysql://192.168.162.207:3306/nav?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=admin
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 4. MyBatis Mapper XML 파일 확인

다음 XML 파일들이 올바르게 매핑되어 있는지 확인하세요:

- `src/main/resources/mapper/UserMapper.xml`
- `src/main/resources/mapper/BookmarkerMapper.xml`
- `src/main/resources/mapper/ReportMapper.xml`
- `src/main/resources/mapper/MarkerMapper.xml`
- `src/main/resources/mapper/inquiryMapper.xml`

### 5. 애플리케이션 실행 및 테스트

#### 프로파일 지정하여 실행

```bash
# Gradle 사용
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 환경 변수 설정
export SPRING_PROFILES_ACTIVE=local  # Linux/Mac
set SPRING_PROFILES_ACTIVE=local     # Windows CMD
$env:SPRING_PROFILES_ACTIVE="local"  # Windows PowerShell
```

#### 연결 테스트

애플리케이션 실행 후 콘솔 로그에서 다음을 확인:

- 데이터베이스 연결 성공 메시지
- 에러가 없으면 정상 연결

#### API 테스트

1. 회원가입 API 호출하여 데이터베이스에 저장되는지 확인
2. 로그인 API 호출하여 정상 작동하는지 확인

## 추가 작업 사항

### MarkerMapper XML에 추가할 쿼리

`src/main/resources/mapper/MarkerMapper.xml`에 다음 쿼리들을 추가해야 합니다:

```xml
<!-- 역명으로 마커 조회 -->
<select id="selectByStationName" resultType="com.HATW.dto.MarkerDTO">
    SELECT * FROM markers
    WHERE stationName LIKE CONCAT('%', #{stationName}, '%')
</select>

<!-- 타입 리스트로 마커 조회 -->
<select id="findMarkersByTypes" resultType="com.HATW.dto.MarkerDTO">
    SELECT * FROM markers
    WHERE type IN
    <foreach collection="types" item="type" open="(" separator="," close=")">
        #{type}
    </foreach>
</select>
```

### UserMapper XML 확인

`src/main/resources/mapper/UserMapper.xml`에 다음 메서드들이 정의되어 있는지 확인:

- `findByIdx`
- `findByPhoneNumber`
- `findByNameAndPhone`

## 문제 해결

### 연결 실패 시

1. MySQL 서비스 실행 확인
2. 포트 확인 (기본 3306)
3. 사용자 권한 확인
4. 데이터베이스 이름 확인

### 테이블이 없다는 오류

- 위의 CREATE TABLE 문을 실행하여 테이블 생성

### 외래 키 제약 조건 오류

- users 테이블을 먼저 생성한 후 다른 테이블 생성
- 또는 외래 키 제약 조건을 나중에 추가

## 다음 단계

1. ✅ 코드 오류 수정 완료
2. ⏳ 데이터베이스 생성 및 테이블 생성
3. ⏳ application.properties 설정
4. ⏳ MyBatis Mapper XML 확인/수정
5. ⏳ 애플리케이션 실행 및 테스트

데이터베이스 작업이 완료되면 알려주세요!
