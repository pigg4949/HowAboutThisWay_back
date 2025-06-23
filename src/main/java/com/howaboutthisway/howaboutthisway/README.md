# 👩‍🦽 HowAboutThisWay: 교통약자를 위한 스마트 길안내 서비스

장애인, 고령자, 임산부 등 교통약자를 위해 설계된 **스마트 길안내 시스템**입니다.  
계단, 단차, 급경사 등을 회피하고, 엘리베이터·슬로프 등의 **이동 편의 시설**을 자동으로 경로에 포함시켜  
**보다 안전하고 효율적인 이동 경로**를 추천합니다.

---

## 📌 프로젝트 개요

| 항목            | 내용                                                                 |
|----------------|----------------------------------------------------------------------|
| 프로젝트명      | HowAboutThisWay                                                      |
| 개발 기간       | 2025.05.01 ~ 2025.06.20                                              |
| 팀명            | 교통혁신단                                                           |
| 주요 기능       | 교통약자 전용 경로탐색, 실시간 장애물 회피, 다중 POI 기반 검색, 출입구 좌표 제공 |
| 주요 기술 스택  | Java, Spring Boot, MySQL, MyBatis, GeoTools, VWorld API, Tmap API, HTML/CSS/JS |

---

## 🧠 기획 배경

기존 길안내 서비스는 주로 자동차 중심 또는 비장애인 기준의 경로를 제공합니다.  
이에 우리는 실제 **교통약자의 시선**에서 **진입 가능성과 안전성**을 고려한 경로 탐색 알고리즘을 구축하고,  
공공 데이터를 기반으로 접근성 정보를 시각화해 **길에서의 불안을 최소화**하고자 합니다.

---

## 🛠️ 주요 기능

### 🔹 사용자 기능
- 원하는 목적지를 입력하면 엘리베이터, 슬로프, 낮은 경사 등을 포함한 최적 경로 제공
- 높은 계단, 공사장, 단차 등 교통약자 통행 장애 요소 회피
- Tmap 및 VWorld 기반 장소 검색 및 좌표 자동 추출
- 도착지 근처 다중 출입구/건물 입구 중 선택 가능

### 🔹 관리자 기능
- 시설물(에스컬레이터, 엘리베이터 등) 위치 정보 등록 및 관리
- 노후화/보수 중 시설 표시 및 비활성화 처리

---

## ⚙️ 기술 스택

| 구분         | 사용 기술                                                   |
|--------------|--------------------------------------------------------------|
| Language     | Java 17, JavaScript (ES6)                                    |
| Framework    | Spring Boot 3, MyBatis, JSTL                                  |
| DB           | MySQL                                                        |
| API          | Tmap API, VWorld POI API, 서울시 OpenAPI                     |
| GIS 분석     | GeoTools, QGIS, SRTM 데이터                                   |
| 기타         | Postman, Git, IntelliJ, VSCode                               |

---

## 🗂️ 프로젝트 구조 (간략 요약)

📁 src
┣ 📂 main
┃ ┣ 📂 java.com.howaboutthisway
┃ ┃ ┣ 📂 controller # Spring 컨트롤러
┃ ┃ ┣ 📂 service # 비즈니스 로직
┃ ┃ ┣ 📂 mapper # MyBatis 인터페이스
┃ ┃ ┣ 📂 model # DTO, VO 클래스
┃ ┃ ┗ 📂 util # Tmap, VWorld, GeoTools 도우미
┃ ┗ 📂 resources
┃ ┣ 📂 static # 정적 리소스: JS, CSS, 이미지
┃ ┣ 📂 templates # EJS/JSP 템플릿
┃ ┗ 📂 mapper # MyBatis 매퍼 XML

yaml
복사
편집

---

## 📈 ERD (요약)

> 실제 ERD는 `/docs/ERD.png` 또는 Notion 링크 등으로 첨부하세요.

- 사용자 테이블 (`User`)
- 교통약자 시설 테이블 (`Facility`)
- 시설 종류 (`FacilityType`)
- 경로 요청/응답 로그 (`RouteLog`)
- 장소 검색 결과 캐시 (`PoiResult`)

---

## 👥 팀 소개

| 이름 (역할) | 주요 담당 |
|-------------|-----------|
| 👨‍💼 [팀장 이름] | **총괄**, ERD 설계, HTML/JS 퍼블리싱, Spring Controller |
| 👩‍💻 [팀원 B] | **리뷰 및 시설 관리 기능**, 관리자 페이지 설계 |
| 👨‍🔧 [팀원 C] | **회원가입/로그인**, 사용자 인증/보안 로직 |
| 🧑‍🎨 [팀원 D] | **프론트엔드** (템플릿 공통 레이아웃, 반응형 UI) |
| 👨‍💻 [팀원 E] | **경로탐색 로직**, VWorld/Tmap API 연동 |
| 👩‍💻 [팀원 F] | **GeoTools 활용 공간데이터 파싱/좌표 변환**, QGIS 도식화 |

> 💬 개인 연락처/학번/소속 등은 직접 수정해주세요.

---

## 🔐 설치 및 실행 방법

1. 프로젝트 클론
```bash
git clone https://github.com/your-repo/howaboutthisway.git
MySQL DB 생성 및 application.yml 설정
(src/main/resources/application.yml 또는 .properties 참고)

빌드 및 실행 (IntelliJ or CLI)

bash
복사
편집
./mvnw spring-boot:run
접속

arduino
복사
편집
http://localhost:8080
📄 라이선스
본 프로젝트는 공공 데이터 기반으로 학술/비상업 목적의 개발에 사용됩니다.
저작권 관련 사항은 LICENSE 파일을 참고하세요.

📬 문의
팀장 이메일 또는 공용 Google Form 등으로 수정 가능

📧 howaboutthisway.team@kmail.com
📘 관련 문서: Notion 링크, Figma 링크 등

yaml
복사
편집

---

## 📌 다음 단계?

- 이미지 첨부: ERD, UI, 메인 화면
- 실 서버 URL 또는 배포 방법 추가
- API 명세 링크 추가 (Swagger or Postman Collection)

---