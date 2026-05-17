#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

stop_from_pid_file() {
  local pid_file="$1"
  local label="$2"

  if [ ! -f "$pid_file" ]; then
    echo "$label PID file not found; skipping."
    return
  fi

  local pid
  pid="$(cat "$pid_file")"

  if [ -z "$pid" ]; then
    echo "$label PID file is empty; removing it."
    rm -f "$pid_file"
    return
  fi

  if kill -0 "$pid" 2>/dev/null; then
    echo "Stopping $label with PID $pid..."
    kill "$pid"

    for _ in {1..10}; do
      if ! kill -0 "$pid" 2>/dev/null; then
        break
      fi
      sleep 1
    done

    if kill -0 "$pid" 2>/dev/null; then
      echo "$label did not stop cleanly; forcing it..."
      kill -9 "$pid"
    fi
  else
    echo "$label is not running; removing stale PID file."
  fi

  rm -f "$pid_file"
}

echo "=================================="
echo "Stopping application..."
echo "=================================="

stop_from_pid_file "frontend.pid" "Frontend"
stop_from_pid_file "backend.pid" "Backend"

echo "=================================="
echo "Stopping infrastructure..."
echo "=================================="

cd main || exit
docker compose stop

echo "=================================="
echo "Everything stopped!"
echo "=================================="
