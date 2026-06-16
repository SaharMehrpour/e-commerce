#!/bin/bash
set -e

MODE=${1:-dev}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo "=================================="

if [ "$MODE" = "k8s" ]; then
  echo "Stopping K8S-like Docker Compose..."
  COMPOSE_FILE="docker-compose.k8s.yml"
else
  echo "Stopping DEV Docker Compose..."
  COMPOSE_FILE="docker-compose.dev.yml"
fi

echo "=================================="

docker compose -f $COMPOSE_FILE down

echo ""
echo "=================================="
echo "System Stopped Successfully ($MODE mode)"
echo "=================================="