# Spring Boot Starter Kit

Spring Boot 3.5 기반의 재사용 가능한 백엔드 프로젝트 템플릿입니다.  
JWT 인증 · OAuth2 소셜 로그인 · Swagger UI · 전역 예외 처리가 모두 포함되어 있으며,  
`init.ps1` / `init.sh` 한 번 실행으로 **새 프로젝트를 즉시 생성**할 수 있습니다.

---

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 21 (Amazon Corretto 21) |
| Spring Boot | 3.5.x |
| 빌드 도구 | Gradle 8.x |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| OAuth2 | Google · Naver · Kakao |
| API 문서 | Swagger / OpenAPI 3.0 (springdoc 2.8) |
| DB (선택) | MySQL 8.x / PostgreSQL 14+ |
| 로컬 개발 DB | H2 인메모리 (별도 설치 불필요) |

---

## 개발 로드맵

| 단계 | 내용 | 상태 |
|------|------|------|
| Phase 1 | 빌드 설정 + 패키지 골격 + init 스크립트 | ✅ 완료 |
| Phase 2 | `common` — BaseEntity, 전역 예외 처리 (ErrorCode · BusinessException · GlobalExceptionHandler) | ✅ 완료 |
| Phase 2-B | `global/response` — ApiResponse\<T\> · PageResponse\<T\> 공통 응답 구조 | ✅ 완료 |
| Phase 3 | `auth` — JWT(AccessToken/RefreshToken) · JwtProvider · JwtAuthenticationFilter · OAuth2 소셜 로그인(Google/Naver/Kakao) · SecurityConfig | ✅ 완료 |
| Phase 4 | Swagger/OpenAPI — OpenApiConfig · Bearer JWT 인증 스키마 | ✅ 완료 |
| Phase 5 | 도메인 샘플 + 테스트 보강 | ⬜ 예정 |

---

## 빠른 시작 — 단계별 가이드

### Step 0. 사전 요구사항 확인

| 도구 | 버전 | 필수 여부 |
|------|------|-----------|
| **Java 21** | Amazon Corretto 21 권장 | ✅ 필수 |
| **Git** | 최신 버전 | ✅ 필수 |
| **Gradle 8.x** | Wrapper 생성에 필요 | ⬜ 선택 (IntelliJ IDEA 사용 시 불필요) |
| **IntelliJ IDEA** | 2023.x 이상 | ⬜ 선택 (권장) |

```bash
# Java 버전 확인
java -version
# 출력 예시: openjdk version "21.x.x" ...
```

---

### Step 1. 레포지토리 클론

```bash
git clone https://github.com/kim-hani/CLAUDE-SPRINGBOOT-STARTERKIT.git
cd CLAUDE-SPRINGBOOT-STARTERKIT
```

클론 후 디렉터리 구조:

```
CLAUDE-SPRINGBOOT-STARTERKIT/
├── template/          ← 실제 Spring Boot 템플릿 소스
├── init.ps1           ← Windows 프로젝트 생성 스크립트
├── init.sh            ← Linux / macOS 프로젝트 생성 스크립트
└── README.md
```

---

### Step 2. 새 프로젝트 생성 (init 스크립트 실행)

스크립트가 템플릿을 복사하고 패키지명·프로젝트명·DB 설정을 자동으로 치환합니다.

#### Windows (PowerShell)

```powershell
.\init.ps1
```

#### Linux / macOS (Bash)

```bash
chmod +x init.sh
./init.sh
```

#### 스크립트 실행 시 입력 항목

| 항목 | 입력 예시 | 설명 |
|------|----------|------|
| 프로젝트 이름 | `my-api` | 생성될 폴더명 및 Gradle 프로젝트명 |
| 기본 패키지명 | `com.company.myapi` | Java 패키지 경로 (소문자, 점으로 구분) |
| DB 선택 | `mysql` 또는 `postgresql` | application.yml 프로파일 자동 변경 |

#### 스크립트가 수행하는 작업 (내부 동작)

| 단계 | 설명 |
|------|------|
| [1/5] 템플릿 복사 | `template/` 디렉터리를 `../my-api/` 로 복사 |
| [2/5] 패키지명 치환 | 모든 `.java` · `.yml` · `.gradle` 파일의 `com.example.starter` → 입력한 패키지명 |
| [3/5] 디렉터리 재구성 | `src/main/java/com/example/starter` → `src/main/java/com/company/myapi` |
| [4/5] settings.gradle | 프로젝트명 업데이트 |
| [5/5] application.yml | `active: local` → `active: mysql` 또는 `active: postgresql` |

> 생성된 프로젝트는 **스타터킷 폴더의 상위 디렉터리**에 만들어집니다.  
> 예) `CLAUDE-SPRINGBOOT-STARTERKIT/`와 같은 레벨에 `my-api/` 생성

---

### Step 3. 환경 변수 설정

#### 3-1. JWT Secret Key 생성

애플리케이션 실행 전 JWT 서명에 사용할 512비트 이상의 랜덤 키를 생성합니다.

**Windows (PowerShell)**
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { [byte](Get-Random -Max 256) }))
```

**Linux / macOS**
```bash
openssl rand -base64 64
```

#### 3-2. 환경 변수 설정

> ⚠️ **보안 주의**: 아래 값들을 `.yml` 파일이나 소스 코드에 직접 입력하면 안 됩니다.  
> 반드시 환경 변수 또는 시스템 환경 변수로 주입하세요.

**Windows (PowerShell) — 현재 세션에 임시 설정**
```powershell
# JWT (필수)
$env:JWT_SECRET = "위에서_생성한_Base64_키"

# DB 연결 (mysql 또는 postgresql 선택 시 필수)
$env:DB_URL      = "jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "yourpassword"

# OAuth2 소셜 로그인 (소셜 로그인 사용 시 필수)
$env:GOOGLE_CLIENT_ID      = "Google_콘솔_client_id"
$env:GOOGLE_CLIENT_SECRET  = "Google_콘솔_client_secret"
$env:NAVER_CLIENT_ID       = "Naver_개발자센터_client_id"
$env:NAVER_CLIENT_SECRET   = "Naver_개발자센터_client_secret"
$env:KAKAO_CLIENT_ID       = "Kakao_개발자센터_REST_API_키"
$env:KAKAO_CLIENT_SECRET   = "Kakao_개발자센터_client_secret"
```

**Linux / macOS**
```bash
export JWT_SECRET="위에서_생성한_Base64_키"
export DB_URL="jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul"
export DB_USERNAME="root"
export DB_PASSWORD="yourpassword"
export GOOGLE_CLIENT_ID="..."
export GOOGLE_CLIENT_SECRET="..."
export NAVER_CLIENT_ID="..."
export NAVER_CLIENT_SECRET="..."
export KAKAO_CLIENT_ID="..."
export KAKAO_CLIENT_SECRET="..."
```

#### 3-3. OAuth2 키 발급 위치

| 제공자 | 발급 URL |
|--------|----------|
| Google | Google Cloud Console → API 및 서비스 → 사용자 인증 정보 |
| Naver  | Naver 개발자센터 → 애플리케이션 등록 |
| Kakao  | Kakao Developers → 내 애플리케이션 |

#### 3-4. OAuth2 Redirect URI 등록

각 제공자 콘솔에서 아래 URI를 **승인된 리디렉션 URI**로 등록하세요.

```
http://localhost:8080/login/oauth2/code/google
http://localhost:8080/login/oauth2/code/naver
http://localhost:8080/login/oauth2/code/kakao
```

---

### Step 4. 애플리케이션 실행

생성된 프로젝트 디렉터리로 이동합니다.

```bash
cd ../my-api
```

#### 4-1. H2 인메모리 DB로 즉시 실행 (로컬 개발 권장)

DB 설치 없이 바로 실행 가능합니다.  
단, init 스크립트에서 DB를 선택했다면 먼저 Step 3의 DB 환경 변수를 설정해야 합니다.

```bash
# application.yml을 active: local 로 되돌리거나
# 아래처럼 프로파일을 직접 지정하여 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Windows**
```powershell
.\gradlew bootRun
```

**Linux / macOS**
```bash
./gradlew bootRun
```

#### 4-2. MySQL 사용

```powershell
# Windows
$env:DB_URL      = "jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "yourpassword"
$env:JWT_SECRET  = "생성한_키"
.\gradlew bootRun
```

```bash
# Linux / macOS
export DB_URL="jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul"
export DB_USERNAME="root"
export DB_PASSWORD="yourpassword"
export JWT_SECRET="생성한_키"
./gradlew bootRun
```

#### 4-3. PostgreSQL 사용

```powershell
# Windows
$env:DB_URL      = "jdbc:postgresql://localhost:5432/mydb"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "yourpassword"
$env:JWT_SECRET  = "생성한_키"
.\gradlew bootRun
```

---

### Step 5. 기동 확인

서버가 정상적으로 실행되면 아래 URL로 상태를 확인합니다.

| URL | 설명 |
|-----|------|
| `http://localhost:8080/actuator/health` | 헬스체크 (H2 · MySQL · PostgreSQL 모두 동작) |
| `http://localhost:8080/h2-console` | H2 콘솔 (local 프로파일에서만 활성화) |
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI (local 프로파일에서만 활성화) |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3.0 JSON 명세 |

헬스체크 정상 응답 예시:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    ...
  }
}
```

---

### Step 6. Swagger UI에서 API 테스트

1. `http://localhost:8080/swagger-ui/index.html` 접속
2. `POST /auth/login` API로 AccessToken 발급
3. 우측 상단 **Authorize** 버튼 클릭
4. `Bearer {발급받은_AccessToken}` 형식으로 입력 후 **Authorize**
5. 자물쇠 아이콘이 잠긴 보호 API 테스트 가능

> ⚠️ Swagger UI는 `application-local.yml`에서만 활성화됩니다.  
> 운영 환경(mysql · postgresql 프로파일)에서는 자동으로 비활성화됩니다.

---

### Step 7. 도메인 기능 추가

새 비즈니스 기능은 `domain/` 패키지 하위에 아래 구조로 추가합니다.

```
src/main/java/{패키지}/domain/{기능명}/
├── controller/    ← @RestController, API 엔드포인트
├── service/       ← @Service, 비즈니스 로직
├── repository/    ← @Repository, JPA Repository 인터페이스
├── entity/        ← @Entity, BaseEntity 상속
└── dto/           ← 요청/응답 DTO
```

**Entity 예시** — BaseEntity를 상속하면 `id · createdAt · updatedAt · deletedAt` 자동 관리
```java
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
}
```

**응답 래퍼 사용 예시**
```java
// 단건 응답
return ApiResponse.success("조회 성공", postDto);

// 페이징 응답
return ApiResponse.success("목록 조회 성공", PageResponse.of(postPage));

// 에러 발생 (GlobalExceptionHandler가 자동 처리)
throw new BusinessException(ErrorCode.NOT_FOUND);
```

---

### Step 8. UserDetailsService 구현 (이메일/비밀번호 로그인 사용 시)

`AuthService`의 로그인 로직이 동작하려면 `UserDetailsService`를 직접 구현해야 합니다.

```java
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 이메일로 사용자를 조회하여 Spring Security UserDetails 반환
     */
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

## 주요 API 엔드포인트

| 메서드 | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/auth/login` | 이메일/비밀번호 로그인 → AccessToken + RefreshToken 발급 | 불필요 |
| POST | `/auth/refresh` | RefreshToken으로 AccessToken 재발급 | 불필요 |
| POST | `/auth/logout` | 로그아웃 (RefreshToken 삭제) | 필요 |
| GET | `/oauth2/authorization/google` | Google 소셜 로그인 시작 | 불필요 |
| GET | `/oauth2/authorization/naver` | Naver 소셜 로그인 시작 | 불필요 |
| GET | `/oauth2/authorization/kakao` | Kakao 소셜 로그인 시작 | 불필요 |
| GET | `/actuator/health` | 서버 헬스체크 | 불필요 |

---

## 프로젝트 구조

```
src/main/java/{패키지}/
├── {ProjectName}Application.java      ← 진입점 (@EnableJpaAuditing 포함)
├── domain/                            ← 기능 단위 비즈니스 로직 (직접 추가)
│   └── {기능명}/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
├── auth/                              ← 인증/인가 (완료)
│   ├── controller/AuthController.java ← POST /auth/login, /auth/refresh, /auth/logout
│   ├── service/AuthService.java       ← 로그인, 토큰 재발급, 로그아웃 비즈니스 로직
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── TokenResponse.java
│   │   └── TokenRefreshRequest.java
│   ├── entity/RefreshToken.java       ← RefreshToken DB 저장 엔티티
│   ├── repository/RefreshTokenRepository.java
│   ├── jwt/
│   │   ├── JwtProvider.java           ← AccessToken/RefreshToken 생성·검증 (HS512)
│   │   └── JwtAuthenticationFilter.java ← Bearer 토큰 추출·검증 필터
│   └── oauth/
│       ├── OAuth2UserInfo.java         ← 소셜 사용자 정보 추상 클래스
│       ├── GoogleOAuth2UserInfo.java
│       ├── NaverOAuth2UserInfo.java
│       ├── KakaoOAuth2UserInfo.java
│       ├── OAuth2UserInfoFactory.java  ← provider별 구현체 분기 팩토리
│       ├── CustomOAuth2UserService.java← OAuth2 사용자 로드 (회원 처리 훅 포함)
│       ├── OAuth2AuthenticationSuccessHandler.java ← 로그인 성공 시 JWT 발급
│       └── OAuth2AuthenticationFailureHandler.java ← 로그인 실패 에러 응답
├── common/                            ← 전역 공통 유틸 (완료)
│   ├── entity/BaseEntity.java         ← id, createdAt, updatedAt, deletedAt (소프트 딜리트)
│   └── exception/
│       ├── ErrorCode.java             ← HTTP status + code + message enum
│       ├── BusinessException.java     ← RuntimeException 확장, ErrorCode 보유
│       ├── ErrorResponse.java         ← 일관된 에러 응답 DTO
│       └── GlobalExceptionHandler.java← @RestControllerAdvice 전역 처리
├── global/                            ← 공통 API 응답 구조 (완료)
│   ├── response/
│   │   ├── ApiResponse.java           ← 성공 응답 래퍼 (success, message, data)
│   │   └── PageResponse.java          ← 페이징 응답 래퍼 (Page<T> 변환)
│   └── config/
│       └── OpenApiConfig.java         ← Swagger/OpenAPI 설정, Bearer JWT 보안 스키마
└── config/                            ← Spring Bean 설정 (완료)
    └── SecurityConfig.java            ← Spring Security + JWT 필터 + OAuth2 설정

src/main/resources/
├── application.yml              ← 공통 설정 + 프로파일 분기
├── application-local.yml        ← H2 인메모리 (로컬 개발)
├── application-mysql.yml        ← MySQL 설정
└── application-postgresql.yml   ← PostgreSQL 설정
```

---

## 빌드 및 테스트

```bash
# 전체 빌드 (테스트 포함, H2 자동 사용)
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun

# 테스트만 실행
./gradlew test
```

---

## 자주 묻는 질문 (FAQ)

**Q. `JWT_SECRET` 환경 변수를 설정하지 않으면?**  
> 애플리케이션 구동 시 `Could not resolve placeholder 'JWT_SECRET'` 에러가 발생합니다. Step 3을 참고하여 환경 변수를 설정하세요.

**Q. 소셜 로그인 없이 JWT만 사용하고 싶다면?**  
> `SecurityConfig.java`에서 `.oauth2Login(...)` 설정을 제거하고, OAuth2 환경 변수를 설정하지 않아도 됩니다.

**Q. H2 콘솔 접속 방법은?**  
> `http://localhost:8080/h2-console` 접속 → JDBC URL: `jdbc:h2:mem:testdb` · 사용자명: `sa` · 비밀번호: 빈칸

**Q. 운영 환경에서 Swagger가 노출되지 않도록 하려면?**  
> 기본적으로 `application.yml`에서 Swagger가 비활성화되어 있습니다. `local` 프로파일에서만 활성화되므로 별도 설정 없이 안전합니다.

**Q. `gradlew` 실행 시 권한 오류가 발생하면?**  
> Linux / macOS에서는 `chmod +x gradlew` 후 `./gradlew bootRun`을 실행하세요.
