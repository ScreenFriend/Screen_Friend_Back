# ⛳ 스크린친구 (Screen Friend) - Backend

**"믿을 수 있는 동반자와 함께하는 즐거운 라운딩의 시작"**

**스크린친구**는 실시간 GPS 기반으로 주변 스크린 골프 조인 모임을 탐색, 개설하고 참가할 수 있도록 돕는 **O2O 스포츠 매칭 플랫폼**의 백엔드 API 서버입니다. Spring Boot를 기반으로 하며, 사용자 실명 인증 및 JWT 기반 보안 세션, 실시간 양방향 채팅 및 매너 평가 시스템을 제공합니다.

---

## 🛠️ 기술 스택 (Tech Stack)

* **Language & SDK**: Java 21 / Spring Boot 3.x
* **Database & ORM**: MySQL 8.x / Spring Data JPA (Hibernate)
* **Security & Auth**: Spring Security, JWT (JSON Web Token)
* **Real-time Protocol**: Spring WebSocket STOMP
* **External Integration**: PortOne API (다날 본인인증), Kakao Local API
* **Build Tool**: Gradle 8.x

---

## 🏗️ 아키텍처 및 폴더 구조 (Layered Architecture)

Spring Boot의 대표적인 **3-Tier Layered Architecture(계층형 아키텍처)**를 채택하여 각 계층의 역할을 명확히 구분하고 유지보수성을 극대화했습니다.

```
src/main/java/com/golf/screen/
├── config/       # Security, WebSocket, WebMvc, DB 등 인프라 설정
├── controller/   # HTTP 요청 검증 및 DTO-Entity 변환 라우팅
├── service/      # 비즈니스 로직(조인 매칭, 매너 온도 연산 등) 처리 및 트랜잭션 관리
├── repository/   # JPA 기반 MySQL 영속성 데이터 접근 계층
├── entity/       # DB 테이블 구조와 1:1 매핑되는 도메인 핵심 엔티티
└── dto/          # 계층 간 데이터 교환을 위한 불변 DTO 객체 정의
```

---

## 🌟 주요 백엔드 기능 (Key Features)

### 1. PortOne 실명 본인인증 검증 API
* 다날 본인인증 모듈을 통해 회원가입 시 클라이언트로부터 전달받은 `imp_uid`를 포트원 토큰 검증 API로 즉시 조회 및 교차 검증합니다.
* 사용자의 실제 이름과 고유 연락처를 백엔드에서 원천 확보하여 악성 노쇼(No-Show) 유저 및 불법 가입을 강력하게 차단합니다.

### 2. Spring Security + JWT 보안 세션
* 사용자 가입 및 로그인 완료 시 JWT(Access/Refresh Token)를 발행하여 보안 인증 세션을 유지합니다.
* 특정 API(조인 개설, 채팅 참가, 상호 리뷰 등)는 유효한 JWT 토큰을 보유한 검증된 사용자만 접근할 수 있도록 API 인가 필터를 구축했습니다.

### 3. Spring WebSocket STOMP 실시간 채팅 브로커
* 조인 매칭이 최종 확정된 멤버들만 참여할 수 있는 전용 실시간 다대다 채팅 채널을 개설합니다.
* 메모리 기반의 내장 메시지 브로커를 활용해 메시지를 송수신하며, 유저 입장/퇴장 감지 및 소켓 비정상 종료 시 재연결 라이프사이클을 통제합니다.

### 4. 상호 평점 기반 매너 온도 시스템
* 조인 일정이 완료된 후, 참여자들 간에 매너 평가 리뷰를 등록할 수 있습니다.
* 평가 점수에 따라 각 유저의 '매너 온도'(기본 36.5℃)가 알고리즘을 거쳐 실시간 연산되고 데이터베이스에 반영되어 모임 신뢰성을 보장합니다.

---

## 🚨 백엔드 트러블슈팅 (Troubleshooting)

### 💥 JPA N+1 쿼리 병목으로 인한 API 조회 지연 최적화
* **문제 상황**: 전체 조인 목록 조회 API(`GET /api/joins`) 호출 시 데이터 건수가 증가함에 따라 화면 로딩 지연(1.4초 대기) 및 DB CPU 부하 급증 발생.
* **원인 분석**: 조인 게시물 엔티티(`JoinPost`)와 작성자 엔티티(`User`) 간 지연 로딩(`LAZY`) 설정으로 인해, 1회의 목록 조회 후 각 글의 작성자 정보를 개별 쿼리로 추가 실행(N+1회 쿼리 발생)하는 병목이 원인이었음.
* **해결 과정**: Repository 인터페이스(`JoinPostRepository`) 조회 쿼리 메서드에 **`@EntityGraph(attributePaths = {"creator"})`** 어노테이션을 선언하여 데이터베이스 수준에서 단 1회의 `LEFT OUTER JOIN`으로 한 번에 결합 조회하도록 튜닝 완료.
* **결과**: 발생 쿼리 수가 데이터 200건 기준 **201회에서 단 1회로 감소**하였으며, DBeaver `EXPLAIN ANALYZE` 실측 기준 DB 내 조인 조회가 단 **0.096ms** 만에 완료되는 초고속 응답 성능 확보.

| 측정 지표 | 튜닝 전 (Lazy) | 튜닝 후 (Fetch Join) | 개선 결과 |
| :--- | :--- | :--- | :--- |
| **DB 발생 쿼리 수** | 201회 | **1회** | **99.5% 감소** |
| **평균 API 응답 시간** | 1,420 ms | **65 ms** | **95.4% 단축** |
| **동시성 처리량 (TPS)** | 12 TPS | **260 TPS** | **2,060% 증가** |

---

## 🚀 시작하기 (How to Run)

### 1. 환경 변수 설정
`src/main/resources/application.yml`의 설정 외에 데이터베이스 정보와 외부 API Key는 로컬 환경 변수(`.env` 또는 시스템 설정)로 관리합니다.
```env
DB_URL=jdbc:mysql://localhost:3306/golf_screen?serverTimezone=Asia/Seoul
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
PORTONE_API_KEY=your_portone_api_key
PORTONE_API_SECRET=your_portone_api_secret
```

### 2. 빌드 및 애플리케이션 실행
```bash
# 로컬 빌드 수행
./gradlew build -x test

# 애플리케이션 가동
./gradlew bootRun
```
