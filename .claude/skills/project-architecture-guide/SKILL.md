# 프로젝트 아키텍처 표준 가이드

## 패키지 구조

모든 도메인은 `com.example.starter.domain.{domain}/` 아래에 다음 레이어로 구성합니다.

```
src/main/java/{basePackage}/
└── domain/
    └── {domainName}/                        # 소문자 단수형 (예: post, product)
        ├── controller/
        │   └── {Domain}Controller.java
        ├── service/
        │   └── {Domain}Service.java         # 비즈니스 로직 (단일 클래스)
        ├── repository/
        │   └── {Domain}Repository.java
        ├── entity/
        │   └── {Domain}.java
        └── dto/
            ├── {Domain}CreateRequest.java
            ├── {Domain}UpdateRequest.java   # 수정 API가 있는 경우
            └── {Domain}Response.java
```

공통 인프라는 도메인 패키지 밖에 위치합니다.

```
common/
├── entity/BaseEntity.java             # id, createdAt, updatedAt, deletedAt
├── exception/ErrorCode.java           # 전역 에러 코드 enum
├── exception/BusinessException.java   # 비즈니스 예외 최상위 클래스
├── exception/ErrorResponse.java       # 에러 응답 포맷
└── exception/GlobalExceptionHandler.java

global/
├── response/ApiResponse.java          # 성공 응답 래퍼
├── response/PageResponse.java         # 페이지네이션 응답 래퍼
└── config/OpenApiConfig.java
```

---

## 네이밍 규칙

| 레이어 | 클래스명 패턴 | 예시 |
|---|---|---|
| Entity | `{Domain}` | `Post` |
| Controller | `{Domain}Controller` | `PostController` |
| Service | `{Domain}Service` | `PostService` |
| Repository | `{Domain}Repository` | `PostRepository` |
| 생성 요청 DTO | `{Domain}CreateRequest` | `PostCreateRequest` |
| 수정 요청 DTO | `{Domain}UpdateRequest` | `PostUpdateRequest` |
| 응답 DTO | `{Domain}Response` | `PostResponse` |
| 테이블명 | `{domain}` (스네이크케이스 단수) | `post` |
| URL 경로 | `/api/{domains}` (소문자 복수형) | `/api/posts` |

---

## 레이어별 작성 규칙

### Entity
```java
@Getter
@Entity
@Table(name = "{domain}")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class {Domain} extends BaseEntity {
    // 필드 선언 — setter 금지, 상태 변경은 도메인 메서드로 표현
    // Enum 타입: @Enumerated(EnumType.STRING) 적용
    // 비즈니스 메서드: update*, change*, cancel* 등

    @Builder
    public {Domain}(...) { ... }
}
```
- `BaseEntity` 상속 필수 — id, createdAt, updatedAt, deletedAt 자동 포함
- setter 금지, 상태 변경은 의미있는 도메인 메서드로

### Repository
```java
public interface {Domain}Repository extends JpaRepository<{Domain}, Long> {
    // 소프트 딜리트 사용 시
    @Query("SELECT e FROM {Domain} e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<{Domain}> findActiveById(@Param("id") Long id);

    @Query("SELECT e FROM {Domain} e WHERE e.deletedAt IS NULL")
    Page<{Domain}> findAllActive(Pageable pageable);
}
```

### DTO
```java
// Request: Jakarta Validation 어노테이션 적용
@Getter
@NoArgsConstructor
public class {Domain}CreateRequest {
    @NotBlank
    @Size(max = 200)
    private String title;
}

// Response: Entity → DTO 변환은 정적 팩토리 메서드 사용
@Getter
@Builder
public class {Domain}Response {
    private Long id;
    private String title;
    private LocalDateTime createdAt;

    public static {Domain}Response from({Domain} entity) {
        return {Domain}Response.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
```

### Service
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // 클래스 기본: 조회 최적화
public class {Domain}Service {

    private final {Domain}Repository {domain}Repository;

    public {Domain}Response get{Domain}(Long id) {
        {Domain} {domain} = {domain}Repository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND));
        return {Domain}Response.from({domain});
    }

    @Transactional   // 쓰기 작업은 메서드 레벨 재선언
    public {Domain}Response create{Domain}({Domain}CreateRequest request) { ... }

    @Transactional
    public void delete{Domain}(Long id) {
        {Domain} {domain} = {domain}Repository.findActiveById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND));
        {domain}.markDeleted();
    }
}
```
- 엔티티 직접 반환 금지 — 반드시 DTO 변환 후 반환

### Controller
```java
@Tag(name = "{Domain}", description = "{도메인 설명}")
@RestController
@RequestMapping("/api/{domains}")
@RequiredArgsConstructor
public class {Domain}Controller {

    private final {Domain}Service {domain}Service;

    @Operation(summary = "단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<{Domain}Response>> get{Domain}(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success({domain}Service.get{Domain}(id)));
    }

    @Operation(summary = "생성")
    @PostMapping
    public ResponseEntity<ApiResponse<{Domain}Response>> create{Domain}(
            @Valid @RequestBody {Domain}CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success({domain}Service.create{Domain}(request)));
    }

    @Operation(summary = "삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete{Domain}(@PathVariable Long id) {
        {domain}Service.delete{Domain}(id);
        return ResponseEntity.ok(ApiResponse.successWithMessage("삭제되었습니다."));
    }
}
```

| HTTP 메서드 | 상황 | 반환 |
|---|---|---|
| GET | 조회 | `ResponseEntity.ok(ApiResponse.success(data))` |
| POST | 생성 | `ResponseEntity.status(CREATED).body(ApiResponse.success(data))` |
| PUT / PATCH | 수정 | `ResponseEntity.ok(ApiResponse.success(data))` |
| DELETE | 삭제 | `ResponseEntity.ok(ApiResponse.successWithMessage("삭제되었습니다."))` |
| 페이지네이션 | 목록 | `ApiResponse<PageResponse<{Domain}Response>>` |

---

## 데이터 흐름

```
HTTP 요청
  └─▶ Controller   (@Valid 검증, 인증 정보 추출)
        └─▶ Service    (비즈니스 로직, 트랜잭션 경계)
              └─▶ Repository  (DB 접근)
                    └─▶ Entity     (상태 변경 메서드)
              └─▶ DTO 변환 후 반환 ({Domain}Response.from(entity))
        └─▶ ApiResponse 래핑
  ◀─── HTTP 응답
```

오류 발생 시:
```
Service → throw BusinessException(ErrorCode.{DOMAIN}_XXX)
  └─▶ GlobalExceptionHandler → ErrorResponse 자동 반환
      (Controller에 try-catch 추가 금지)
```
