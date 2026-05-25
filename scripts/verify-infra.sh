#!/usr/bin/env bash
# Verify local Postgres + Kafka after: docker compose up -d
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "==> Docker Compose status"
docker compose ps

echo ""
echo "==> PostgreSQL (host localhost:5433 → container :5432)"
docker compose exec -T postgres pg_isready -U orchestrai -d orchestrai
if command -v psql >/dev/null 2>&1; then
  PGPASSWORD=orchestrai psql -h localhost -p 5433 -U orchestrai -d orchestrai -c 'SELECT 1 AS ok;' 2>/dev/null || true
fi

echo ""
echo "==> Kafka (localhost:9092)"
docker compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null
echo "Kafka broker API versions OK"

echo ""
echo "==> Optional: list topics"
docker compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || true

echo ""
echo "All infrastructure checks passed."
