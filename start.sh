#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

start_from_pid_file() {
  local pid_file="$1"
  local label="$2"

  if [ -f "$pid_file" ]; then
    local existing_pid
    existing_pid="$(cat "$pid_file")"

    if [ -n "$existing_pid" ] && kill -0 "$existing_pid" 2>/dev/null; then
      echo "$label is already running with PID $existing_pid"
      return 1
    fi

    rm -f "$pid_file"
  fi
}

start_from_pid_file "backend.pid" "Backend"
start_from_pid_file "frontend.pid" "Frontend"

echo "=================================="
echo "Starting infrastructure..."
echo "=================================="

cd main || exit

docker compose up -d

echo "=================================="
echo "Building backend..."
echo "=================================="

./mvnw clean install

echo "=================================="
echo "Starting Spring Boot backend..."
echo "=================================="

./mvnw spring-boot:run > backend.log 2>&1 &
BACKEND_PID=$!

echo "$BACKEND_PID" > ../backend.pid

cd ../frontend || exit

echo "=================================="
echo "Starting React frontend..."
echo "=================================="

if [ ! -d "node_modules" ]; then
  npm install
fi

npm run dev > frontend.log 2>&1 &
FRONTEND_PID=$!

echo "$FRONTEND_PID" > ../frontend.pid

cd ..

echo ""
echo "=================================="
echo "Everything started!"
echo "=================================="
echo "Backend PID : $BACKEND_PID"
echo "Frontend PID: $FRONTEND_PID"
echo ""
echo "Backend log : main/backend.log"
echo "Frontend log: frontend/frontend.log"
echo "=================================="
