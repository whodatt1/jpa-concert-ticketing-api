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
.\gradlew clean build
.\gradlew bootRun

# Mac / Linux (Terminal)
./gradlew clean build
./gradlew bootRun
```

서버 구동 시 `DataLoader`가 작동하여, API 테스트를 위한 **필수 마스터 데이터(공연장 3곳, 카테고리 4개)와 30건의 더미 콘서트**를 H2 DB에 자동 세팅합니다.

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **H2 Database:** http://localhost:8080/h2-console

## 4. 핵심 기술 및 트러블슈팅

### [1] 좌석 생성 병목 해결 (Bulk Insert)

- **문제:** 신규 콘서트 생성 시, 회차별로 좌석(Seat) 데이터를 JPA `saveAll()`로 생성할 때 심각한 성능 저하 및 지연 발생.
- **해결:** JPA의 영속성 컨텍스트를 우회하여, Spring JDBC의 `jdbcTemplate.batchUpdate()`활용한 벌크 인서트(Bulk Insert)로 전환. OOM을 방지하고 생성 소요 시간을 단축.

### [2] 동시성 제어 아키텍처 구축 (개편)

- **문제:** 기존 `StringRedisTemplate(Lettuce)` 기반 락 사용 시, 락 소유권 확인(GET) 후 해제(DEL)하는 찰나의 지연 발생 시 타인의 락을 오삭제할 수 있는 크리티컬 버그를 테스트 코드로 발견.
- **해결:** 
	- **Redisson 도입 (원자성 보장):** 락 소유권 검증과 삭제 로직이 분리되어 있던 기존 방식의 결함을 해결하기 위해, 내부적으로 Lua 스크립트를 사용하여 두 과정을 원자적(Atomic)으로 한 번에 처리하는 Redisson으로 마이그레이션. 이를 통해 타인의 락 오삭제 문제를 완벽하게 차단.
	- **JPA 낙관적 락 적용 (최후의 방어선):** Redis 락 타임아웃 등 예기치 못한 네트워크 지연으로 발생할 수 있는 트랜잭션 엇박자마저 물리적으로 차단하기 위해, Seat 엔티티에 JPA 낙관적 락(`@Version`)을 적용. DB 레벨에서 중복 예매를 원천 차단하고 커스텀 예외(`ObjectOptimisticLockingFailureException`)를 통해 안전하게 처리.

### [3] 인프라 의존성을 제거한 예매 만료 스케줄러 재설계 (개편)

- **문제:** 기존 `Lettuce` 환경에서는 Redis 락의 TTL(5분)을 결제 대기 시간으로 그대로 사용했으나, `Redisson` 도입으로 락을 단순 '동시성 방어용'으로 역할을 분리하면서 더 이상 Redis 인프라를 통해 비즈니스 타임아웃을 추적할 수 없게 됨.
- **해결:** 결제 대기 시간 관리의 책임을 Redis(인프라)에서 DB(도메인) 영역으로 완전 이관. 스케줄러의 Redis 의존성을 제거하고, `Reservation` 엔티티에 생성 시간(`createdAt`)을 추가하여 1분마다 DB를 조회, '생성된 지 5분이 지난 PENDING 상태의 예매'만 일괄 취소(`CANCEL`) 및 좌석 원복(`AVAILABLE`)하도록 전면 재설계.

### [4] Facade 계층 분리로 인한 DB 커넥션 효율화

- **문제:** 기존 `@Transactional` 내부에서 분산 락을 획득할 경우, Redis 네트워크 통신 대기 시간 동안 DB 커넥션을 점유하게 됨.
- **해결:** `SeatFacade`를 도입하여 락 획득 성공자만 실제 DB 트랜잭션(SeatService)에 진입하도록 설계.

### [5] Querydsl과 인덱스 튜닝을 통한 조회 성능 극대화

- **N+1 및 데이터 뻥튀기 방지:** 콘서트 디테일 조회 시 `MultipleBagFetchException`을 차단하기 위해 스케줄(Schedule)은 `fetchJoin`으로 가격(Price)은 `default_batch_fetch_size`를 활용한 지연 로딩으로 최적화.
- **서브쿼리 튜닝:** 카테고리 필터링 조회 시 N:M 테이블 JOIN으로 인한 데이터 중복을 방지하고자 `EXISTS`서브쿼리로 리팩토링.
- **인덱스 튜닝:** 
	- **콘서트 목록:** 정렬 최적화를 위해 `(del_yn, created_at)`복합 인덱스 적용
	- **좌석 맵:** 잦은 업데이트가 발생하는 상태(`status`) 컬럼을 배제하고 단일 컬럼(`conert_schedule_id`)으로 인덱스를 재설계하여 최적화

### [6] 안정적인 API 환경 및 Swagger 고도화

- **Global Exception:** `@RestControllerAdvice`를 활용한 전역 예외 처리 및 공통 응답 규격(ApiResponse/ApiErrorResponse)을 적용하여 혼란을 최소화.
- **Swagger 문서화:** `@ParameterObject`를 통해 `record`객체 및 페이징 파라미터를 명시적으로 노출, 마스터 데이터(카테고리, 공연장 ID)의 Example 값을 주입하여 API 테스트 편의성을 극대화.

## 5. 테스트 성능 및 검증

### 5. 테스트 성능 및 검증

#### [1] 분산 락 및 낙관적 락 통합 동시성 테스트
- **목적:** 100명의 사용자가 동시에 동일한 좌석에 접근하는 상황을 시뮬레이션하여, 시스템의 데이터 정합성과 보호 메커니즘을 검증.
- **구현 방식:** 
	- `@SpringBootTest` 환경에서 `ExecutorService`와 `CountDownLatch`를 활용하여 100개의 스레드가 동시에 `reserveSeat()`를 호출하도록 구성.
    - `AtomicInteger`를 사용하여 멀티 스레드 환경에서 안전하게 성공/실패 횟수를 카운트.
- **검증 결과:** - 100건의 요청 중 **단 1건만 최종 예매 성공**을 확인.
    - 대부분의 요청은 `Redisson` 분산 락 획득 단계에서 차단(`SEAT_ALREADY_LOCKED`)되어 DB 인입 트래픽을 최소화함.
    - 분산 락 해제 시점과 트랜잭션 커밋 시점의 미세한 차이로 발생하는 중복 진입건은 엔티티의 `JPA 낙관적 락(@Version)`에 의해 최종적으로 거절됨을 확인.

#### [2] Lettuce 기반 비원자적 락 해제 결함 증명 테스트
- **목적:** 기존 `StringRedisTemplate(Lettuce)` 환경에서 '소유권 확인(GET) 후 삭제(DEL)' 과정이 원자적으로 처리되지 않아 발생하는 '타인 락 오삭제' 버그를 코드로 재현.
- **구현 방식:** 
	- `@MockitoSpyBean`과 `doAnswer`를 활용하여 유저 1이 본인의 락 소유권을 확인한 직후 로직을 강제로 일시 정지(Wait) 시킴.
    - 그 사이 유저 1의 락 만료 및 유저 2의 신규 락 획득 상황을 연출한 뒤 유저 1의 로직을 재개.
- **검증 결과:** 
	- 로직이 재개된 유저 1이 자신의 락이 만료되었음을 인지하지 못하고 **유저 2가 보유한 신규 락을 삭제**하는 현상을 성공적으로 재현.
    - 이를 통해 Lua 스크립트를 사용하여 확인과 삭제를 원자적으로 처리하는 `Redisson` 도입의 기술적 당위성을 증명.

#### [3] JPA MultipleBagFetchException 결함 재현 테스트
- **목적:** JPA에서 두 개 이상의 자식 컬렉션(List)을 동시에 `fetchJoin`할 때 발생하는 기술적 제약 사항을 코드로 재현하고, 최적화 전략의 유효성을 검증.
- **구현 방식:** - Querydsl을 사용하여 콘서트 정보 조회 시, `Schedules`와 `Prices` 두 컬렉션 모두에 의도적으로 `.fetchJoin()`을 적용하여 쿼리 실행.
- **검증 결과:** 
	- 하이버네이트가 2개 이상의 Bag을 동시에 가져올 수 없다는 `MultipleBagFetchException`을 정확히 발생시키는 것을 확인.

## 6. 프로젝트 구조

```text
.
├── src
│   ├── main
│   │   ├── java/com/tiketing/api
│   │   │   ├── concert            # 공연 정보, 공연장, 회차별 일정 관리
│   │   │   ├── reservation        # 좌석 선점 로직 및 스케줄러 관리
│   │   │   ├── payment            # 가상 결제 프로세스 처리
│   │   │   └── global             # 전역 예외 처리(Exception), 공통 응답(Response), 설정(Config)
│   │   │
│   │   └── resources              # application.yaml, docker-compose 등 설정 파일
│   │
│   └── test/java/com/tiketing/api
│       └── service                # 핵심 비즈니스 로직 동시성(Concurrency) 테스트
│
└── build.gradle                   # 의존성 및 프로젝트 빌드 구성
```
