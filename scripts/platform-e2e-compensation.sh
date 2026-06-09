#!/usr/bin/env bash
# E2E compensation: create user → DLT event → compensation topic → user FAILED
set -euo pipefail

GATEWAY="${GATEWAY_HTTP:-http://localhost}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"
KAFKA_CONTAINER="${KAFKA_CONTAINER:-unp-kafka}"
UNIQUE_EMAIL="comp-e2e-$(date +%s)@example.com"
EVENT_ID="$(uuidgen | tr '[:upper:]' '[:lower:]')"

pass() { echo "  OK  $*"; }
fail() { echo "  FAIL $*"; exit 1; }

echo "=== Compensation E2E ==="

LOGIN=$(curl -fsS -X POST "${GATEWAY}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}")
TOKEN=$(echo "${LOGIN}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -n "${TOKEN}" ] && pass "login" || fail "login"

CREATE=$(curl -fsS -X POST "${GATEWAY}/api/users" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Comp E2E\",\"email\":\"${UNIQUE_EMAIL}\",\"age\":25}")
USER_ID=$(echo "${CREATE}" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
[ -n "${USER_ID}" ] && pass "user created id=${USER_ID}" || fail "user created"

DLT_PAYLOAD="{\"eventId\":\"${EVENT_ID}\",\"operation\":\"USER_CREATED\",\"email\":\"${UNIQUE_EMAIL}\"}"
echo "${EVENT_ID}:${DLT_PAYLOAD}" | docker exec -i "${KAFKA_CONTAINER}" \
  kafka-console-producer \
  --bootstrap-server kafka:29092 \
  --topic user-notifications.DLT \
  --property "parse.key=true" \
  --property "key.separator=:" \
  >/dev/null
pass "DLT event published"

echo "Waiting for compensation → user notificationDeliveryStatus=FAILED..."
for _ in $(seq 1 30); do
  USER=$(curl -fsS "${GATEWAY}/api/users/${USER_ID}" -H "Authorization: Bearer ${TOKEN}")
  if echo "${USER}" | grep -q '"notificationDeliveryStatus":"FAILED"'; then
    pass "user ${USER_ID} marked FAILED"
    echo "=== Compensation E2E passed ==="
    exit 0
  fi
  sleep 2
done
fail "notificationDeliveryStatus not FAILED for user ${USER_ID}"
