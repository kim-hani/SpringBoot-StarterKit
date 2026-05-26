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
git clone https://github.com/kim-hani/SpringBoot-StarterKit.git
cd SpringBoot-StarterKit
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

### Step 3. 민감 정보 보안 설정 (필수)

> ⛔ **절대 금지**: JWT 비밀키, DB 접속 정보, OAuth2 키를 `.yml` 파일이나 소스 코드에 **직접 입력하지 마세요.**  
> 이 정보가 GitHub에 한 번이라도 올라가면, 계정 탈취·DB 무단 접근·토큰 위조 등의 보안 사고로 이어질 수 있습니다.  
> 이 프로젝트는 모든 민감 정보를 **환경 변수**로 주입받도록 설계되어 있습니다.

---

#### 3-1. YML 설정 구조 이해

각 `application-{profile}.yml` 파일은 실제 값 대신 `${환경변수명}` 형태로 외부 값을 참조합니다.

**`application.yml` — JWT 설정**
```yaml
jwt:
  secret: ${JWT_SECRET}        # 기본값 없음 — 환경 변수 미설정 시 앱 실행 자체가 실패
  access-token-expiration: 1800000      # AccessToken 30분 (ms)
  refresh-token-expiration: 1209600000  # RefreshToken 14일 (ms)
```

**`application-mysql.yml` — MySQL DB 설정**
```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/starterdb?serverTimezone=Asia/Seoul}
    username: ${DB_USERNAME:root}   # 콜론(:) 뒤는 환경 변수 미설정 시 사용할 기본값
    password: ${DB_PASSWORD:}       # 기본값 없음 (빈 문자열)
```

**`application-local.yml` — OAuth2 소셜 로그인 설정 (로컬 개발)**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}        # 기본값 없음 — 소셜 로그인 사용 시 필수
            client-secret: ${GOOGLE_CLIENT_SECRET}
          naver:
            client-id: ${NAVER_CLIENT_ID}
            client-secret: ${NAVER_CLIENT_SECRET}
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
```

> 💡 `${변수명:기본값}` 형식은 환경 변수가 없을 때 기본값을 사용합니다.  
> `${JWT_SECRET}`처럼 기본값이 없으면 환경 변수 미설정 시 애플리케이션 시작이 실패합니다.

---

#### 3-2. .gitignore 필수 확인

생성된 프로젝트의 `.gitignore`에 아래 항목이 반드시 포함되어 있어야 합니다.

```gitignore
# 환경 변수 파일 — 절대 커밋 금지
.env
.env.local

# IntelliJ 로컬 설정 (환경 변수 포함 가능)
.idea/workspace.xml
.idea/tasks.xml

# 빌드 산출물
build/
.gradle/
```

> ⚠️ `.env` 파일을 사용한다면, `.gitignore`에 추가 전까지 절대 `git add`하지 마세요.  
> 한 번이라도 커밋·푸시되면 git 히스토리에 영구 기록되어 키 재발급이 필요합니다.

---

#### 3-3. JWT Secret Key 생성

애플리케이션 실행 전 JWT 서명에 사용할 512비트 이상의 랜덤 키를 생성합니다.

**Windows (PowerShell)**
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { [byte](Get-Random -Max 256) }))
```

**Linux / macOS**
```bash
openssl rand -base64 64
```

출력된 Base64 문자열 전체를 복사해 둡니다.

---

#### 3-4. 환경 변수 설정 방법

아래 세 가지 방법 중 하나를 선택합니다. **방법 A (IntelliJ Run Configuration)를 권장**합니다.

---

##### 방법 A. IntelliJ Run Configuration (로컬 개발 권장)

소스 코드와 완전히 분리되어 가장 안전하고 편리합니다.

1. 상단 메뉴 **Run → Edit Configurations...**
2. 실행 설정(`StarterApplication`) 선택
3. **Environment variables** 항목 오른쪽 편집 아이콘(`...`) 클릭
4. `+` 버튼으로 아래 값 입력 후 **OK → Apply → OK**

| Name | Value | 비고 |
|------|-------|------|
| `JWT_SECRET` | Step 3-3에서 생성한 Base64 값 | 필수 |
| `DB_URL` | `jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul` | MySQL 사용 시 |
| `DB_USERNAME` | `root` | MySQL 사용 시 |
| `DB_PASSWORD` | DB 비밀번호 | MySQL 사용 시 |
| `GOOGLE_CLIENT_ID` | Google Cloud 콘솔에서 발급 | 소셜 로그인 사용 시 |
| `GOOGLE_CLIENT_SECRET` | Google Cloud 콘솔에서 발급 | 소셜 로그인 사용 시 |
| `NAVER_CLIENT_ID` | Naver 개발자센터에서 발급 | 소셜 로그인 사용 시 |
| `NAVER_CLIENT_SECRET` | Naver 개발자센터에서 발급 | 소셜 로그인 사용 시 |
| `KAKAO_CLIENT_ID` | Kakao Developers에서 발급 | 소셜 로그인 사용 시 |
| `KAKAO_CLIENT_SECRET` | Kakao Developers에서 발급 | 소셜 로그인 사용 시 |

---

##### 방법 B. 터미널 세션 임시 설정

터미널을 닫으면 사라지는 일시적 설정입니다. `./gradlew bootRun` 전에 실행하세요.

**Windows (PowerShell)**
```powershell
# JWT (필수)
$env:JWT_SECRET = "위에서_생성한_Base64_키"

# DB 연결 (mysql / postgresql 선택 시 필수)
$env:DB_URL      = "jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "yourpassword"

# OAuth2 소셜 로그인 (소셜 로그인 사용 시)
$env:GOOGLE_CLIENT_ID     = "Google_콘솔_client_id"
$env:GOOGLE_CLIENT_SECRET = "Google_콘솔_client_secret"
$env:NAVER_CLIENT_ID      = "Naver_개발자센터_client_id"
$env:NAVER_CLIENT_SECRET  = "Naver_개발자센터_client_secret"
$env:KAKAO_CLIENT_ID      = "Kakao_개발자센터_REST_API_키"
$env:KAKAO_CLIENT_SECRET  = "Kakao_개발자센터_client_secret"
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

---

##### 방법 C. `.env` 파일 사용 (팀 개발 시 편리)

**Step 1.** 프로젝트 루트에 `.env` 파일 생성 (`.gitignore`에 이미 추가되어 있어야 함)

```dotenv
# .env — 절대 Git에 커밋하지 마세요
JWT_SECRET=위에서_생성한_Base64_키

DB_URL=jdbc:mysql://localhost:3306/mydb?serverTimezone=Asia/Seoul
DB_USERNAME=root
DB_PASSWORD=yourpassword

GOOGLE_CLIENT_ID=xxx
GOOGLE_CLIENT_SECRET=xxx
NAVER_CLIENT_ID=xxx
NAVER_CLIENT_SECRET=xxx
KAKAO_CLIENT_ID=xxx
KAKAO_CLIENT_SECRET=xxx
```

**Step 2.** 팀원 공유용 `.env.example` 파일을 Git에 추가 (실제 값 없이 키 이름만 기재)

```dotenv
# .env.example — 팀원 가이드용 (실제 값 절대 입력 금지)
JWT_SECRET=
DB_URL=
DB_USERNAME=
DB_PASSWORD=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
```

> ⚠️ Spring Boot는 `.env` 파일을 자동으로 읽지 않습니다.  
> IntelliJ의 **EnvFile 플러그인** 설치 후 Run Configuration에서 `.env` 경로를 지정하거나,  
> 터미널에서 직접 로드(`source .env` — Linux/macOS) 후 `./gradlew bootRun`을 실행하세요.

---

#### 3-5. OAuth2 키 발급 위치

| 제공자 | 발급 URL |
|--------|----------|
| Google | Google Cloud Console → API 및 서비스 → 사용자 인증 정보 |
| Naver  | Naver 개발자센터 → 애플리케이션 등록 |
| Kakao  | Kakao Developers → 내 애플리케이션 |

---

#### 3-6. OAuth2 Redirect URI 등록

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

## Claude Code 에이전트

이 StarterKit에는 Claude Code 전용 에이전트가 내장되어 있습니다.  
도메인 명세를 입력하면 Entity · Repository · DTO · Service · Controller · ErrorCode 파일을 **한 번에 자동 생성**합니다.

### 사전 요구사항

- [Claude Code](https://claude.ai/code) 설치 및 로그인
- 이 레포지토리를 클론한 상태

### 실행 방법

**1. 프로젝트 루트에서 Claude Code 실행**

```bash
claude
```

**2. 에이전트 호출**

Claude Code 채팅창에서 `@domain-scaffolder`로 에이전트를 직접 호출합니다.

```
@domain-scaffolder Product 도메인 만들어줘
```

자연어로 설명해도 자동으로 에이전트가 실행됩니다.

```
상품(Product) 도메인 추가해줘. 상품명, 가격, 재고수량, 카테고리가 필요해
Order 도메인 만들어줘. 주문번호, 주문일시, 총금액, 주문상태(PENDING/CONFIRMED/CANCELLED)가 필요해
```

### 에이전트가 수집하는 항목

에이전트는 코드 생성 전 아래 명세를 인터랙티브하게 수집합니다.

| 항목 | 예시 |
|------|------|
| 도메인 이름 (PascalCase) | `Post`, `Product`, `Order` |
| 필드 목록 | `title / String / NotBlank, max=200 / 게시글 제목` |
| 생성할 API | 목록 조회, 단건 조회, 생성, 수정, 삭제 |
| 인증/인가 설정 | 인증 필요 여부, 대상 API |
| 특이 비즈니스 규칙 | "본인 게시글만 수정/삭제 가능" |

명세를 모두 수집한 뒤 생성 계획을 먼저 보여주고 확인 후 파일을 생성합니다.

### 자동 생성 파일 목록

```
domain/{domain}/
├── entity/{Domain}.java                  ← BaseEntity 상속, JPA 어노테이션
├── repository/{Domain}Repository.java    ← JpaRepository + 소프트 딜리트 쿼리
├── dto/{Domain}CreateRequest.java        ← Jakarta Validation 어노테이션 적용
├── dto/{Domain}UpdateRequest.java
├── dto/{Domain}Response.java             ← Entity → DTO 정적 팩토리 메서드
├── service/{Domain}Service.java          ← @Transactional 비즈니스 로직
└── controller/{Domain}Controller.java    ← ApiResponse 래핑, Swagger 어노테이션
```

`common/exception/ErrorCode.java`에 도메인 에러 코드 섹션도 자동으로 추가됩니다.

---

## 자주 묻는 질문 (FAQ)

**Q. `JWT_SECRET` 환경 변수를 설정하지 않으면?**  
> 애플리케이션 구동 시 `Could not resolve placeholder 'JWT_SECRET'` 에러가 발생합니다.  
> Step 3-3에서 키를 생성한 뒤, Step 3-4의 방법 A(IntelliJ Run Configuration)로 등록하거나 터미널에서 `$env:JWT_SECRET="키값"` 설정 후 실행하세요.

**Q. `.yml` 파일에 직접 값을 넣으면 안 되나요?**  
> 절대 안 됩니다. `.yml` 파일은 Git에 커밋되므로 비밀키나 DB 비밀번호가 GitHub에 그대로 노출됩니다.  
> 반드시 `${환경변수명}` 형태로 외부에서 주입하고, 실제 값은 IntelliJ Run Configuration 또는 `.env` 파일(`.gitignore` 필수)에만 보관하세요.

**Q. OAuth2 로그인 없이 JWT만 쓰고 싶다면?**  
> `SecurityConfig.java`에서 `.oauth2Login(...)` 블록을 제거하고, OAuth2 관련 환경 변수(`GOOGLE_*`, `NAVER_*`, `KAKAO_*`)는 설정하지 않아도 됩니다.

**Q. H2 콘솔 접속 방법은?**  
> `http://localhost:8080/h2-console` 접속 → JDBC URL: `jdbc:h2:mem:testdb` · 사용자명: `sa` · 비밀번호: 빈칸

**Q. 운영 환경에서 Swagger가 노출되지 않도록 하려면?**  
> 기본적으로 `application.yml`에서 Swagger가 비활성화되어 있습니다. `local` 프로파일에서만 활성화되므로 별도 설정 없이 안전합니다.

**Q. `gradlew` 실행 시 권한 오류가 발생하면?**  
> Linux / macOS에서는 `chmod +x gradlew` 후 `./gradlew bootRun`을 실행하세요.
