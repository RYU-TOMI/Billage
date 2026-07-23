# Billage

대학생을 위한 생활 공유 플랫폼 **Billage** 의 백엔드 저장소입니다.

학교를 기반으로 같은 학교 학생들끼리 생활용품과 공구를 대여하고, 휴지·세제 등 생필품은 공동구매하여 생활비를 절약할 수 있도록 돕습니다.

## 배포 주소

| | 주소 |
| --- | --- |
| API | https://api.billage.site |
| API 문서 (Swagger) | https://api.billage.site/swagger-ui.html |
| 헬스체크 | https://api.billage.site/api/health |

AWS EC2 + Docker Compose (Caddy · Spring Boot · MySQL) 구성이며 HTTPS 는 Let's Encrypt 인증서를 사용합니다.

---

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.0 |
| 빌드 | Gradle (Wrapper 포함) |
| ORM | Spring Data JPA |
| DB | MySQL 8.4 (운영) / H2 (로컬) |
| 인증 | Spring Security + OAuth2 Client(카카오) + JWT (jjwt 0.12.7) |
| API 문서 | springdoc-openapi 3.0.3 |
| 인프라 | AWS EC2, Docker Compose, Caddy |

> ⚠️ **Spring Boot 4.x 입니다.** 3.x 와 달라진 점이 있어 튜토리얼 코드를 그대로 복사하면 깨집니다.
> - 의존성 이름: `spring-boot-starter-web` → **`spring-boot-starter-webmvc`**
> - Jackson 3 사용: `com.fasterxml.jackson.databind.ObjectMapper` → **`tools.jackson.databind.ObjectMapper`**
>   (애노테이션은 `com.fasterxml.jackson.annotation` 그대로)
> - 테스트: `@AutoConfigureMockMvc` 패키지가 **`org.springframework.boot.webmvc.test.autoconfigure`** 로 이동

---

## 도메인 모델

대여와 공동구매를 **하나의 게시글(`Post`) 도메인으로 통합**하고 `type` 으로 구분합니다.

| 엔티티 | 설명 |
| --- | --- |
| `User` | 회원. 카카오 `social_id` 로 식별. `credit` 잔액과 `school` 보유 |
| `Post` | 게시글. `type`(RENTAL/GROUP_BUY), `category`(TOOL/LIVING), `status`(OPEN/CLOSED) |
| `Application` | 참여. 승인 단계 없이 **신청 즉시 CONFIRMED** |
| `CreditHistory` | 크레딧 증감 내역. `amount` 부호로 구분, `reason`(CHARGE/PAYMENT/REFUND) |

```
User ─1:N─ Post ─1:N─ Application ─N:1─ User
User ─1:N─ CreditHistory ─N:1(nullable)─ Post
```

- `capacity` 는 RENTAL 이면 서버가 항상 `1` 로 강제합니다. 두 타입이 같은 정원 체크 로직을 씁니다.
- `deadline`(GROUP_BUY 전용), `rental_period`(RENTAL 전용) 는 DB 에서 NULL 허용이고 **서비스 계층에서 검증**합니다.
- 모든 엔티티는 `BaseTimeEntity` 를 상속해 `created_at` / `updated_at` 이 자동 관리됩니다.
- 엔티티 생성은 빌더 대신 **정적 팩토리 메서드** `Xxx.create(...)` 를 사용합니다.

---

## 공통 규약

**Base URL** `/api` · **인증** `Authorization: Bearer {accessToken}`

### 응답 포맷

```json
// 성공
{ "success": true, "data": { }, "error": null }

// 실패
{ "success": false, "data": null, "error": { "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." } }
```

서비스에서 `throw new BusinessException(ErrorCode.POST_NOT_FOUND)` 를 던지면
`GlobalExceptionHandler` 가 위 형식과 알맞은 HTTP 상태 코드로 변환합니다.

에러 코드는 `global/exception/ErrorCode.java` 에 도메인별로 모여 있습니다.
**공통 파일이라 여러 명이 동시에 고치면 충돌합니다. 자기 도메인 블록 안에서만 추가하세요.**

### 페이징

목록 조회는 공통으로 `page`(0-base, 기본 0), `size`(기본 20) 쿼리 파라미터를 받습니다.

---

## API

### 구현 완료

| Method | URI | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/health` | 헬스체크 | – |
| GET | `/oauth2/authorization/kakao` | 카카오 로그인 시작 | – |
| GET | `/login/oauth2/code/kakao` | 카카오 콜백 (서버 처리) | – |
| POST | `/api/auth/reissue` | 액세스 토큰 재발급 | – |
| GET | `/api/users/me` | 내 정보 조회 | ✅ |
| PATCH | `/api/users/me/school` | 학교 선택 (온보딩) | ✅ |
| GET | `/api/schools` | 학교 목록 검색 (`?keyword=`) | ✅ |
| GET | `/api/users/me/credit` | 크레딧 잔액 조회 | ✅ |
| POST | `/api/users/me/credit/charge` | 크레딧 충전 (모의) | ✅ |
| GET | `/api/users/me/credit/history` | 크레딧 내역 조회 | ✅ |
| PATCH | `/api/applications/{id}/cancel` | 참여 취소 (크레딧 환불) | ✅ |

### 작업 예정

| Method | URI | 설명 |
| --- | --- | --- |
| POST | `/api/posts/{postId}/applications` | 참여하기 (정원 체크 · 크레딧 차감) |
| POST | `/api/posts` | 게시글 등록 |
| GET | `/api/posts` | 목록 조회 (`?type=&category=&page=&size=`) |
| GET | `/api/posts/{postId}` | 상세 조회 |
| GET | `/api/posts/search` | 검색 (`?keyword=`) |
| POST | `/api/images` | 이미지 업로드 |
| GET | `/api/applications/me` | 내 참여 목록 |

---

## 인증

로그인은 **카카오 OAuth2 단독**입니다. 자체 회원가입(이메일·비밀번호)은 없습니다.
`/api/auth/**`, `/api/health`, `/oauth2/**`, `/login/oauth2/**`, Swagger 를 제외한 모든 API 는 인증이 필요합니다.

### 로그인 흐름

```
프론트 → GET /oauth2/authorization/kakao
       → 카카오 인증
       → GET /login/oauth2/code/kakao?code=...        (서버가 처리)
         · social_id 로 회원 조회, 없으면 생성 (school = null)
         · 자체 JWT 발급
       → {OAUTH2_REDIRECT_URI}?accessToken=...&refreshToken=...&isNewUser=true   (성공)
       → {OAUTH2_REDIRECT_URI}?error=social_login_failed                          (실패)
```

**온보딩 분기는 `GET /api/users/me` 의 `school` 이 `null` 인지로 판단하세요.**
리다이렉트의 `isNewUser` 보다 정확합니다 — 가입만 하고 학교를 안 고른 채 이탈한 회원은
`isNewUser=false` 인데도 온보딩이 필요합니다.

### 요청마다 일어나는 일

`JwtAuthenticationFilter` 가 `Authorization` 헤더의 토큰을 검증하고
`SecurityContext` 에 인증 객체를 세팅합니다. **principal 은 `AuthUser` 레코드**이며 `userId` 만 담습니다.
DB 조회는 하지 않습니다.

```java
@GetMapping("/posts/mine")
public ApiResponse<List<PostResponse>> myPosts(@AuthenticationPrincipal AuthUser authUser) {
    Long userId = authUser.getUserId();
    ...
}
```

User 엔티티 전체가 필요하면 서비스에서 `userRepository.findById(userId)` 로 조회하세요.

- **액세스 토큰만** 인증에 사용됩니다. 리프레시 토큰을 헤더에 넣으면 401 입니다.
- 토큰 발급은 `jwtTokenProvider.createAccessToken(userId)` / `createRefreshToken(userId)`
- 인증 없이 열어야 할 경로는 `SecurityConfig.PUBLIC_ENDPOINTS` 에 추가하세요.

### 카카오 개발자센터 설정

```bash
KAKAO_CLIENT_ID=...        # REST API 키
KAKAO_CLIENT_SECRET=...    # 보안 > Client Secret
```

- **Redirect URI 등록** (필수) — *제품 설정 > 카카오 로그인*
  - 로컬 `http://localhost:8080/login/oauth2/code/kakao`
  - 운영 `https://api.billage.site/login/oauth2/code/kakao`
- **동의항목** — `profile_nickname`, `profile_image`, `account_email`
  - 개발자센터에서 **켜두지 않은 항목을 요청하면 로그인 시 `KOE205`** 로 실패합니다
  - `account_email` 은 선택 동의라 사용자가 거부하면 `User.email` 이 `null` 로 저장됩니다
  - 요청 항목 변경: `KAKAO_SCOPE=profile_nickname,profile_image`
- **웹 도메인 등록** (선택) — *앱 설정 > 앱 > 제품 링크 관리 > 웹 도메인*
  - 서버 리다이렉트 방식이라 로그인에는 필요 없습니다
  - 프론트가 카카오 JS SDK(공유하기·지도 등)를 쓸 때 필요합니다

---

## 시작하기

JDK 21 만 있으면 됩니다. Gradle 과 DB 는 따로 설치하지 않아도 됩니다.

```bash
git clone https://github.com/RYU-TOMI/Billage.git
cd Billage
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

기본 프로필은 `local` 이며 H2 인메모리 DB 로 실행됩니다.

- 서버 http://localhost:8080
- Swagger http://localhost:8080/swagger-ui.html
- H2 콘솔 http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:billage;MODE=MySQL;DB_CLOSE_DELAY=-1`, User `sa`, 비밀번호 없음)

### 카카오 로그인까지 켜서 실행

`.env` 에 키를 넣어뒀다면 **Spring Boot 는 `.env` 를 자동으로 읽지 않으므로** 셸에서 주입해야 합니다.

```bash
set -a && . ./.env && set +a && ./gradlew bootRun
```

IntelliJ 는 Run Configuration 의 Environment variables 에 넣으면 됩니다.

### MySQL 로 실행

```bash
DB_URL=jdbc:mysql://localhost:3306/billage \
DB_USERNAME=root DB_PASSWORD=password \
./gradlew bootRun --args='--spring.profiles.active=dev'
```

> 🔒 DB 비밀번호·JWT 시크릿·SSH 키는 **절대 커밋하지 마세요.** 공개 저장소입니다.
> `.env`, `.env.prod`, `*.pem`, `*.key` 는 `.gitignore` 에 등록돼 있습니다.

---

## 프로젝트 구조

```
src/main/java/com/billage/
├── domain/
│   ├── user/          회원, 학교 온보딩
│   ├── auth/          토큰 재발급
│   ├── post/          게시글 (대여 · 공동구매 공통)
│   ├── application/   참여
│   └── credit/        크레딧 잔액 · 내역
└── global/
    ├── common/
    │   ├── response/ApiResponse.java      공통 응답 포맷
    │   └── entity/BaseTimeEntity.java     created_at / updated_at
    ├── config/                            Security · CORS · Swagger · JPA Auditing
    ├── security/
    │   ├── AuthUser.java                  로그인 유저 principal
    │   ├── jwt/                           토큰 발급 · 검증 · 필터
    │   └── oauth2/                        카카오 로그인 처리
    └── exception/                         ErrorCode · 전역 예외 처리
```

도메인은 `controller` / `service` / `repository` / `entity` / `dto` 로 나눕니다.

---

## 배포

```bash
./deploy/deploy.sh
```

로컬에서 jar 를 빌드해 EC2 로 전송하고 컨테이너를 재시작한 뒤 헬스체크까지 수행합니다.
저장소 루트에 `billage-key.pem` 이 필요합니다.

> 서버(t3.small)에서 Gradle 빌드를 돌리면 메모리 부족으로 죽기 때문에 **빌드는 항상 로컬에서** 합니다.

### 운영 환경변수

`/opt/billage/.env.prod` 에 있습니다. (권한 600, 저장소에 없음)

| 변수 | 설명 |
| --- | --- |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | MySQL 접속 정보 |
| `JWT_SECRET` | 32바이트 이상. `openssl rand -base64 32` |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` | 카카오 발급값 |
| `OAUTH2_REDIRECT_URI` | 로그인 후 돌아갈 프론트 주소 |
| `CORS_ALLOWED_ORIGINS` | 허용 출처. 와일드카드(`*`) 사용 가능 |

> **`server.forward-headers-strategy: framework`** (`application-prod.yml`) 는 지우면 안 됩니다.
> Caddy 가 HTTPS 를 종료하기 때문에, 이게 없으면 Spring 이 자기 주소를 `http` 로 인식해
> 카카오에 보내는 `redirect_uri` 가 어긋나고 **KOE006** 으로 로그인이 실패합니다.

---

## 협업 규칙

### 브랜치 전략

기능 단위로 브랜치를 나누는 feature-driven 방식입니다.

- `main` — 배포 가능한 안정 브랜치
- `feature/*` — 기능 단위 작업 브랜치 (예: `feature/post-crud`)

`main` 에서 브랜치를 따서 작업하고, 완료되면 `main` 으로 PR 을 올려 리뷰 후 머지합니다.

### 커밋 컨벤션

```
feat:     새로운 기능 추가
fix:      버그 수정
refactor: 코드 리팩토링
docs:     문서 수정
chore:    빌드, 설정 등 기타 변경
test:     테스트 추가·수정
```

### 여러 명이 같이 건드리는 파일

충돌이 잦으니 수정 전에 공유해 주세요.

- `global/exception/ErrorCode.java`
- `global/config/SecurityConfig.java` (공개 경로 추가)
- `domain/user/entity/User.java`
