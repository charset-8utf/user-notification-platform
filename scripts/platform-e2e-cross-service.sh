#!/usr/bin/env bash
# Cross-service E2E: login → create user → Kafka → Mailpit notification
set -euo pipefail

GATEWAY="${GATEWAY_HTTP:-http://localhost}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"
UNIQUE_EMAIL="e2e-$(date +%s)@example.com"

pass() { echo "  OK  $*"; }
fail() { echo "  FAIL $*"; exit 1; }

echo "=== Cross-service E2E ==="

LOGIN=$(curl -fsS -X POST "${GATEWAY}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}")
TOKEN=$(echo "${LOGIN}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -n "${TOKEN}" ] && pass "login" || fail "login"

CREATE=$(curl -fsS -X POST "${GATEWAY}/api/users" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"E2E User\",\"email\":\"${UNIQUE_EMAIL}\",\"age\":25}")
echo "${CREATE}" | grep -q '"id"' && pass "user created" || fail "user created"

echo "Waiting for async notification (outbox → inbox → mail)..."
for _ in $(seq 1 30); do
  if curl -fsS "${GATEWAY}/api/notifications/logs?page=0&size=5" \
      -H "Authorization: Bearer ${TOKEN}" | grep -q "${UNIQUE_EMAIL}"; then
    pass "notification log for ${UNIQUE_EMAIL}"
    echo "=== Cross-service E2E passed ==="
    exit 0
  fi
  sleep 2
done
fail "notification not observed in logs"
