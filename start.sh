#!/bin/bash
set -e

MODE=${1:-dev}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo "=================================="

if [ "$MODE" = "k8s" ]; then
  echo "Starting K8S-like Docker Compose..."
  COMPOSE_FILE="docker-compose.k8s.yml"
else
  echo "Starting DEV Docker Compose..."
  COMPOSE_FILE="docker-compose.dev.yml"
fi

echo "=================================="

docker compose -f $COMPOSE_FILE up --build -d

echo ""
echo "=================================="
echo "System Started Successfully ($MODE mode)"
echo "=================================="

if [ "$MODE" = "k8s" ]; then
  echo "Frontend (K8S mode): http://localhost:5173"
  echo "Gateway (via frontend): http://localhost:5173/api"
  echo "Direct Gateway (if exposed): http://localhost:8080"
else
  echo "Frontend: http://localhost:5173"
  echo "Backend (Order/Inventory via Gateway or direct): http://localhost:8080"
  echo "Prometheus: http://localhost:9090"
fi

echo "=================================="