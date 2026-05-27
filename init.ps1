# Spring Boot Starter Kit — 프로젝트 초기화 스크립트 (Windows PowerShell)
# 사용법: .\init.ps1

param()
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "   Spring Boot Starter Kit 초기화"     -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# ── 입력 받기 ──────────────────────────────────────────────────────────────────
$projectName = Read-Host "프로젝트 이름  (예: my-api)"
$packageName  = Read-Host "기본 패키지명 (예: com.company.myapi)"
$dbChoice     = Read-Host "DB 선택        [mysql/postgresql]"

# ── 유효성 검사 ────────────────────────────────────────────────────────────────
if ([string]::IsNullOrWhiteSpace($projectName)) {
    Write-Host "오류: 프로젝트 이름을 입력해야 합니다." -ForegroundColor Red
    exit 1
}
if ([string]::IsNullOrWhiteSpace($packageName) -or $packageName -notmatch '^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*)+$') {
    Write-Host "오류: 패키지명 형식이 올바르지 않습니다. (예: com.company.myapi)" -ForegroundColor Red
    exit 1
}
if ($dbChoice -ne "mysql" -and $dbChoice -ne "postgresql") {
    Write-Host "오류: DB는 'mysql' 또는 'postgresql' 중 하나를 입력하세요." -ForegroundColor Red
    exit 1
}

$scriptDir   = Split-Path -Parent $MyInvocation.MyCommand.Path
$templateDir = Join-Path $scriptDir "template"
$targetDir   = $scriptDir

if (Test-Path (Join-Path $targetDir "build.gradle")) {
    Write-Host "오류: 이미 초기화된 프로젝트입니다. (build.gradle 이 이미 존재합니다)" -ForegroundColor Red
    exit 1
}

$oldPackage     = "com.example.starter"
$oldPackagePath = "com\example\starter"
$newPackagePath = $packageName.Replace(".", "\")

Write-Host ""
Write-Host "▶ 프로젝트 생성 중..." -ForegroundColor Yellow

# ── 1. 템플릿 복사 ────────────────────────────────────────────────────────────
Copy-Item -Path "$templateDir\*" -Destination $targetDir -Recurse -Force
Remove-Item -Path $templateDir -Recurse -Force
Write-Host "  [1/5] 템플릿 복사 완료" -ForegroundColor Green

# ── 2. 파일 내용 패키지명 치환 ─────────────────────────────────────────────────
Get-ChildItem -Path $targetDir -Recurse -File |
    Where-Object { $_.Extension -in @(".java", ".yml", ".yaml", ".gradle", ".properties", ".xml") } |
    ForEach-Object {
        $content = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
        if ($content.Contains($oldPackage)) {
            $content = $content.Replace($oldPackage, $packageName)
            [System.IO.File]::WriteAllText($_.FullName, $content, [System.Text.Encoding]::UTF8)
        }
    }
Write-Host "  [2/5] 패키지명 치환 완료" -ForegroundColor Green

# ── 3. Java 소스 디렉터리 구조 재구성 ─────────────────────────────────────────
function Move-PackageDirectory {
    param(
        [string]$oldBase,
        [string]$newBase,
        [string]$comRoot   # 삭제할 최상위 com 디렉터리
    )
    if (-not (Test-Path $oldBase)) { return }
    New-Item -ItemType Directory -Force -Path $newBase | Out-Null
    Get-ChildItem -Path $oldBase -Recurse | ForEach-Object {
        $dest = $_.FullName.Replace($oldBase, $newBase)
        if ($_.PSIsContainer) {
            New-Item -ItemType Directory -Force -Path $dest | Out-Null
        } else {
            Copy-Item -Path $_.FullName -Destination $dest -Force
        }
    }
    Remove-Item -Path $comRoot -Recurse -Force -ErrorAction SilentlyContinue
}

$javaMainOld = Join-Path $targetDir "src\main\java\$oldPackagePath"
$javaMainNew = Join-Path $targetDir "src\main\java\$newPackagePath"
$javaTestOld = Join-Path $targetDir "src\test\java\$oldPackagePath"
$javaTestNew = Join-Path $targetDir "src\test\java\$newPackagePath"
$mainComRoot = Join-Path $targetDir "src\main\java\com"
$testComRoot = Join-Path $targetDir "src\test\java\com"

Move-PackageDirectory $javaMainOld $javaMainNew $mainComRoot
Move-PackageDirectory $javaTestOld $javaTestNew $testComRoot
Write-Host "  [3/5] 디렉터리 구조 재구성 완료" -ForegroundColor Green

# ── 4. settings.gradle 프로젝트명 치환 ────────────────────────────────────────
$settingsFile = Join-Path $targetDir "settings.gradle"
$content = [System.IO.File]::ReadAllText($settingsFile, [System.Text.Encoding]::UTF8)
$content = $content.Replace("starter-kit-template", $projectName)
[System.IO.File]::WriteAllText($settingsFile, $content, [System.Text.Encoding]::UTF8)
Write-Host "  [4/5] settings.gradle 업데이트 완료" -ForegroundColor Green

# ── 5. application.yml DB 프로파일 설정 ───────────────────────────────────────
$appYml = Join-Path $targetDir "src\main\resources\application.yml"
$content = [System.IO.File]::ReadAllText($appYml, [System.Text.Encoding]::UTF8)
$content = $content.Replace("active: local", "active: $dbChoice")
[System.IO.File]::WriteAllText($appYml, $content, [System.Text.Encoding]::UTF8)
Write-Host "  [5/5] application.yml DB 프로파일 설정 완료 ($dbChoice)" -ForegroundColor Green

# ── 6. Gradle Wrapper 생성 (Gradle 설치 시) ────────────────────────────────────
Push-Location $scriptDir
try {
    if (Get-Command gradle -ErrorAction SilentlyContinue) {
        Write-Host ""
        Write-Host "▶ Gradle Wrapper 생성 중..." -ForegroundColor Yellow
        gradle wrapper --gradle-version 8.10.2 --quiet
        Write-Host "  Gradle Wrapper 생성 완료" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "⚠  Gradle이 설치되어 있지 않습니다." -ForegroundColor Yellow
        Write-Host "   IntelliJ IDEA로 프로젝트를 열면 Gradle을 자동으로 설정합니다." -ForegroundColor Yellow
        Write-Host "   또는 Gradle 설치 후 이 디렉터리에서 'gradle wrapper --gradle-version 8.10.2' 실행" -ForegroundColor Yellow
    }
} finally {
    Pop-Location
}

# ── 7. 초기화 파일 정리 및 Git 재초기화 ──────────────────────────────────────
Write-Host ""
Write-Host "▶ Git 저장소 초기화 중..." -ForegroundColor Yellow

$initSh  = Join-Path $scriptDir "init.sh"
$initPs1 = Join-Path $scriptDir "init.ps1"
if (Test-Path $initSh)  { Remove-Item $initSh  -Force }
if (Test-Path $initPs1) { Remove-Item $initPs1 -Force }

Push-Location $scriptDir
try {
    if (Test-Path ".git") { Remove-Item ".git" -Recurse -Force }
    git init -q
    git add .
    git commit -q -m "chore: initialize project from Spring Boot Starter Kit"
    Write-Host "  Git 초기화 완료" -ForegroundColor Green
} finally {
    Pop-Location
}

# ── 완료 메시지 ─────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "======================================" -ForegroundColor Green
Write-Host "  ✅ 프로젝트 생성 완료!"              -ForegroundColor Green
Write-Host "     $scriptDir"                       -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
Write-Host ""
Write-Host "다음 단계:" -ForegroundColor Cyan
Write-Host "  1. .\gradlew bootRun            # H2 인메모리로 바로 실행 가능"
Write-Host "  2. http://localhost:8080/actuator/health 로 기동 확인"
Write-Host ""
Write-Host "MySQL/PostgreSQL 사용 시 환경 변수 설정:" -ForegroundColor Cyan
Write-Host "  `$env:DB_URL      = 'jdbc:mysql://localhost:3306/mydb'"
Write-Host "  `$env:DB_USERNAME = 'root'"
Write-Host "  `$env:DB_PASSWORD = 'yourpassword'"
Write-Host ""
