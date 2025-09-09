-- Koffe Konnect - DDL per user spec (MySQL)

-- 1) User Service
CREATE DATABASE IF NOT EXISTS user_service;
USE user_service;

CREATE TABLE users (
  user_id   VARCHAR(100) PRIMARY KEY,  -- 사내 이메일 형식
  is_admin  BOOLEAN,                   -- 회원가입 페이지에는 노출 X
  username  VARCHAR(50),               -- 이름
  nickname  VARCHAR(50),               -- 닉네임
  password  VARCHAR(255)               -- 비밀번호
);

-- 2) Appointment Service
CREATE DATABASE IF NOT EXISTS appointment_service;
USE appointment_service;

CREATE TABLE appointments (
  appointment_id     VARCHAR(100) PRIMARY KEY,  -- 약속 고유 ID
  host_id            VARCHAR(100),              -- FK: user_service.user_id (논리 참조)
  title              VARCHAR(200),              -- 선택옵션
  description        TEXT,                      -- 약속 설명
  start_time         TIMESTAMP,                 -- 시작 시간
  end_time           TIMESTAMP,                 -- 종료 시간
  location_id        VARCHAR(100),              -- FK: location_service.location_id (논리 참조)
  appointment_status VARCHAR(20)                -- planned / ongoing / done / cancelled
);

-- 3) Guest Service
CREATE DATABASE IF NOT EXISTS guest_service;
USE guest_service;

CREATE TABLE guests (
  guest_id       VARCHAR(100) PRIMARY KEY,      -- 참가자 고유 ID
  appointment_id VARCHAR(100),                  -- FK: appointment_service.appointment_id (논리 참조)
  user_id        VARCHAR(100),                  -- FK: user_service.user_id (논리 참조)
  guest_status   VARCHAR(20)                    -- coming / came / noshow
);

-- 4) Location Service
CREATE DATABASE IF NOT EXISTS location_service;
USE location_service;

CREATE TABLE locations (
  location_id VARCHAR(100) PRIMARY KEY,         -- 장소 고유 ID
  building    VARCHAR(100),                     -- 건물명
  floor       VARCHAR(50)                       -- 층수
);

-- 5) Notification Service
CREATE DATABASE IF NOT EXISTS notification_service;
USE notification_service;

CREATE TABLE notifications (
  notification_id  VARCHAR(100) PRIMARY KEY,    -- 알림 고유 ID
  user_id          VARCHAR(100),                -- FK: appointment_service.host_id (논리 참조)
  appointment_id   VARCHAR(100),                -- FK: appointment_service.appointment_id (논리 참조)
  guest_id         VARCHAR(100),                -- 신규
  notification_time TIMESTAMP                   -- FK: appointment_service.end_time (논리 참조)
);
