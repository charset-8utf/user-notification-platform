#!/usr/bin/env bash
# Smoke через API Gateway (профиль cloud). Запускать после:
#   docker compose --profile cloud up -d
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

GATEWAY="${GATEWAY_HTTP:-http://localhost}"
BFF="${BFF_HTTP:-http://localhost}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"

pass() { echo "  OK  $*"; }
fail() { echo "  FAIL $*"; exit 1; }

echo "=== Platform smoke (cloud / gateway) ==="

echo "[1] Gateway health"
curl -fsS "${GATEWAY}/actuator/health" | grep -q '"status":"UP"' && pass "api-gateway" || fail "api-gateway"

echo "[2] JWT login via gateway"
LOGIN=$(curl -fsS -X POST "${GATEWAY}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}")
TOKEN=$(echo "${LOGIN}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -n "${TOKEN}" ] && pass "JWT via gateway" || fail "JWT login"

echo "[3] GET /api/users via gateway"
curl -fsS "${GATEWAY}/api/users?page=0&size=1" \
  -H "Authorization: Bearer ${TOKEN}" | head -c 120 >/dev/null && pass "users list" || fail "users list"

echo "[4] BFF /bff/me"
curl -fsS "${BFF}/bff/me" -H "Authorization: Bearer ${TOKEN}" | grep -q '"user"' && pass "bff/me" || fail "bff/me"

echo "=== All cloud smoke checks passed ==="
