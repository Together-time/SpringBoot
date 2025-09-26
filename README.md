# 같이가요 (Together Time) - Backend

> 팀 단위 일정 관리 및 실시간 소통을 지원하는 플랫폼의 백엔드 서버

## 📋 프로젝트 개요

같이가요는 팀 기반 일정 관리 및 실시간 소통 플랫폼입니다. 팀원들과 함께 일정을 관리하고 실시간 채팅을 통해 소통할 수 있습니다. 또한 다른 팀의 공개 일정을 검색하여 참고할 수 있는 서비스입니다.

## ✨ 주요 기능

### 🔐 인증 및 사용자 관리
- **카카오 소셜 로그인**: OAuth2 기반 간편 로그인
- **JWT 토큰 관리**: RTR(Refresh Token Rotation) 방식 적용
- **토큰 블랙리스트**: 로그아웃 시 토큰 무효화 처리
- **서비스 탈퇴**: 완전한 계정 삭제 기능

### 👥 팀 관리
- **팀 생성 및 관리**: 팀 설정 및 권한 관리
- **팀원 초대**: 멤버 검색을 통한 팀원 초대
- **팀 탈퇴**: 개별 팀 탈퇴 기능

### 🗓️ 일정 관리 (Project)
- **일정 CRUD**: 일정 생성, 조회, 수정, 삭제
- **공개/비공개 설정**: 팀 외부 공개 여부 설정
- **일정 검색**: 태그 기반 공개 일정 검색
- **조회수 관리**: 스케줄러를 통한 조회수 동기화
- **검색 결과 정렬**: 조회순으로 정렬 지원

### 💬 실시간 채팅
- **팀 내 채팅**: 팀원 간 실시간 메시지 교환
- **읽음 표시**: 메시지별 읽음/미읽음 상태 관리
- **실시간 접속 상태**: 팀원의 온라인/오프라인 상태 표시
- **메시지 히스토리**: 채팅 기록 저장 및 조회

## 🛠️ 기술 스택

### Backend Framework
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 17
- **Build Tool**: Gradle
- **Security**: Spring Security + OAuth2

### Database
- **RDB**: MySQL (사용자, 팀 정보)
- **NoSQL**: MongoDB (일정, 채팅 데이터)
- **Cache**: Redis (토큰 블랙리스트, Pub/Sub)

### Real-time Communication
- **WebSocket**: Spring WebSocket (실시간 채팅)
- **Message Broker**: Redis Pub/Sub

### Authentication
- **OAuth2**: 카카오 소셜 로그인
- **JWT**: Access Token + Refresh Token (RTR 방식)
- **Token Management**: 블랙리스트 기반 토큰 무효화

### 추가 의존성
- **JWT**: JJWT 0.11.5 (API, Implementation, Jackson)
- **JSON Processing**: Jackson JSR310 (날짜/시간 처리)
- **Database Connectors**: MySQL Connector 8.0.33
- **Development**: Lombok, Spring Boot DevTools

## 🏗️ 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── com/tt/Together_time/
│   │       ├── config/              # 설정 파일들
│   │       │   ├── SecurityConfig.java
│   │       │   ├── WebSocketConfig.java
│   │       │   ├── MongoConfig.java
│   │       │   ├── JacksonConfig.java
│   │       │   ├── RedisConfig.java
│   │       │   ├── WebConfig.java
│   │       │   └── WebsocketHandshakeInterceptor.java
│   │       ├── controller/          # REST API 컨트롤러
│   │       │   ├── AuthController.java
│   │       │   ├── ChatController.java
│   │       │   ├── MemberController.java
│   │       │   ├── ProjectController.java
│   │       │   ├── ScheduleController.java
│   │       │   └── TeamController.java
│   │       ├── service/             # 비즈니스 로직
│   │       │   ├── ChatService.java
│   │       │   ├── CustomOAuth2UserService.java
│   │       │   ├── MemberService.java
│   │       │   ├── OnlineStatusService.java
│   │       │   ├── ProjectDtoService.java
│   │       │   ├── ProjectService.java
│   │       │   ├── RedisMessageSubscriber.java
│   │       │   ├── ScheduleService.java
│   │       │   └── TeamService.java
│   │       ├── Handler/          # 핸들러 관련
│   │       │   └── CustomOAuth2SuccessHandler.java
│   │       ├── repository/          # 데이터 접근 계층
│   │       │   ├── AuthRepository.java
│   │       │   ├── ChatMongoRepository.java
│   │       │   ├── MemberRepository.java
│   │       │   ├── ProjectMongoRepository.java
│   │       │   ├── ProjectRepository.java
│   │       │   ├── RedisDao.java
│   │       │   ├── ScheduleRepository.java
│   │       │   └── TeamRepository.java
│   │       ├── scheduler/          # 스케줄링 관련
│   │       │   └── ViewSyncScheduler.java
│   │       ├── security/             # 보안 관련
│   │       │   ├── JwtAuthenticationFilter.java
│   │       │   └── JwtTokenProvider.java
│   │       ├── websocket/           # WebSocket 핸들러
│   │       │   ├── ChatWebSocketHandler.java
│   │       │   └── OnlineStatusWebSocketHandler.java
│   │       ├── exception/           # 예외 처리
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── ExceptionHandlerFilter.java
│   │       │   ├── BlacklistedTokenException.java
│   │       │   └── InvalidRefreshTokenException.java
│   │       └── domain/              # 데이터 전송 객체 및 엔티티들
│   │           ├── dto/             # 데이터 전송 객체
│   │           │   ├── ChatDto.java
│   │           │   ├── MemberDto.java
│   │           │   ├── ProjectDto.java
│   │           │   ├── ScheduleRequest.java
│   │           │   ├── Sender.java
│   │           │   └── TeamCommand.java
│   │           ├── enums/           # JPA 엔티티 (RDB)
│   │           │   ├── ProjectSortType.java
│   │           │   └── ProjectVisibility.java
│   │           ├── rdb/             # JPA 엔티티 (RDB)
│   │           │   ├── Member.java
│   │           │   ├── Project.java
│   │           │   ├── Schedule.java
│   │           │   └── Team.java
│   │           └── mongodb/         # MongoDB 도큐먼트
│   │               ├── ProjectDocument.java
│   │               └── ChatDocument.java
│   └── resources/
│       └── application.yml
└── test/
```

## 🔧 핵심 기술 구현

### JWT 토큰 관리 (RTR 방식)
- Access Token과 Refresh Token 발급
- Refresh Token Rotation으로 보안 강화
- Redis 기반 토큰 블랙리스트 관리

### 실시간 채팅 시스템
- WebSocket을 통한 실시간 통신
- Redis Pub/Sub를 활용한 메시지 브로드캐스팅
- 읽음 표시 및 접속 상태 실시간 동기화

### 하이브리드 데이터베이스 구조
- **RDB**: 사용자, 팀 정보 (정형 데이터)
- **MongoDB**: 일정, 채팅 데이터 (비정형 데이터)

### 스케줄링
- Spring Scheduler를 활용한 조회수 동기화
- 주기적인 데이터 정합성 관리

## 🧪 주요 예외 처리

### 커스텀 예외
- `BlacklistedTokenException`: 블랙리스트된 토큰 사용 시
- `InvalidRefreshTokenException`: 유효하지 않은 Refresh Token

### 글로벌 예외 처리
- `GlobalExceptionHandler`: 전역 예외 처리
- `ExceptionHandlerFilter`: JWT 필터 단계 예외 처리
