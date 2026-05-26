# 검증 및 예외 처리 규칙

## Jakarta Validation — 타입별 권장 어노테이션

| Java 타입 | 조건 | 어노테이션 | 비고 |
|---|---|---|---|
| `String` | 필수, 빈 문자열 불가 | `@NotBlank` | null + "" + " " 모두 거부 |
| `String` | null만 불가 | `@NotNull` | 빈 문자열은 허용 |
| `String` | 최대 길이 | `@Size(max = N)` | DB 컬럼 길이와 맞출 것 |
| `String` | 길이 범위 | `@Size(min = M, max = N)` | |
| `String` | 이메일 형식 | `@Email` | |
| `String` | 정규식 패턴 | `@Pattern(regexp = "...")` | 전화번호, 코드 등 |
| `Integer` / `Long` | 필수 | `@NotNull` | 기본형(int/long)은 불필요 |
| `Integer` / `Long` | 최솟값 | `@Min(N)` | 음수 불가: `@Min(0)` |
| `Integer` / `Long` | 최댓값 | `@Max(N)` | |
| `Integer` / `Long` | 양수 | `@Positive` | 0 포함이면 `@PositiveOrZero` |
| `BigDecimal` | 정수/소수 자릿수 | `@Digits(integer=N, fraction=M)` | |
| `BigDecimal` | 양수만 | `@Positive` | |
| `Boolean` | 필수 | `@NotNull` | |
| `List` / `Set` | null 불가 + 비어있으면 안 됨 | `@NotEmpty` | |
| `List` / `Set` | 크기 제한 | `@Size(min=N, max=M)` | |
| `Enum` | 필수 | `@NotNull` | |
| `LocalDate` / `LocalDateTime` | 과거만 허용 | `@Past` | 생년월일 등 |
| `LocalDate` / `LocalDateTime` | 미래만 허용 | `@Future` | 예약일 등 |
| `LocalDate` / `LocalDateTime` | 현재 이후 허용 | `@FutureOrPresent` | |

### 어노테이션 조합 예시

```java
// 제목: 필수, 최대 200자
@NotBlank
@Size(max = 200)
private String title;

// 가격: 필수, 0 이상
@NotNull
@Min(0)
private Integer price;

// 이메일: 필수, 형식 검증
@NotBlank
@Email
private String email;

// 상태 Enum: 필수
@NotNull
private PostStatus status;

// 태그 목록: 최대 10개
@Size(max = 10)
private List<String> tags;
```

---

## ErrorCode 추가 규칙

### 파일 위치
`template/src/main/java/com/example/starter/common/exception/ErrorCode.java`

### 섹션 구조

기존 마지막 섹션 아래에 새 도메인 섹션을 추가합니다.

```java
// ==================== {DOMAIN} ({도메인 한글 설명}) ====================
{DOMAIN}_NOT_FOUND(HttpStatus.NOT_FOUND,       "{DOMAIN}-001", "{도메인}을(를) 찾을 수 없습니다."),
{DOMAIN}_DUPLICATE(HttpStatus.CONFLICT,         "{DOMAIN}-002", "이미 존재하는 {도메인}입니다."),
{DOMAIN}_FORBIDDEN(HttpStatus.FORBIDDEN,        "{DOMAIN}-003", "{도메인}에 대한 권한이 없습니다."),
{DOMAIN}_INVALID_STATUS(HttpStatus.BAD_REQUEST, "{DOMAIN}-004", "유효하지 않은 상태입니다.");
```

### 코드 채번 규칙
- 도메인 prefix: 영문 대문자 스네이크케이스 (`POST`, `PRODUCT`, `ORDER`)
- 번호: 동일 도메인 내 `001`부터 순서 증가
- 마지막 항목을 제외하고 `,` 쉼표, 마지막 항목은 `;` 세미콜론

### 상황별 권장 ErrorCode

| 상황 | HTTP 상태 | 코드 접미사 | 예시 메시지 |
|---|---|---|---|
| 리소스 없음 | 404 NOT_FOUND | `_NOT_FOUND` | "게시글을 찾을 수 없습니다." |
| 중복 생성 | 409 CONFLICT | `_DUPLICATE` | "이미 존재하는 상품명입니다." |
| 권한 없음 | 403 FORBIDDEN | `_FORBIDDEN` | "수정 권한이 없습니다." |
| 유효하지 않은 상태 | 400 BAD_REQUEST | `_INVALID_STATUS` | "취소된 주문은 수정할 수 없습니다." |
| 재고/수량 부족 | 400 BAD_REQUEST | `_OUT_OF_STOCK` | "재고가 부족합니다." |
| 기간 만료 | 400 BAD_REQUEST | `_EXPIRED` | "만료된 쿠폰입니다." |
| 이미 삭제됨 | 404 NOT_FOUND | `_NOT_FOUND` | (소프트 딜리트는 NOT_FOUND로 통일) |

---

## 예외 처리 패턴

### 기본 조회 패턴
```java
{Domain} {domain} = {domain}Repository.findActiveById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND));
```

### 커스텀 메시지가 필요한 경우
```java
throw new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND,
        "ID " + id + "에 해당하는 {도메인}을 찾을 수 없습니다.");
```

### 조건부 예외

```java
// 중복 검사
if ({domain}Repository.existsByTitle(request.getTitle())) {
    throw new BusinessException(ErrorCode.{DOMAIN}_DUPLICATE);
}

// 소유권 검사
if (!{domain}.getAuthorId().equals(currentUserId)) {
    throw new BusinessException(ErrorCode.{DOMAIN}_FORBIDDEN);
}

// 상태 검사
if ({domain}.getStatus() == Status.CANCELLED) {
    throw new BusinessException(ErrorCode.{DOMAIN}_INVALID_STATUS);
}
```

### Controller에 try-catch 금지
`GlobalExceptionHandler`가 `BusinessException`을 자동으로 `ErrorResponse`로 변환합니다.
Controller에 예외 처리 코드를 추가하지 마세요.

---

## 소프트 딜리트 패턴

소프트 딜리트 도메인은 Repository 쿼리에 반드시 `deletedAt IS NULL` 조건을 포함하고,
삭제 실행은 `markDeleted()`로 처리합니다.

```java
// Repository
@Query("SELECT e FROM {Domain} e WHERE e.id = :id AND e.deletedAt IS NULL")
Optional<{Domain}> findActiveById(@Param("id") Long id);

// Service — 삭제 실행
@Transactional
public void delete{Domain}(Long id) {
    {Domain} {domain} = {domain}Repository.findActiveById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.{DOMAIN}_NOT_FOUND));
    {domain}.markDeleted();   // BaseEntity.markDeleted() — deletedAt = now()
}
```

삭제된 리소스를 다시 단건 조회하면 `findActiveById`가 `Optional.empty()`를 반환하여
`{DOMAIN}_NOT_FOUND`로 처리됩니다. 별도의 삭제 여부 체크 로직은 불필요합니다.
