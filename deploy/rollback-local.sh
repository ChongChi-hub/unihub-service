#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <previous-commit-sha-tag>"
  exit 1
fi

PREV_TAG="$1"
echo "Rolling back Unihub services to tag: $PREV_TAG"
IMAGE_TAG="$PREV_TAG" docker compose -f compose.prod.yml --profile core pull
IMAGE_TAG="$PREV_TAG" docker compose -f compose.prod.yml --profile core up -d
echo "Rollback to $PREV_TAG complete."
