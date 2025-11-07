# 이길어때 (HowAboutThisWay) - 교통약자 전용 네비게이션 백엔드 서비스

## 프로젝트 개요

"이길어때"는 교통약자를 위한 전용 네비게이션 서비스의 백엔드 시스템입니다. T-Map API를 활용하여 보행자 경로 및 대중교통 경로를 제공하고, 사용자 맞춤형 서비스를 제공합니다.

## 기술 스택

### 프레임워크 및 언어

- **Java 17**
- **Spring Boot 3.5.3**
- **Gradle** (빌드 도구)

### 데이터베이스

- **MySQL** (데이터베이스)
- **MyBatis 3.0.3** (ORM 프레임워크)

### 인증 및 보안

- **JWT (JSON Web Token)** - 인증 토큰 관리
- **BCrypt** - 비밀번호 암호화
- **세션 기반 인증** - HttpSession 활용

### 외부 API 연동

- **T-Map API**
  - POI 검색
  - 보행자 경로 안내
  - 대중교통 경로 안내
- **카카오 OAuth 2.0** - 소셜 로그인
- **구글 OAuth 2.0** - 소셜 로그인
- **CoolSMS** - SMS 인증 서비스

### 기타 라이브러리

- **Lombok** - 보일러플레이트 코드 감소
- **Gson** - JSON 처리
- **Jackson** - JSON 직렬화/역직렬화

## 프로젝트 구조

```
src/main/java/com/HATW/
├── controller/          # REST API 컨트롤러
│   ├── AdminController.java      # 관리자 기능
│   ├── AuthController.java       # 인증 관련 (SMS 인증)
│   ├── BookmarkerController.java # 북마크 관리
│   ├── MapController.java        # 지도 및 경로 검색
│   ├── PageController.java       # 페이지 컨트롤러
│   ├── ReportController.java     # 신고 기능
│   └── UserController.java       # 사용자 관리
├── service/             # 비즈니스 로직
│   ├── AdminService.java
│   ├── AdminServiceImpl.java
│   ├── BookmarkService.java
│   ├── BookmarkServiceImpl.java
│   ├── MapService.java
│   ├── MapServiceImpl.java
│   ├── ReportService.java
│   ├── ReportServiceImpl.java
│   ├── UserService.java
│   └── UserServiceImpl.java
├── mapper/              # MyBatis Mapper 인터페이스
│   ├── BookmarkerMapper.java
│   ├── InquiryMapper.java
│   ├── MarkerMapper.java
│   ├── ReportMapper.java
│   └── UserMapper.java
├── dto/                 # 데이터 전송 객체
│   ├── BookmarkerDTO.java
│   ├── InquiryDTO.java
│   ├── MarkerDTO.java
│   ├── ReportDTO.java
│   ├── RouteRequestDTO.java
│   ├── SearchHistoryDTO.java
│   └── UserDTO.java
├── util/                # 유틸리티 클래스
│   ├── AdminRoleFilter.java      # 관리자 권한 필터
│   ├── GoogleUtil.java           # 구글 OAuth 유틸
│   ├── JwtAuthenticationFilter.java  # JWT 인증 필터
│   ├── JwtTokenProvider.java     # JWT 토큰 생성/검증
│   ├── KakaoUtil.java            # 카카오 OAuth 유틸
│   ├── LoginCheckFilter.java     # 로그인 체크 필터
│   ├── PasswordEncoderUtil.java  # 비밀번호 인코더
│   ├── PasswordUtil.java         # 비밀번호 유틸
│   ├── SessionUtil.java          # 세션 유틸
│   └── SmsService.java           # SMS 서비스
└── dao/                 # 데이터 접근 객체
    └── MapDAO.java

src/main/resources/
├── application.properties    # 애플리케이션 설정
└── mapper/                  # MyBatis XML 매퍼
    ├── BookmarkerMapper.xml
    ├── inquiryMapper.xml
    ├── MarkerMapper.xml
    ├── ReportMapper.xml
    └── UserMapper.xml
```

## 주요 기능

### 1. 사용자 관리 (UserController)

- **회원가입** (`POST /api/users/register`)
  - 사용자 정보 등록
  - BCrypt를 사용한 비밀번호 암호화
- **로그인/로그아웃** (`POST /api/users/login`, `POST /api/users/logout`)
  - 세션 기반 인증
- **사용자 정보 조회/수정** (`GET /api/users/me`, `PUT /api/users/me`)
- **회원 탈퇴** (`DELETE /api/users/me`)
- **아이디 찾기** (`POST /api/users/find-id`)
- **비밀번호 찾기** (`POST /api/users/find-password`)
- **전화번호 인증** (`POST /api/users/verify-phone`)

### 2. 지도 및 경로 검색 (MapController)

- **POI 검색** (`GET /api/map/searchLocation`)
  - T-Map API를 통한 장소 검색
  - 키워드 기반 검색
- **보행자 경로 안내** (`POST /api/map/pedestrianRoute`)
  - 출발지/도착지 좌표 기반 보행 경로 제공
  - T-Map 보행자 경로 API 연동
- **대중교통 경로 안내** (`POST /api/map/transitRoute`)
  - 대중교통 경로 검색 및 안내
  - T-Map 대중교통 API 연동
- **경로 로그 저장** (`POST /api/map/routeLog`)
  - 사용자 경로 검색 이력 저장 (선택적)

### 3. 북마크 관리 (BookmarkerController)

- **북마크 조회** (`GET /api/bookmarks`, `GET /api/bookmarks/user/{userId}`)
  - 전체 북마크 또는 사용자별 북마크 조회
- **북마크 생성** (`POST /api/bookmarks`)
- **북마크 수정** (`PUT /api/bookmarks/{bookmarkId}`)
- **북마크 삭제** (`DELETE /api/bookmarks/{bookmarkId}`)

### 4. 신고 기능 (ReportController)

- **신고 제출** (`POST /api/reports`)
  - 이미지 파일 포함 신고 제출
- **내 신고 조회** (`GET /api/reports/my`)
- **신고 삭제** (`DELETE /api/reports/{reportId}`)

### 5. 관리자 기능 (AdminController)

- **전체 신고 조회** (`GET /api/admin/reports`)
- **신고 승인** (`POST /api/admin/reports/{id}/approve`)
- **신고 거부** (`POST /api/admin/reports/{id}/reject`)
- **신고 답변** (`POST /api/admin/reports/{id}/reply`)

### 6. 인증 기능 (AuthController)

- **SMS 인증번호 전송** (`POST /auth/send-code`)
- **인증번호 검증** (`POST /auth/verify-code`)
- **비밀번호 재설정** (`POST /auth/reset-password`)

## 설정 파일 (application.properties)

### 데이터베이스 설정

```properties
spring.datasource.url=jdbc:mysql://192.168.162.207:3306/nav?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=admin
spring.datasource.password=1234
```

### T-Map API 설정

- POI 검색 URL
- 보행자 경로 API URL
- 대중교통 경로 API URL
- AppKey 설정

### OAuth 설정

- 카카오 OAuth (Client ID, Redirect URI, Token URL)
- 구글 OAuth (Client ID, Client Secret, Redirect URI, Token URL)

### 기타 설정

- JWT Secret Key
- CoolSMS API 설정
- 파일 업로드 디렉토리 (`reportImgs/`)

## 작동 방식

### 1. 인증 흐름

1. 사용자가 로그인 요청
2. `UserService`에서 사용자 정보 조회 및 비밀번호 검증
3. 검증 성공 시 세션에 사용자 ID 저장
4. JWT 토큰 생성 (선택적)
5. 이후 요청은 세션 또는 JWT로 인증 확인

### 2. 경로 검색 흐름

1. 클라이언트에서 출발지/도착지 좌표 전송
2. `MapController`에서 요청 수신
3. `MapService`에서 T-Map API 호출
4. T-Map API 응답을 JSON으로 변환하여 클라이언트에 반환
5. (선택적) 경로 로그를 데이터베이스에 저장

### 3. 소셜 로그인 흐름

1. 클라이언트에서 OAuth 인증 후 Access Token 전송
2. `KakaoUtil` 또는 `GoogleUtil`을 통해 사용자 정보 조회
3. 사용자 정보를 기반으로 회원가입 또는 로그인 처리

### 4. 신고 처리 흐름

1. 사용자가 신고 내용 및 이미지 업로드
2. `ReportService`에서 신고 정보 저장
3. 관리자가 신고 목록 조회 및 승인/거부 처리

## 데이터베이스 스키마

주요 테이블 (추정):

- `users` - 사용자 정보
- `bookmarks` - 북마크 정보
- `reports` - 신고 정보
- `markers` - 지도 마커 정보
- `inquiries` - 문의사항
- `search_history` - 검색 이력

## API 엔드포인트 요약

### 사용자 관련

- `POST /api/users/register` - 회원가입
- `POST /api/users/login` - 로그인
- `POST /api/users/logout` - 로그아웃
- `GET /api/users/me` - 내 정보 조회
- `PUT /api/users/me` - 내 정보 수정
- `DELETE /api/users/me` - 회원 탈퇴

### 지도/경로 관련

- `GET /api/map/searchLocation?keyword={keyword}` - 장소 검색
- `POST /api/map/pedestrianRoute` - 보행자 경로
- `POST /api/map/transitRoute` - 대중교통 경로
- `POST /api/map/routeLog` - 경로 로그 저장

### 북마크 관련

- `GET /api/bookmarks` - 전체 북마크 조회
- `GET /api/bookmarks/user/{userId}` - 사용자별 북마크 조회
- `POST /api/bookmarks` - 북마크 생성
- `PUT /api/bookmarks/{bookmarkId}` - 북마크 수정
- `DELETE /api/bookmarks/{bookmarkId}` - 북마크 삭제

### 신고 관련

- `POST /api/reports` - 신고 제출
- `GET /api/reports/my` - 내 신고 조회
- `DELETE /api/reports/{reportId}` - 신고 삭제

### 관리자 관련

- `GET /api/admin/reports` - 전체 신고 조회
- `POST /api/admin/reports/{id}/approve` - 신고 승인
- `POST /api/admin/reports/{id}/reject` - 신고 거부
- `POST /api/admin/reports/{id}/reply` - 신고 답변

### 인증 관련

- `POST /auth/send-code` - SMS 인증번호 전송
- `POST /auth/verify-code` - 인증번호 검증
- `POST /auth/reset-password` - 비밀번호 재설정

## 빌드 및 실행

### 빌드

```bash
./gradlew build
```

### 실행

```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/HowAboutThisWay-0.0.1-SNAPSHOT.jar
```

## 개발 환경 설정

1. Java 17 설치
2. MySQL 데이터베이스 설정
3. `application.properties` 파일에 데이터베이스 및 API 키 설정
4. T-Map API 키 발급 및 설정
5. 카카오/구글 OAuth 앱 등록 및 설정
6. CoolSMS API 키 설정 (SMS 인증 사용 시)

## CORS 설정

현재 프론트엔드는 `http://localhost:5173`에서 실행되는 것으로 설정되어 있습니다.

```java
@CrossOrigin(origins = "http://localhost:5173")
```

## 보안 고려사항

- 비밀번호는 BCrypt로 해시화하여 저장
- JWT 토큰을 통한 인증 지원
- 세션 기반 인증 지원
- 관리자 권한 필터링 (`AdminRoleFilter`)
- 로그인 체크 필터링 (`LoginCheckFilter`)

## 향후 개선 사항

- 경로 로그 저장 기능 구현 (현재는 콘솔 출력만)
- JWT 토큰 만료 시간 설정 (`application.properties`에 `jwt.expiration` 추가 필요)
- 에러 핸들링 개선
- API 문서화 (Swagger/OpenAPI)
- 단위 테스트 및 통합 테스트 추가

## 라이선스

이 프로젝트는 개인/팀 프로젝트입니다.
