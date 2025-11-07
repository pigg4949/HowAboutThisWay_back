# MySQL 스키마 생성 오류 해결 가이드

## 발생한 오류

```
ERROR 1044: Access denied for user 'pigg4949'@'localhost' to database 'hatw'
```

**번역:** 사용자 'pigg4949'@'localhost'가 데이터베이스 'hatw'에 대한 접근이 거부되었습니다.

## 원인 분석

`pigg4949` 사용자가 `hatw` 스키마(데이터베이스)를 생성하려고 시도했지만, **데이터베이스를 생성할 권한(CREATE 권한)이 없어서** 발생한 오류입니다.

### 가능한 원인들:

1. **권한 부족 (가장 가능성 높음)**

   - `pigg4949` 사용자에게 `CREATE` 권한이 부여되지 않음
   - 또는 `ALL PRIVILEGES` 권한이 없음

2. **사용자 미생성**

   - `pigg4949` 사용자가 MySQL에 존재하지 않을 수 있음

3. **비밀번호 오류**
   - MySQL Workbench 연결 시 잘못된 비밀번호 사용

## 해결 방법

### 방법 1: root 사용자로 권한 부여 (권장)

#### 1단계: root 사용자로 MySQL Workbench 접속

- MySQL Workbench를 열고 `root` 사용자로 접속합니다.
- 또는 터미널에서:
  ```bash
  mysql -u root -p
  ```

#### 2단계: 권한 부여 SQL 실행

**옵션 A: 모든 데이터베이스에 대한 권한 부여 (개발 환경용)**

```sql
-- 사용자가 없으면 생성
CREATE USER IF NOT EXISTS 'pigg4949'@'localhost' IDENTIFIED BY 'your_password';

-- 모든 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'pigg4949'@'localhost';

-- 권한 즉시 적용
FLUSH PRIVILEGES;
```

**옵션 B: 특정 데이터베이스(hatw)에만 권한 부여 (더 안전함)**

```sql
-- 사용자가 없으면 생성
CREATE USER IF NOT EXISTS 'pigg4949'@'localhost' IDENTIFIED BY 'your_password';

-- hatw 데이터베이스에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON hatw.* TO 'pigg4949'@'localhost';

-- 권한 즉시 적용
FLUSH PRIVILEGES;
```

**옵션 C: CREATE 권한만 부여 (최소 권한 원칙)**

```sql
-- 사용자가 없으면 생성
CREATE USER IF NOT EXISTS 'pigg4949'@'localhost' IDENTIFIED BY 'your_password';

-- CREATE 권한만 부여
GRANT CREATE ON *.* TO 'pigg4949'@'localhost';

-- 권한 즉시 적용
FLUSH PRIVILEGES;
```

> ⚠️ **주의:** `your_password`를 실제 비밀번호로 변경하세요!

#### 3단계: 권한 확인

```sql
-- 권한이 제대로 부여되었는지 확인
SHOW GRANTS FOR 'pigg4949'@'localhost';
```

#### 4단계: MySQL Workbench 재연결

- MySQL Workbench에서 연결을 끊고 다시 `pigg4949` 사용자로 접속합니다.
- 이제 `hatw` 스키마를 생성할 수 있어야 합니다.

### 방법 2: root 사용자로 직접 스키마 생성

권한 부여가 복잡하다면, `root` 사용자로 직접 스키마를 생성할 수도 있습니다:

```sql
-- root 사용자로 접속한 후
CREATE DATABASE hatw CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 그 다음 pigg4949 사용자에게 해당 데이터베이스에 대한 권한 부여
GRANT ALL PRIVILEGES ON hatw.* TO 'pigg4949'@'localhost';
FLUSH PRIVILEGES;
```

## 단계별 실행 가이드

### MySQL Workbench에서 실행:

1. **root 사용자로 새 연결 생성**

   - MySQL Workbench 실행
   - `+` 버튼 클릭하여 새 연결 추가
   - Connection Name: `root_local`
   - Username: `root`
   - Password: root 비밀번호 입력
   - Test Connection 클릭하여 연결 확인
   - OK 클릭

2. **root 연결로 접속**

   - 생성한 `root_local` 연결 더블클릭

3. **SQL 스크립트 실행**

   - Query 탭 열기
   - 위의 권한 부여 SQL 복사하여 붙여넣기
   - `your_password`를 실제 비밀번호로 변경
   - Execute 버튼 클릭 (또는 Ctrl+Enter)

4. **pigg4949 사용자로 재연결**
   - 연결 종료
   - `pigg4949` 사용자로 다시 접속
   - 스키마 생성 재시도

### 터미널(CMD/PowerShell)에서 실행:

```bash
# root로 MySQL 접속
mysql -u root -p

# 비밀번호 입력 후, 위의 SQL 명령 실행
```

## 확인 사항

권한 부여 후 다음을 확인하세요:

```sql
-- 1. 사용자 존재 확인
SELECT user, host FROM mysql.user WHERE user = 'pigg4949';

-- 2. 권한 확인
SHOW GRANTS FOR 'pigg4949'@'localhost';

-- 3. 데이터베이스 목록 확인
SHOW DATABASES;
```

## 추가 문제 해결

### 문제 1: "Access denied for user 'root'@'localhost'"

- root 비밀번호를 잊어버린 경우:
  - MySQL 서비스를 중지하고 안전 모드로 시작
  - 비밀번호 재설정

### 문제 2: "Unknown database 'hatw'"

- 데이터베이스가 아직 생성되지 않았습니다.
- 먼저 데이터베이스를 생성하거나, root로 생성한 후 권한 부여

### 문제 3: 여전히 권한 오류 발생

- MySQL Workbench 연결을 완전히 종료하고 다시 접속
- 또는 MySQL 서비스 재시작:
  ```bash
  # Windows
  net stop MySQL80
  net start MySQL80
  ```

## 보안 권장사항

1. **최소 권한 원칙**

   - 필요한 권한만 부여
   - 프로덕션 환경에서는 `ALL PRIVILEGES ON *.*` 사용 지양

2. **강력한 비밀번호 사용**

   - `pigg4949` 사용자 비밀번호를 강력하게 설정

3. **특정 데이터베이스만 권한 부여**
   - `GRANT ALL PRIVILEGES ON hatw.*` 형식 사용 권장

## 참고

- MySQL 공식 문서: [GRANT Statement](https://dev.mysql.com/doc/refman/8.0/en/grant.html)
- 권한 종류: SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER 등
