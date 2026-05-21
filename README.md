# Spring Boot Starter Kit

Spring Boot 프로젝트를 빠르게 시작하기 위한 재사용 가능한 템플릿입니다.  
`init.ps1` (Windows) 또는 `init.sh` (Linux/macOS) 를 실행하면 패키지명·DB를 선택한 새 프로젝트가 자동으로 생성됩니다.

---

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 21 (Amazon Corretto 21) |
| Spring Boot | 3.5.x |
| 빌드 도구 | Gradle 8.x |
| DB (선택) | MySQL 8.x / PostgreSQL 14+ |
| 로컬 개발 DB | H2 인메모리 (별도 설치 불필요) |

---

## 사전 요구사항

- **Java 21** — [Amazon Corretto 21 다운로드](https://aws.amazon.com/corretto/)
- **Gradle 8.x** — Wrapper 자동 생성에 필요 (IntelliJ IDEA 사용 시 불필요)

---

## 새 프로젝트 생성

### Windows
```powershell
.\init.ps1
```

### Linux / macOS
```bash
chmod +x init.sh
./init.sh
```

스크립트 실행 시 아래 항목을 입력합니다.

| 항목 | 입력 예시 |
|------|----------|
| 프로젝트 이름 | `my-api` |
| 기본 패키지명 | `com.company.myapi` |
| DB 선택 | `mysql` 또는 `postgresql` |

생성된 프로젝트는 상위 디렉터리(`../my-api`)에 만들어집니다.

---

## 로컬에서 바로 실행 (H2 인메모리)

```bash
cd ../my-api
./gradlew bootRun
```

기본 프로파일 `local`이 H2 인메모리 DB를 사용하므로 **별도 DB 설치 없이** 즉시 실행 가능합니다.

- 헬스체크: http://localhost:8080/actuator/health
- H2 콘솔: http://localhost:8080/h2-console

---

## MySQL / PostgreSQL 사용

init 스크립트로 DB를 선택하면 `application.yml`의 프로파일이 자동 변경됩니다.  
실행 전에 아래 환경 변수를 설정하세요.

### Windows (PowerShell)
```powershell
$env:DB_URL      = "jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "yourpassword"
./gradlew bootRun
```

### Linux / macOS
```bash
export DB_URL="jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul"
export DB_USERNAME="root"
export DB_PASSWORD="yourpassword"
./gradlew bootRun
```

> ⚠️ **보안 주의**: 비밀번호 등 민감 정보는 절대 코드나 yml 파일에 직접 입력하지 마세요.

---

## 프로젝트 구조

```
src/main/java/{패키지}/
├── {ProjectName}Application.java      ← 진입점 (@EnableJpaAuditing 포함)
├── domain/                            ← 기능 단위 비즈니스 로직
│   └── {기능명}/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
├── auth/                              ← 인증/인가 ✅ 완료
│   ├── controller/
│   │   └── AuthController.java        ← POST /auth/login, /auth/refresh, /auth/logout
│   ├── service/
│   │   └── AuthService.java           ← 로그인, 토큰 재발급, 로그아웃 비즈니스 로직
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── TokenResponse.java
│   │   └── TokenRefreshRequest.java
│   ├── entity/
│   │   └── RefreshToken.java          ← RefreshToken DB 저장 엔티티
│   ├── repository/
│   │   └── RefreshTokenRepository.java
│   ├── jwt/
│   │   ├── JwtProvider.java           ← AccessToken/RefreshToken 생성·검증 (HS512)
│   │   └── JwtAuthenticationFilter.java ← Bearer 토큰 추출·검증 필터 (OncePerRequestFilter)
│   └── oauth/
│       ├── OAuth2UserInfo.java         ← 소셜 사용자 정보 추상 클래스
│       ├── GoogleOAuth2UserInfo.java
│       ├── NaverOAuth2UserInfo.java
│       ├── KakaoOAuth2UserInfo.java
│       ├── OAuth2UserInfoFactory.java  ← provider별 구현체 분기 팩토리
│       ├── CustomOAuth2UserService.java← OAuth2 사용자 로드 (회원 처리 훅 포함)
│       ├── OAuth2AuthenticationSuccessHandler.java ← 로그인 성공 시 JWT 발급
│       └── OAuth2AuthenticationFailureHandler.java ← 로그인 실패 에러 응답
├── common/                            ← 전역 공통 유틸 ✅ 완료
│   ├── entity/
│   │   └── BaseEntity.java            ← id, createdAt, updatedAt, deletedAt (소프트 딜리트)
│   └── exception/
│       ├── ErrorCode.java             ← HTTP status + code + message enum
│       ├── BusinessException.java     ← RuntimeException 확장, ErrorCode 보유
│       ├── ErrorResponse.java         ← 일관된 에러 응답 DTO (Validation 상세 포함)
│       └── GlobalExceptionHandler.java← @RestControllerAdvice 전역 처리
├── global/                            ← 공통 API 응답 구조 ✅ 완료
│   └── response/
│       ├── ApiResponse.java           ← 성공 응답 래퍼 (success, message, data)
│       └── PageResponse.java          ← 페이징 응답 래퍼 (Page<T> 변환)
├── global/                            ← 공통 API 응답 구조 ✅ 완료
│   ├── response/
│   │   ├── ApiResponse.java           ← 성공 응답 래퍼 (success, message, data)
│   │   └── PageResponse.java          ← 페이징 응답 래퍼 (Page<T> 변환)
│   └── config/
│       └── OpenApiConfig.java         ← Swagger/OpenAPI 설정, Bearer JWT 보안 스키마 ✅ 완료 (Phase 4)
└── config/                            ← Spring Bean 설정 ✅ 완료 (Phase 3)
    └── SecurityConfig.java            ← Spring Security + JWT 필터 + OAuth2 설정

src/main/resources/
├── application.yml                 ← 공통 설정 + 프로파일 분기
├── application-local.yml           ← H2 인메모리 (로컬 개발)
├── application-mysql.yml           ← MySQL 설정
└── application-postgresql.yml      ← PostgreSQL 설정
```

---

## Phase 4 — Swagger / OpenAPI

### 접근 경로 (로컬 환경에서만 활성화)

| 경로 | 설명 |
|------|------|
| http://localhost:8080/swagger-ui/index.html | Swagger UI (API 문서 & 테스트) |
| http://localhost:8080/v3/api-docs | OpenAPI 3.0 JSON 명세 |
| http://localhost:8080/v3/api-docs.yaml | OpenAPI 3.0 YAML 명세 |

> ⚠️ **보안 주의**: Swagger UI는 `application.yml`에서 기본 비활성화 상태입니다.  
> `application-local.yml`의 `springdoc.swagger-ui.enabled: true` 설정으로 로컬에서만 동작합니다.  
> 운영 환경(mysql, postgresql 프로파일)에서는 Swagger UI가 자동으로 비활성화됩니다.

### Swagger UI에서 JWT 인증하기

1. `POST /auth/login` API를 호출하여 AccessToken을 발급받습니다.
2. 우측 상단 **Authorize** 버튼을 클릭합니다.
3. `Bearer {발급받은_AccessToken}` 형식으로 입력하고 **Authorize** 버튼을 누릅니다.
4. 이후 자물쇠 아이콘이 잠긴 API들을 직접 테스트할 수 있습니다.

---

## Phase 3 — JWT 및 OAuth2 환경 변수 설정

### JWT Secret Key 생성 (Windows PowerShell)
```powershell
# 64바이트(512비트) 랜덤 Base64 키 생성
[Convert]::ToBase64String((1..64 | ForEach-Object { [byte](Get-Random -Max 256) }))
```

### 환경 변수 설정 (Windows PowerShell)
```powershell
$env:JWT_SECRET        = "여기에_위에서_생성한_Base64_키_입력"
$env:GOOGLE_CLIENT_ID  = "Google_콘솔에서_발급한_client_id"
$env:GOOGLE_CLIENT_SECRET = "Google_콘솔에서_발급한_client_secret"
$env:NAVER_CLIENT_ID   = "Naver_개발자센터에서_발급한_client_id"
$env:NAVER_CLIENT_SECRET  = "Naver_개발자센터에서_발급한_client_secret"
$env:KAKAO_CLIENT_ID   = "Kakao_개발자센터에서_발급한_REST_API_키"
$env:KAKAO_CLIENT_SECRET  = "Kakao_개발자센터에서_발급한_client_secret"
./gradlew bootRun
```

> ⚠️ **보안 주의**: 위 값들을 코드나 yml 파일에 직접 입력하지 마세요.

### OAuth2 키 발급 위치
| 제공자 | 발급 URL |
|--------|----------|
| Google | https://console.cloud.google.com → API 및 서비스 → 사용자 인증 정보 |
| Naver  | https://developers.naver.com → 애플리케이션 등록 |
| Kakao  | https://developers.kakao.com → 내 애플리케이션 |

### Redirect URI 등록 (각 제공자 콘솔에서 설정 필요)
```
http://localhost:8080/login/oauth2/code/google
http://localhost:8080/login/oauth2/code/naver
http://localhost:8080/login/oauth2/code/kakao
```

### 주요 API 엔드포인트
| 메서드 | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/auth/login` | 이메일/비밀번호 로그인 | 불필요 |
| POST | `/auth/refresh` | AccessToken 재발급 | 불필요 |
| POST | `/auth/logout` | 로그아웃 | 필요 |
| GET | `/oauth2/authorization/google` | Google 소셜 로그인 시작 | 불필요 |
| GET | `/oauth2/authorization/naver` | Naver 소셜 로그인 시작 | 불필요 |
| GET | `/oauth2/authorization/kakao` | Kakao 소셜 로그인 시작 | 불필요 |

### UserDetailsService 구현 안내
Phase 3 틀만 제공하므로 실제 프로젝트에서는 `UserDetailsService`를 구현해야 합니다.
```java
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().name())
                .build();
    }
}
```

---

## 빌드 및 테스트

```bash
# 빌드 (테스트는 H2 자동 사용)
./gradlew clean build

# 실행
./gradlew bootRun

# 테스트만 실행
./gradlew test
```

---

## 개발 로드맵

| 단계 | 내용 | 상태 |
|------|------|------|
| Phase 1 | 빌드 설정 + 패키지 골격 + init 스크립트 | ✅ 완료 |
| Phase 2 | `common` — BaseEntity, 전역 예외 처리(ErrorCode, BusinessException, GlobalExceptionHandler) | ✅ 완료 |
| Phase 2-B | `global/response` — ApiResponse\<T\>, PageResponse\<T\> 공통 응답 구조 | ✅ 완료 |
| Phase 3 | `auth` — JWT(AccessToken/RefreshToken), JwtProvider, JwtAuthenticationFilter, OAuth2 소셜 로그인(Google/Naver/Kakao), SecurityConfig | ✅ 완료 |
| Phase 4 | Swagger/OpenAPI — OpenApiConfig, Bearer JWT 인증 스키마 | ✅ 완료 |
| Phase 5 | 도메인 샘플 + 테스트 보강 | ⬜ 예정 |
