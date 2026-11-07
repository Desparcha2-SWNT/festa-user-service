#!/usr/bin/env bash
# Uso: ./scripts/smoke.sh http://localhost:8082
set -euo pipefail
base="${1:-http://localhost:8082}"

echo "== Health =="
curl -fsS "${base}/health" | jq .

echo "== Prometheus (primeras métricas festa_) =="
curl -fsS "${base}/metrics" | grep '^festa_' | head -n 20 || true

echo "== OpenAPI info =="
curl -fsS "${base}/v3/api-docs" | jq -r '.info.title' || true

echo "OK"