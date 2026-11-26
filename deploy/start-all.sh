#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT_DIR/.pids"
LOG_DIR="$ROOT_DIR/logs"

mkdir -p "$PID_DIR" "$LOG_DIR"
cd "$ROOT_DIR"

echo "[1/4] Đang khởi động hạ tầng Docker (Postgres, Redis, RabbitMQ, MinIO, Mailpit)..."
docker compose --profile infra up -d

echo "[2/4] Đang build các service..."
./gradlew :services:identity-service:bootJar :services:academic-service:bootJar :services:api-gateway:bootJar --no-daemon -x test

start_service() {
  local name="$1"
  local port="$2"
  local jar
  jar=$(find "$ROOT_DIR/services/$name/build/libs" -maxdepth 1 -name "*.jar" ! -name "*-plain.jar" | head -n 1)

  if [[ -f "$PID_DIR/$name.pid" ]] && kill -0 "$(cat "$PID_DIR/$name.pid")" 2>/dev/null; then
    echo "  - $name đang chạy sẵn (PID $(cat "$PID_DIR/$name.pid"))"
    return 0
  fi

  nohup java -XX:MaxRAMPercentage=50.0 -jar "$jar" > "$LOG_DIR/$name.log" 2>&1 &
  echo $! > "$PID_DIR/$name.pid"
  echo "  - Đã bật $name (PID $!, log: logs/$name.log)"

  echo -n "    Đang chờ $name sẵn sàng trên cổng :$port"
  for _ in $(seq 1 40); do
    if curl -sf "http://localhost:${port}/actuator/health" | grep -q '"UP"'; then
      echo " -> ĐÃ SẴN SÀNG"
      return 0
    fi
    echo -n "."
    sleep 1
  done
  echo " -> HẾT THỜI GIAN CHỜ (kiểm tra logs/$name.log)"
  return 1
}

echo "[3/4] Đang khởi chạy các service backend..."
start_service "identity-service" 8081
start_service "academic-service" 8082
start_service "api-gateway" 8080

echo "[4/4] Toàn bộ hệ thống Unihub backend đang chạy!"
echo "  - API Gateway:      http://localhost:8080"
echo "  - Swagger UI:       http://localhost:8080/swagger-ui.html"
echo "  - Identity Service: http://localhost:8081"
echo "  - Academic Service: http://localhost:8082"
