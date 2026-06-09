#!/usr/bin/env bash
set -euo pipefail

GATEWAY="${GATEWAY_HTTP:-http://localhost}"
BFF="${BFF_HTTP:-http://localhost}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"

pass() { echo "  OK  $*"; }
fail() { echo "  FAIL $*"; exit 1; }

echo "=== K8s smoke ==="

curl -fsS "${GATEWAY}/actuator/health" | grep -q '"status":"UP"' && pass "api-gateway" || fail "api-gateway"

LOGIN=$(curl -fsS -X POST "${GATEWAY}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}")
TOKEN=$(echo "${LOGIN}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -n "${TOKEN}" ] && pass "JWT login" || fail "JWT login"

curl -fsS "${GATEWAY}/api/users?page=0&size=1" \
  -H "Authorization: Bearer ${TOKEN}" | head -c 120 >/dev/null && pass "users via gateway" || fail "users"

curl -fsS "${BFF}/bff/me" -H "Authorization: Bearer ${TOKEN}" | grep -q '"user"' && pass "bff/me" || fail "bff/me"

echo "=== K8s smoke passed ==="
