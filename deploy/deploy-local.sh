#!/usr/bin/env bash
set -euo pipefail

TAG="${1:-latest}"
export IMAGE_TAG="$TAG"

echo "Pulling production-like images (tag: $IMAGE_TAG)..."
docker compose -f compose.prod.yml --profile core pull

echo "Starting Unihub services..."
docker compose -f compose.prod.yml --profile core up -d

echo "Waiting for services to become healthy..."
for port in 8081 8082 8080; do
  until curl -sf "http://localhost:${port}/actuator/health" | grep -q '"UP"'; do
    sleep 2
  done
  echo "Service on port ${port} is UP"
done

echo "Deploy complete. Gateway: http://api.unihub.localhost (or http://localhost:8080)"
