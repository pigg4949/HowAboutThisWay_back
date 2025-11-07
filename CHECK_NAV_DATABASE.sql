-- 기존 nav 데이터베이스 구조 확인 및 필요한 컬럼 추가 SQL
-- MySQL Workbench에서 nav 데이터베이스에 연결한 후 실행하세요

USE nav;

-- ============================================
-- 1. 테이블 목록 확인
-- ============================================
SHOW TABLES;

-- ============================================
-- 2. 각 테이블 구조 확인
-- ============================================

-- users 테이블 구조 확인
DESCRIBE users;

-- bookmarks 테이블 구조 확인
DESCRIBE bookmarks;

-- reports 테이블 구조 확인
DESCRIBE reports;

-- markers 테이블 구조 확인
DESCRIBE markers;

-- inquiries 테이블 구조 확인
DESCRIBE inquiries;

-- ============================================
-- 3. 필요한 컬럼 추가 (없는 경우에만)
-- ============================================

-- users 테이블에 isAdmin 컬럼 추가 (없는 경우)
-- 먼저 컬럼이 있는지 확인 후 실행하세요
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS isAdmin BOOLEAN DEFAULT FALSE COMMENT '관리자 여부(0 = 일반, 1 = 관리자)' 
AFTER isActive;

-- markers 테이블에 stationName 컬럼 추가 (없는 경우)
ALTER TABLE markers 
ADD COLUMN IF NOT EXISTS stationName VARCHAR(100) COMMENT '역명 (지하철 역 마커용)' 
AFTER weight;

-- ============================================
-- 4. 컬럼 존재 여부 확인 (MySQL 8.0 이상)
-- ============================================

-- users 테이블의 isAdmin 컬럼 확인
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'nav' 
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'isAdmin';

-- markers 테이블의 stationName 컬럼 확인
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'nav' 
  AND TABLE_NAME = 'markers'
  AND COLUMN_NAME = 'stationName';

-- ============================================
-- 5. 전체 테이블 구조 확인
-- ============================================

-- 모든 테이블의 컬럼 정보 확인
SELECT 
    TABLE_NAME,
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT,
    COLUMN_KEY,
    EXTRA
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'nav'
ORDER BY TABLE_NAME, ORDINAL_POSITION;

