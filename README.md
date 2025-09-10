# Guest Service

약속 참가자 관리를 담당하는 MSA 서비스입니다.

## 🏗️ 서비스 개요

Guest Service는 약속(Appointment) 참가자들의 등록, 상태 관리, 조회 기능을 제공하는 마이크로서비스입니다.

- **포트**: 8083
- **데이터베이스**: MySQL 8.0 (Docker 컨테이너)
- **개발 환경**: H2 인메모리 DB 지원

## 📋 주요 기능

- 약속 참가자 등록/취소
- 참가자 상태 관리 (coming, absent, late 등)
- 참가자 목록 조회
- 호스트 권한 기반 상태 변경
- 다른 MSA 서비스와의 연동

## 🔗 API 엔드포인트

### 1. 약속 관련 (Appointment Service 프록시)

#### 전체 약속 목록 조회
```http
GET /appointments
```

#### 특정 약속 상세 조회
```http
GET /appointments/{appointment_id}
```

### 2. 참가자 관리

#### 참가자 등록
```http
POST /appointments/{appointment_id}/guests
Content-Type: application/json

{
    "user_id": "user123",
    "guest_status": "coming"
}
```

**응답 예시:**
```json
{
    "guest_id": "guest1694123456789",
    "appointment_id": "appointment123",
    "user_id": "user123",
    "guest_status": "coming",
    "created_at": "2024-01-15T10:30:00",
    "updated_at": "2024-01-15T10:30:00"
}
```

#### 참가자 목록 조회
```http
GET /appointments/{appointment_id}/guests
```

#### 참가자 상태 조회
```http
GET /appointments/{appointment_id}/guests/{guest_id}/guest_status
```

#### 참가자 상태 변경 (호스트만 가능)
```http
PATCH /appointments/{appointment_id}/guests/{guest_id}/guest_status
Content-Type: application/json
X-User-ID: {host_user_id}

{
    "guest_status": "late"
}
```

#### 참가 취소
```http
DELETE /appointments/{appointment_id}/guests/{guest_id}
```

## 🏛️ 아키텍처

### 데이터베이스 스키마

```sql
CREATE TABLE guests (
    guest_id VARCHAR(255) PRIMARY KEY,
    appointment_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    guest_status VARCHAR(50) DEFAULT 'coming',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY unique_appointment_user (appointment_id, user_id)
);
```

### 주요 엔티티

#### Guest Entity
- `guest_id`: 참가자 고유 ID (타임스탬프 기반 생성)
- `appointment_id`: 약속 ID
- `user_id`: 사용자 ID
- `guest_status`: 참가 상태 (coming, absent, late 등)
- `created_at`, `updated_at`: 생성/수정 시간

## 🔄 MSA 서비스 연동

### 1. User Service 연동
- **목적**: 사용자 정보 검증
- **엔드포인트**: `GET /users/{userId}`
- **사용 시점**: 참가자 등록 시, 상태 변경 권한 확인 시
- **설정**: `services.user.url` 환경변수로 URL 설정

### 2. Appointment Service 연동
- **목적**: 약속 정보 조회, 호스트 권한 검증
- **엔드포인트**: 
  - `GET /appointments` - 전체 약속 목록
  - `GET /appointments/{appointmentId}` - 특정 약속 정보
- **사용 시점**: 호스트 권한 확인, 약속 정보 프록시 제공
- **설정**: `appointment.service.url` (기본값: http://localhost:8081)

## 🎯 비즈니스 로직

### 참가자 등록 프로세스
1. User Service에서 사용자 존재 여부 확인
2. 중복 참가 방지 (동일 약속에 동일 사용자 중복 등록 불가)
3. Guest ID 생성 (타임스탬프 기반)
4. 데이터베이스에 저장

### 상태 변경 프로세스
1. Guest 존재 여부 확인
2. Appointment Service에서 약속 정보 조회
3. 요청자가 호스트인지 권한 확인
4. 상태 업데이트 수행

### 권한 관리
- **호스트 권한**: Appointment Service에서 `host_id` 확인
- **참가자 본인**: 자신의 참가 취소만 가능
- **상태 변경**: 호스트만 가능

## 🗄️ 더미 데이터 현황

현재 서비스는 더미 데이터를 별도로 제공하지 않으며, 실제 데이터는 다음과 같이 생성됩니다:

### Guest 데이터 생성 방식
- **Guest ID**: `"guest" + System.currentTimeMillis()` 형태로 자동 생성
- **기본 상태**: `"coming"`으로 설정
- **타임스탬프**: 등록/수정 시점 자동 기록

### 테스트 환경 (H2 DB)
- 개발 환경에서는 H2 인메모리 데이터베이스 사용
- `application-dev.yml` 프로파일로 설정
- H2 Console: `http://localhost:8083/h2-console`

## 🐳 Docker 환경

### docker-compose.yml
```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: guest_service
    ports:
      - "3306:3306"
      
  guest-service:
    build: .
    ports:
      - "8083:8083"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/guest_service
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
```

## 🚀 실행 방법

### 1. Docker Compose 실행
```bash
docker-compose up -d
```

### 2. 개발 환경 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 3. 환경변수 설정
```bash
# .env 파일 생성
USER_SERVICE_URL=http://localhost:8082
APPOINTMENT_SERVICE_URL=http://localhost:8081
```

## 📊 모니터링

### Health Check
```http
GET /actuator/health
```

### 개발 환경 DB 콘솔
- URL: `http://localhost:8083/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (비어있음)

## 🔍 참고 서비스

Guest Service 개발 시 다음 서비스들을 참고하세요:

1. **User Service** (포트: 8082)
   - 사용자 정보 관리
   - 인증/인가 기능
   - API: `GET /users/{userId}`

2. **Appointment Service** (포트: 8081)
   - 약속 생성/관리
   - 호스트 권한 관리
   - API: `GET /appointments`, `GET /appointments/{id}`

3. **Notification Service** (향후 연동 예정)
   - 참가자 상태 변경 알림
   - 실시간 알림 기능

## 🛠️ 기술 스택

- **Framework**: Spring Boot 3.5.5
- **Language**: Java 17
- **Database**: MySQL 8.0, H2 (개발환경)
- **ORM**: Spring Data JPA
- **HTTP Client**: WebClient (Spring WebFlux)
- **Build Tool**: Gradle
- **Container**: Docker

## 📝 개발 규칙

- 필드명은 snake_case 사용 (예: `guest_id`, `user_id`)
- API 응답은 RESTful 설계 원칙 준수
- 에러 처리는 적절한 HTTP 상태 코드와 메시지 제공
- 트랜잭션 관리는 서비스 레이어에서 처리
