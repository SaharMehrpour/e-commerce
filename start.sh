#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo "=================================="
echo "Starting application..."
echo "=================================="

docker compose up --build -d

echo ""
echo "=================================="
echo "Application started!"
echo "=================================="
echo "Frontend: http://localhost:5173"
echo "Backend : http://localhost:8080"
echo "=================================="