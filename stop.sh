#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo "=================================="
echo "Stopping application..."
echo "=================================="

docker compose down

echo "=================================="
echo "Application stopped!"
echo "=================================="