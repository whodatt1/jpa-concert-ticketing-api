# JPA CONCERT TICKETING REST API

## 1. 프로젝트 개요

- **소개:**  본 프로젝트는 JPA 학습을 위한 콘서트 예매 환경을 가정한 REST API 서버입니다. 
- **목표:** 데이터 처리 시 발생하는 병목 현상을 최적화하고 분산 환경에서 정합성을 보장할 수 있도록 설계하였습니다.
- **아키텍처:** 도메인 별 응집도를 높인 계층형 구조(Domain-based Layered Architecture) 적용.

## 2. 기술 스택

- **Backend:** Java 21, Spring Boot 3.5, Spring Data JPA, Querydsl
- **Database:** H2, Redis (Lock)
- **Docs & Tools**: Swagger (Springdoc 2.8.9), Docker, Git, Gradle

### 3. 빌드 및 실행 가이드

본 프로젝트는 분산 락 처리를 위해 Redis를 사용하므로, 로컬 환경에 **Docker**가 설치 및 실행되어 있어야 합니다.

- Java 21
- Docker & Docker Compose

```bash
git clone [레포지토리 주소]
cd [프로젝트 폴더명]

# Docker Desktop
docker-compose up -d

# Windows (CMD / PowerShell)
.\gradlew clean build -x test
.\gradlew bootRun

# Mac / Linux (Terminal)
./gradlew clean build -x test 
./gradlew bootRun
```

서버 구동 시 `DataLoader`가 작동하여, API 테스트를 위한 **필수 마스터 데이터(공연장 3곳, 카테고리 4개)와 30건의 더미 콘서트**를 H2 DB에 자동 세팅합니다.

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **H2 Database:** http://localhost:8080/h2-console

## 4. 핵심 기술 및 트러블슈팅

### [1] 좌석 생성 병목 해결 (Bulk Insert)

- **문제:** 신규 콘서트 생성 시, 회차별로 좌석(Seat) 데이터를 JPA `saveAll()`로 생성할 때 심각한 성능 저하 및 지연 발생.
- **해결:** JPA의 영속성 컨텍스트를 우회하여, Spring JDBC의 `jdbcTemplate.batchUpdate()`활용한 벌크 인서트(Bulk Insert)로 전환. OOM을 방지하고 생성 소요 시간을 단축.

### [2] Redis 분산 락을 활용한 예매 동시성 제어

- **문제:** 콘서트 예매 시 동일한 좌석에 다수의 사용자가 동시에 접근할 때 발생하는 동시성 문제 및 데드락 위험.
- **해결(선점 락):** Redis를 활용하여 특정 좌석에 대해 5분 선점(Lock) 로직을 추가하여 데이터 정합성 보장.
- **해결(상태 복구 스케줄러):** Redis의 TTL이 만료되었으나 DB 상태와 불일치하는 유령 데이터를 방지하기 위해, 1분 주기의 `SeatRecoveryScheduler`를 도입하여 락 만료 좌석을 자동 복구.

### [3] Querydsl과 인덱스 튜닝을 통한 조회 성능 극대화

- **N+1 및 데이터 뻥튀기 방지:** 콘서트 디테일 조회 시 `MultipleBagFetchException`을 차단하기 위해 스케줄(Schedule)은 `fetchJoin`으로 가격(Price)은 `default_batch_fetch_size`를 활용한 지연 로딩으로 최적화.
- **서브쿼리 튜닝:** 카테고리 필터링 조회 시 N:M 테이블 JOIN으로 인한 데이터 중복을 방지하고자 `EXISTS`서브쿼리로 리팩토링.
- **인덱스 튜닝:** 
	- **콘서트 목록:** 정렬 최적화를 위해 `(del_yn, created_at)`복합 인덱스 적용
	- **좌석 맵:** 잦은 업데이트가 발생하는 상태(`status`) 컬럼을 배제하고 단일 컬럼(`conert_schedule_id`)으로 인덱스를 재설계하여 최적화

### [4] 안정적인 API 환경 및 Swagger 고도화

- **Global Exception:** `@RestControllerAdvice`를 활용한 전역 예외 처리 및 공통 응답 규격(ApiResponse/ApiErrorResponse)을 적용하여 혼란을 최소화.
- **Swagger 문서화:** `@ParameterObject`를 통해 `record`객체 및 페이징 파라미터를 명시적으로 노출, 마스터 데이터(카테고리, 공연장 ID)의 Example 값을 주입하여 API 테스트 편의성을 극대화.

## 5. 프로젝트 구조


```text
.
├── src
│   ├── main
│   │   ├── java/com/tiketing/api
│   │   │   ├── concert              # [공연 도메인] 공연, 공연장, 일정 관리
│   │   │   │   ├── controller       # API 엔드포인트 (ConcertController)
│   │   │   │   ├── dto              # 요청/응답 데이터 객체
│   │   │   │   ├── entity           # JPA 엔티티 (Concert, Venue, Category 등)
│   │   │   │   ├── enums            # 도메인 상수 (Rating, Status)
│   │   │   │   └── repository       # 데이터 접근 계층 (Querydsl 포함)
│   │   │   │
│   │   │   ├── reservation          # [예약 도메인] 좌석 예점유 및 예약 로직
│   │   │   │   ├── controller       # SeatController
│   │   │   │   ├── entity           # Seat 엔티티
│   │   │   │   ├── repository       # JDBC Bulk Insert 및 Querydsl 구현체
│   │   │   │   ├── scheduler        # 좌석 복구 스케줄러 (SeatRecovery)
│   │   │   │   └── service          # 핵심 예약 비즈니스 로직
│   │   │   │
│   │   │   ├── payment              # [결제 도메인] 결제 프로세스 관리
│   │   │   │   ├── controller       # PaymentController
│   │   │   │   └── service          # PaymentService
│   │   │   │
│   │   │   └── global               # [공통 모듈] 전역 설정 및 유틸리티
│   │   │       ├── config           # Querydsl, Swagger 등 외부 설정
│   │   │       ├── exception        # 공통 예외 처리 (GlobalExceptionHandler)
│   │   │       ├── init             # 초기 데이터 로딩 (DataLoader)
│   │   │       └── response         # 공통 API 응답 규격 (ApiResponse)
│   │   │
│   │   └── resources
│   │       ├── application.yaml     # 어플리케이션 설정
│   │       └── docker-compose.yml   # 인프라 구성 (DB 등)
│   │
│   └── test                         # 단위 및 통합 테스트 코드
└── build.gradle                     # Gradle 의존성 및 프로젝트 빌드 설정
```