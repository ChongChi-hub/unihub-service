#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT_DIR/.pids"
cd "$ROOT_DIR"

echo "Đang tắt các service backend Unihub..."
for name in api-gateway file-service learning-service academic-service identity-service; do
  pid_file="$PID_DIR/$name.pid"
  if [[ -f "$pid_file" ]]; then
    pid="$(cat "$pid_file")"
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      echo "  - Đã tắt $name (PID $pid)"
    else
      echo "  - $name đã tắt từ trước"
    fi
    rm -f "$pid_file"
  else
    echo "  - $name chưa chạy"
  fi
done

echo "Đang tắt hạ tầng Docker..."
docker compose --profile infra down

echo "Đã tắt toàn bộ hệ thống."
