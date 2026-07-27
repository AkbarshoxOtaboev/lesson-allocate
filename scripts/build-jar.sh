#!/usr/bin/env bash
# Frontend + Backend → bitta fat JAR
# Natija: backend/target/lesson-allocate-1.0.0.jar
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> Frontend build (production)..."
cd "$ROOT/frontend"
if [[ ! -d node_modules ]]; then
  npm install
fi
npm run build

echo "==> Backend package (Maven)..."
cd "$ROOT/backend"
if [[ -x "$ROOT/.tools/apache-maven-3.9.9/bin/mvn" ]]; then
  "$ROOT/.tools/apache-maven-3.9.9/bin/mvn" -DskipTests package
elif command -v mvn >/dev/null 2>&1; then
  mvn -DskipTests package
else
  echo "Maven topilmadi" >&2
  exit 1
fi

JAR="$ROOT/backend/target/lesson-allocate-1.0.0.jar"
echo ""
echo "OK: $JAR"
echo "Ishga tushirish: java -jar \"$JAR\" --spring.profiles.active=prod"
