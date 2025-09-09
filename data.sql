/* =========================
   Single DB: user_service (per DDL spec)
   ========================= */
CREATE DATABASE IF NOT EXISTS user_service;
USE user_service;

/* Clean up (drop children first) */
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS guests;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS users;

/* =========================
   1) USERS
   ========================= */
CREATE TABLE users (
  user_id   VARCHAR(100) PRIMARY KEY,   -- corporate email (@kt.com)
  is_admin  BOOLEAN,
  username  VARCHAR(50),
  nickname  VARCHAR(50),
  password  VARCHAR(255)
);

INSERT INTO users (user_id, is_admin, username, nickname, password) VALUES
('IamZero@kt.com',   1, 'Nayoung Kim',   'IamZero',  '$2y$...IamZero'),
('cuttiegyu@kt.com',    0, 'Taegyu Lim',    'cuttiegyu',   '$2y$...cuttiegyu'),
('emperor@kt.com',     0, 'Hyeonji Hwang', 'emperor',    '$2y$...emperor'),
('me@kt.com',       0, 'Hyejee Kim',    'me',      '$2y$...me'),
('thumb@kt.com',    0, 'Jimin Lee',     'thumb',   '$2y$...thumb'),
('roll@kt.com',     0, 'Yerak Kim',     'roll',    '$2y$...roll'),
('unniejjang@kt.com',     0, 'Jimin Park',    'unniejjang',    '$2y$...unniejjang'),
('sky@kt.com',      0, 'Haneul Kim',    'sky',     '$2y$...sky'),
('swim@kt.com',     0, 'Sooyoung Jung', 'swim',    '$2y$...swim'),
('pikachu@kt.com',  0, 'Jiwoo Kim',     'pikachu', '$2y$...pikachu');

/* =========================
   2) LOCATIONS
   ========================= */
CREATE TABLE locations (
  location_id VARCHAR(100) PRIMARY KEY,
  building    VARCHAR(100),
  floor       VARCHAR(50)
);

INSERT INTO locations (location_id, building, floor) VALUES
('1001', '광화문', 'B1'),
('1002', '광화문', '1F'),
('1003', '판교',   '12F'),
('1004', '광화문', '3F'),
('1005', '양재',   '7F'),
('1006', '분당',   '2F'),
('1007', '광화문', '5F'),
('1008', '판교',   '4F'),
('1009', '판교',   '9F'),
('1010', '분당',   '6F');

/* =========================
   3) APPOINTMENTS
   ========================= */
CREATE TABLE appointments (
  appointment_id     VARCHAR(100) PRIMARY KEY,
  host_id            VARCHAR(100),   -- FK (logical): users.user_id
  title              VARCHAR(200),
  description        TEXT,
  start_time         TIMESTAMP,
  end_time           TIMESTAMP,
  location_id        VARCHAR(100),   -- FK (logical): locations.location_id
  appointment_status VARCHAR(20)     -- planned / ongoing / done / cancelled
);

INSERT INTO appointments
(appointment_id, host_id, title, description, start_time, end_time, location_id, appointment_status)
VALUES
('2001', 'IamZero@kt.com',  'Sprint Planning', '나영이가 좋아영', '2025-09-09 10:00:00', '2025-09-09 11:00:00', '1003', 'planned'),
('2002', 'cuttiegyu@kt.com',   '1:1 Meeting',     '태규 오빠, 방울 토마토 먹을래?', '2025-09-09 13:00:00', '2025-09-09 13:30:00', '1002', 'planned'),
('2003', 'roll@kt.com',    'Design Review',   '예락이는 퀸이야', '2025-09-10 15:00:00', '2025-09-10 16:00:00', '1004', 'planned'),
('2004', 'unniejjang@kt.com',    'DB Migration',    '지민 언니 보고싶어어',  '2025-09-10 20:00:00', '2025-09-10 22:00:00', '1005', 'planned'),
('2005', 'sky@kt.com',     'Daily Standup',   '가을 여자 김하늘',       '2025-09-09 09:30:00', '2025-09-09 09:45:00', '1002', 'ongoing'),
('2006', 'swim@kt.com',    'OKR Check-in',    '수영 오빠가 수영했다',    '2025-09-11 11:00:00', '2025-09-11 11:30:00', '1008', 'planned'),
('2007', 'emperor@kt.com',    'Bug Bash',        '현지야 내가 언니야',    '2025-09-12 14:00:00', '2025-09-12 17:00:00', '1007', 'planned'),
('2008', 'thumb@kt.com',   'Partner Sync',    '엄지 공,,,쥬,,,?', '2025-09-09 16:00:00', '2025-09-09 16:45:00', '1006', 'planned'),
('2009', 'me@kt.com',      'Infra Review',    '시크 컨셉 다음주부터',     '2025-09-13 10:00:00', '2025-09-13 11:30:00', '1010', 'planned'),
('2010', 'pikachu@kt.com', 'Retrospective',   '피카츄 피카츄 피카츄',         '2025-09-14 17:00:00', '2025-09-14 18:00:00', '1009', 'planned');

/* =========================
   4) GUESTS
   ========================= */
CREATE TABLE guests (
  guest_id       VARCHAR(100) PRIMARY KEY,
  appointment_id VARCHAR(100),  -- FK (logical): appointments.appointment_id
  user_id        VARCHAR(100),  -- FK (logical): users.user_id
  guest_status   VARCHAR(20)    -- coming / came / noshow
);

INSERT INTO guests (guest_id, appointment_id, user_id, guest_status) VALUES
('3001', '2001', 'cuttiegyu@kt.com',   'coming'),
('3002', '2001', 'IamZero@kt.com',    'coming'),
('3003', '2002', 'emperor@kt.com',  'came'),
('3004', '2003', 'me@kt.com',      'coming'),
('3005', '2003', 'thumb@kt.com',   'coming'),
('3006', '2004', 'roll@kt.com',    'coming'),
('3007', '2005', 'sky@kt.com',     'came'),
('3008', '2006', 'unniejjang@kt.com',    'coming'),
('3009', '2007', 'swim@kt.com',    'coming'),
('3010', '2008', 'pikachu@kt.com', 'coming');

/* =========================
   5) NOTIFICATIONS
   ========================= */
CREATE TABLE notifications (
  notification_id  VARCHAR(100) PRIMARY KEY,
  user_id          VARCHAR(100),   -- host only (FK logical: users.user_id)
  appointment_id   VARCHAR(100),   -- FK logical: appointments.appointment_id
  guest_id         VARCHAR(100),   -- 신규 (옵션)
  notification_time TIMESTAMP
);

INSERT INTO notifications (notification_id, user_id, appointment_id, guest_id, notification_time) VALUES
('5001', 'IamZero@kt.com',  '2001', NULL, '2025-09-09 09:45:00'),
('5002', 'cuttiegyu@kt.com',   '2002', NULL, '2025-09-09 12:45:00'),
('5003', 'roll@kt.com',    '2003', NULL, '2025-09-10 14:45:00'),
('5004', 'unniejjang@kt.com',    '2004', NULL, '2025-09-10 19:30:00'),
('5005', 'sky@kt.com',     '2005', NULL, '2025-09-09 09:20:00'),
('5006', 'swim@kt.com',    '2006', NULL, '2025-09-11 10:45:00'),
('5007', 'emperor@kt.com',    '2007', NULL, '2025-09-12 13:45:00'),
('5008', 'thumb@kt.com',   '2008', NULL, '2025-09-09 15:30:00'),
('5009', 'me@kt.com',      '2009', NULL, '2025-09-13 09:30:00'),
('5010', 'pikachu@kt.com', '2010', NULL, '2025-09-14 16:30:00');
