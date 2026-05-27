#!/usr/bin/env bash
# Spring Boot Starter Kit — 프로젝트 초기화 스크립트 (Linux / macOS)
# 사용법: chmod +x init.sh && ./init.sh

set -euo pipefail

echo ""
echo "======================================"
echo "   Spring Boot Starter Kit 초기화"
echo "======================================"
echo ""

# ── 입력 받기 ──────────────────────────────────────────────────────────────────
read -rp "프로젝트 이름  (예: my-api)          : " PROJECT_NAME
read -rp "기본 패키지명 (예: com.company.myapi) : " PACKAGE_NAME
read -rp "DB 선택        [mysql/postgresql]     : " DB_CHOICE

# ── 유효성 검사 ────────────────────────────────────────────────────────────────
if [[ -z "$PROJECT_NAME" ]]; then
    echo "오류: 프로젝트 이름을 입력해야 합니다." >&2; exit 1
fi
if [[ ! "$PACKAGE_NAME" =~ ^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$ ]]; then
    echo "오류: 패키지명 형식이 올바르지 않습니다. (예: com.company.myapi)" >&2; exit 1
fi
if [[ "$DB_CHOICE" != "mysql" && "$DB_CHOICE" != "postgresql" ]]; then
    echo "오류: DB는 'mysql' 또는 'postgresql' 중 하나를 입력하세요." >&2; exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEMPLATE_DIR="$SCRIPT_DIR/template"
TARGET_DIR="$SCRIPT_DIR"

if [[ -f "$TARGET_DIR/build.gradle" ]]; then
    echo "오류: 이미 초기화된 프로젝트입니다. (build.gradle 이 이미 존재합니다)" >&2; exit 1
fi

OLD_PACKAGE="com.example.starter"
OLD_PACKAGE_PATH="com/example/starter"
NEW_PACKAGE_PATH="${PACKAGE_NAME//./\/}"

echo ""
echo "▶ 프로젝트 생성 중..."

# ── 1. 템플릿 복사 ────────────────────────────────────────────────────────────
cp -r "$TEMPLATE_DIR/." "$TARGET_DIR/"
rm -rf "$TEMPLATE_DIR"
echo "  [1/5] 템플릿 복사 완료"

# ── 2. 파일 내용 패키지명 치환 ─────────────────────────────────────────────────
find "$TARGET_DIR" -type f \( -name "*.java" -o -name "*.yml" -o -name "*.yaml" \
    -o -name "*.gradle" -o -name "*.properties" -o -name "*.xml" \) | while IFS= read -r file; do
    if grep -qF "$OLD_PACKAGE" "$file"; then
        # perl 사용: macOS/Linux 모두 호환
        perl -pi -e "s/\Q$OLD_PACKAGE\E/$PACKAGE_NAME/g" "$file"
    fi
done
echo "  [2/5] 패키지명 치환 완료"

# ── 3. Java 소스 디렉터리 구조 재구성 ─────────────────────────────────────────
JAVA_MAIN_OLD="$TARGET_DIR/src/main/java/$OLD_PACKAGE_PATH"
JAVA_MAIN_NEW="$TARGET_DIR/src/main/java/$NEW_PACKAGE_PATH"
JAVA_TEST_OLD="$TARGET_DIR/src/test/java/$OLD_PACKAGE_PATH"
JAVA_TEST_NEW="$TARGET_DIR/src/test/java/$NEW_PACKAGE_PATH"
OLD_ROOT="${OLD_PACKAGE%%.*}"  # "com"

if [[ -d "$JAVA_MAIN_OLD" ]]; then
    mkdir -p "$JAVA_MAIN_NEW"
    cp -r "$JAVA_MAIN_OLD/." "$JAVA_MAIN_NEW/"
    rm -rf "$TARGET_DIR/src/main/java/$OLD_ROOT"
fi
if [[ -d "$JAVA_TEST_OLD" ]]; then
    mkdir -p "$JAVA_TEST_NEW"
    cp -r "$JAVA_TEST_OLD/." "$JAVA_TEST_NEW/"
    rm -rf "$TARGET_DIR/src/test/java/$OLD_ROOT"
fi
echo "  [3/5] 디렉터리 구조 재구성 완료"

# ── 4. settings.gradle 프로젝트명 치환 ────────────────────────────────────────
perl -pi -e "s/starter-kit-template/$PROJECT_NAME/g" "$TARGET_DIR/settings.gradle"
echo "  [4/5] settings.gradle 업데이트 완료"

# ── 5. application.yml DB 프로파일 설정 ───────────────────────────────────────
perl -pi -e "s/active: local/active: $DB_CHOICE/g" \
    "$TARGET_DIR/src/main/resources/application.yml"
echo "  [5/5] application.yml DB 프로파일 설정 완료 ($DB_CHOICE)"

# ── 6. Gradle Wrapper 생성 (Gradle 설치 시) ────────────────────────────────────
if command -v gradle &>/dev/null; then
    echo ""
    echo "▶ Gradle Wrapper 생성 중..."
    (cd "$SCRIPT_DIR" && gradle wrapper --gradle-version 8.10.2 --quiet)
    chmod +x "$SCRIPT_DIR/gradlew"
    echo "  Gradle Wrapper 생성 완료"
else
    echo ""
    echo "⚠  Gradle이 설치되어 있지 않습니다."
    echo "   IntelliJ IDEA로 프로젝트를 열면 Gradle을 자동으로 설정합니다."
    echo "   또는 Gradle 설치 후 이 디렉터리에서 'gradle wrapper --gradle-version 8.10.2' 실행"
fi

# ── 7. 초기화 파일 정리 및 Git 재초기화 ──────────────────────────────────────
echo ""
echo "▶ Git 저장소 초기화 중..."
rm -f "$SCRIPT_DIR/init.sh" "$SCRIPT_DIR/init.ps1"
(cd "$SCRIPT_DIR" && rm -rf .git && git init -q && git add . \
    && git commit -q -m "chore: initialize project from Spring Boot Starter Kit")
echo "  Git 초기화 완료"

# ── 완료 메시지 ─────────────────────────────────────────────────────────────
echo ""
echo "======================================"
echo "  ✅ 프로젝트 생성 완료!"
echo "     $SCRIPT_DIR"
echo "======================================"
echo ""
echo "다음 단계:"
echo "  1. ./gradlew bootRun            # H2 인메모리로 바로 실행 가능"
echo "  2. http://localhost:8080/actuator/health 로 기동 확인"
echo ""
echo "MySQL/PostgreSQL 사용 시 환경 변수 설정:"
echo "  export DB_URL='jdbc:mysql://localhost:3306/mydb'"
echo "  export DB_USERNAME='root'"
echo "  export DB_PASSWORD='yourpassword'"
echo ""
