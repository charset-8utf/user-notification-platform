#!/usr/bin/env bash
# Smoke всех слоёв платформы после docker compose up.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

USER_HTTPS="${USER_HTTPS:-https://localhost:8443}"
NOTIF_HTTPS="${NOTIF_HTTPS:-https://localhost:8444}"
USER_MGMT="${USER_MGMT:-http://localhost:8081}"
NOTIF_MGMT="${NOTIF_MGMT:-http://localhost:8082}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"

pass() { echo "  OK  $*"; }
fail() { echo "  FAIL $*"; exit 1; }

echo "=== Platform smoke ==="

echo "[1] Management health"
curl -fsS "${USER_MGMT}/actuator/health" | grep -q '"status":"UP"' && pass "user-service mgmt" || fail "user-service mgmt"
curl -fsS "${NOTIF_MGMT}/actuator/health" | grep -q '"status":"UP"' && pass "notification-service mgmt" || fail "notification-service mgmt"

echo "[2] JWT login"
LOGIN=$(curl -fsSk -X POST "${USER_HTTPS}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}")
TOKEN=$(echo "${LOGIN}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -n "${TOKEN}" ] && pass "JWT accessToken" || fail "JWT login"

echo "[3] CRUD user (Kafka path)"
EMAIL="smoke-$(date +%s)@example.com"
CREATE=$(curl -fsSk -w "\n%{http_code}" -X POST "${USER_HTTPS}/api/users" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Smoke User\",\"email\":\"${EMAIL}\",\"age\":30}")
CODE=$(echo "${CREATE}" | tail -1)
[ "${CODE}" = "201" ] && pass "POST /api/users" || fail "POST /api/users code=${CODE}"

echo "[4] Mailpit after Kafka (до 60s)"
for i in $(seq 1 30); do
  count=$(curl -fsS http://localhost:8025/api/v1/messages | sed -n 's/.*"total":\([0-9]*\).*/\1/p')
  if [ "${count:-0}" -gt 0 ]; then
    pass "Mailpit total=${count}"
    break
  fi
  sleep 2
  [ "${i}" -eq 30 ] && fail "Mailpit empty after user create"
done

echo "[5] Service JWT → notification REST"
SVC_JWT=$(./gradlew -q :user-service:serviceJwtSmokeToken)
HTTP=$(curl -fkS -o /dev/null -w "%{http_code}" -X POST "${NOTIF_HTTPS}/api/notifications/email" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${SVC_JWT}" \
  -d '{"eventId":"990e8400-e29b-41d4-a716-446655440099","operation":"USER_CREATED","email":"rest-smoke@example.com"}')
[ "${HTTP}" = "204" ] && pass "notification REST 204" || fail "notification REST code=${HTTP}"

echo "[6] Prometheus metrics"
USER_PROM=$(curl -fsS "${USER_MGMT}/actuator/prometheus")
NOTIF_PROM=$(curl -fsS "${NOTIF_MGMT}/actuator/prometheus")
[[ "${USER_PROM}" == *http_server_requests_seconds* ]] && pass "user http metrics" || fail "user http metrics"
[[ "${USER_PROM}" == *app_outbox_pending* ]] && pass "user outbox gauge" || fail "user outbox gauge"
[[ "${NOTIF_PROM}" == *http_server_requests_seconds* ]] && pass "notification http metrics" || fail "notification http metrics"
[[ "${NOTIF_PROM}" == *app_notification_email_sent* ]] && pass "notification email metrics" || fail "notification email metrics"

echo "=== All smoke checks passed ==="
