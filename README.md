# Billage

해커톤 프로젝트 **Billage** 백엔드 저장소입니다.

## 소개

Billage는 대학생을 위한 생활 공유 플랫폼입니다.

학교 인증을 기반으로 같은 학교 학생들끼리 생활용품과 공구를 안전하게 대여하고, 휴지·세제 등 생필품은 공동구매하여 생활비를 절약할 수 있도록 돕습니다.

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.0 |
| 빌드 도구 | Gradle (Wrapper 포함) |
| ORM | Spring Data JPA |
| DB | MySQL (운영) / H2 (로컬) |
| 인증 | Spring Security + JWT (jjwt 0.12.7) |
| API 문서 | Swagger (springdoc-openapi 3.0.3) |
| 기타 | Lombok, Bean Validation |

> ⚠️ Spring Boot **4.x** 입니다. 3.x와 의존성 이름이 다릅니다 (`spring-boot-starter-web` → `spring-boot-starter-webmvc`). 튜토리얼의 `build.gradle`을 그대로 복사하지 마세요.

## 프로젝트 구조

```
src/main/java/com/billage/
├── BillageApplication.java
└── global/
    ├── common/
    │   ├── response/ApiResponse.java      공통 응답 포맷
    │   ├── entity/BaseTimeEntity.java     created_at / updated_at 자동 관리
    │   └── controller/HealthController.java
    ├── config/
    │   ├── JpaAuditingConfig.java
    │   ├── SecurityConfig.java            인증 경로 규칙
    │   ├── CorsConfig.java
    │   └── SwaggerConfig.java
    ├── security/
    │   ├── AuthUser.java                  로그인한 유저 정보
    │   ├── jwt/JwtTokenProvider.java      토큰 발급·검증
    │   ├── jwt/JwtAuthenticationFilter.java
    │   └── handler/                       401 / 403 응답
    └── exception/
        ├── ErrorCode.java                 에러 코드 enum
        ├── BusinessException.java
        └── GlobalExceptionHandler.java
```

도메인 코드는 `com.billage.domain.*` 아래에 추가합니다.

## 공통 응답 포맷

모든 API는 아래 형식으로 응답합니다.

```json
// 성공
{ "success": true, "data": { }, "error": null }

// 실패
{ "success": false, "data": null, "error": { "code": "POST_NOT_FOUND", "message": "게시글을 찾을 수 없습니다." } }
```

서비스 로직에서 `throw new BusinessException(ErrorCode.POST_NOT_FOUND)` 를 던지면
`GlobalExceptionHandler`가 위 형식과 알맞은 HTTP 상태 코드로 변환합니다.

## 인증

로그인은 **카카오 OAuth2 단독**입니다. 자체 회원가입(이메일·비밀번호)은 없습니다.
카카오 인증을 마치면 서버가 자체 JWT를 발급하고, 이후 요청은 이 JWT로 인증합니다.

`/api/auth/**` 와 `/api/health` 를 제외한 모든 API는 인증이 필요합니다.
요청 헤더에 `Authorization: Bearer {accessToken}` 을 담아 보내면 됩니다.

**컨트롤러에서 로그인한 유저 꺼내기**

```java
@GetMapping("/posts/mine")
public ApiResponse<List<PostResponse>> myPosts(@AuthenticationPrincipal AuthUser authUser) {
    Long userId = authUser.getUserId();
    ...
}
```

**토큰 발급** (로그인 담당자용)

카카오 인증으로 사용자를 찾거나 새로 만든 뒤, 그 유저 id로 토큰을 발급합니다.

```java
String accessToken = jwtTokenProvider.createAccessToken(user.getId());
String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
```

**카카오 로그인 흐름**

```
프론트 → GET /oauth2/authorization/kakao
       → 카카오 인증 화면
       → GET /login/oauth2/code/kakao?code=...   (서버가 처리)
       → {OAUTH2_REDIRECT_URI}?accessToken=...&refreshToken=...&isNewUser=true   (성공)
       → {OAUTH2_REDIRECT_URI}?error=social_login_failed                          (실패)
```

`isNewUser=true` 면 이번에 새로 가입한 회원입니다. 다만 온보딩 화면으로 보낼지는
`GET /api/users/me` 의 `school` 이 `null` 인지로 판단하는 게 정확합니다
(가입만 하고 학교를 안 고른 채 이탈한 회원이 있을 수 있습니다).

카카오 개발자센터에서 발급받은 값을 환경변수로 넣어야 실제 로그인이 됩니다.

```bash
KAKAO_CLIENT_ID=...        # REST API 키
KAKAO_CLIENT_SECRET=...    # 보안 > Client Secret
OAUTH2_REDIRECT_URI=http://localhost:5173/oauth/callback
```

카카오 개발자센터에 **Redirect URI 를 `http://localhost:8080/login/oauth2/code/kakao` 로 등록**해야 하고,
동의항목(`profile_nickname`, `profile_image`, `account_email`)도 활성화해야 합니다.
`account_email` 은 선택 동의라 사용자가 거부하면 이메일이 넘어오지 않습니다.

인증 없이 열어야 할 경로가 생기면 `SecurityConfig.PUBLIC_ENDPOINTS` 에 추가하세요.

> `jwt.secret` 의 기본값은 **로컬 개발 전용**입니다. 배포 시 반드시 환경변수 `JWT_SECRET` 으로 덮어쓰세요.

## API 문서 (Swagger)

서버 실행 후 http://localhost:8080/swagger-ui.html

인증이 필요한 API를 테스트하려면 우측 상단 **Authorize** 버튼을 눌러 액세스 토큰을 입력하세요
(`Bearer ` 는 빼고 토큰 값만 붙여넣습니다).

## 주요 기능

정리되는 대로 추가 예정입니다.

## API 명세서

작성되는 대로 추가 예정입니다.

## 브랜치 전략

기능 단위로 브랜치를 나누는 feature-driven 방식으로 진행합니다.

- `main` — 배포 가능한 안정 브랜치
- `feature/*` — 기능 단위 작업 브랜치 (예: `feature/login`, `feature/rental`)

기능마다 `main`에서 `feature/*` 브랜치를 따서 작업하고, 완료되면 `main`으로 PR을 올려 리뷰 후 머지합니다.

## 커밋 컨벤션

```
feat: 새로운 기능 추가
fix: 버그 수정
refactor: 코드 리팩토링
docs: 문서 수정
chore: 빌드, 설정 등 기타 변경
```

## 시작하기

JDK 21만 설치되어 있으면 됩니다. Gradle과 DB는 따로 설치하지 않아도 됩니다.

```bash
git clone https://github.com/RYU-TOMI/Billage.git
cd Billage
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

기본 프로필은 `local`이며 H2 인메모리 DB로 실행됩니다.

- 서버: http://localhost:8080
- 헬스체크: http://localhost:8080/api/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 콘솔: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:billage;MODE=MySQL;DB_CLOSE_DELAY=-1`, User `sa`, 비밀번호 없음)

### MySQL로 실행하기

```bash
DB_URL=jdbc:mysql://localhost:3306/billage \
DB_USERNAME=root \
DB_PASSWORD=password \
./gradlew bootRun --args='--spring.profiles.active=dev'
```

> DB 비밀번호, JWT 시크릿 등은 **절대 커밋하지 마세요.** 공개 저장소입니다.
> 환경변수로 주입하거나 `application-secret.yml`(gitignore 처리됨)에 두세요.
