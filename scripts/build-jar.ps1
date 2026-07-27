# Frontend + Backend → bitta fat JAR
# Natija: backend/target/lesson-allocate-1.0.0.jar

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

Write-Host "==> Frontend build (production)..." -ForegroundColor Cyan
Push-Location (Join-Path $Root "frontend")
if (-not (Test-Path "node_modules")) {
    npm install
}
npm run build
if ($LASTEXITCODE -ne 0) { throw "Frontend build failed" }
Pop-Location

Write-Host "==> Backend package (Maven)..." -ForegroundColor Cyan
$Mvn = Join-Path $Root ".tools\apache-maven-3.9.9\bin\mvn.cmd"
if (-not (Test-Path $Mvn)) {
    $Mvn = "mvn"
}
Push-Location (Join-Path $Root "backend")
& $Mvn -DskipTests package
if ($LASTEXITCODE -ne 0) { throw "Maven package failed" }
Pop-Location

$Jar = Join-Path $Root "backend\target\lesson-allocate-1.0.0.jar"
Write-Host ""
Write-Host "OK: $Jar" -ForegroundColor Green
Write-Host "Ishga tushirish: java -jar `"$Jar`" --spring.profiles.active=prod"
