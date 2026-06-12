#!/usr/bin/env bash
# E2E compensation (natural path):
# create user → outbox → Kafka → inbox relay → SMTP down → compensation → user deleted (saga rollback)
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=e2e-lib.sh
source "${ROOT}/scripts/e2e-lib.sh"

GATEWAY="${GATEWAY_HTTP:-http://localhost}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"
MAILPIT_CONTAINER="${MAILPIT_CONTAINER:-unp-mailpit}"
MAILPIT_MODE="${MAILPIT_MODE:-docker}"
K8S_NAMESPACE="${K8S_NAMESPACE:-platform}"
UNIQUE_EMAIL="comp-e2e-$(date +%s)@example.com"

pass() { echo "  OK  $*"; }
fail() { echo "  FAIL $*"; exit 1; }

restore_mailpit() {
  if [ "${MAILPIT_MODE}" = "k8s" ]; then
    kubectl -n "${K8S_NAMESPACE}" scale deployment/mailpit --replicas=1 >/dev/null 2>&1 || true
    return
  fi
  if docker ps -a --format '{{.Names}}' | grep -qx "${MAILPIT_CONTAINER}"; then
    docker start "${MAILPIT_CONTAINER}" >/dev/null 2>&1 || true
  fi
}

trap restore_mailpit EXIT

echo "=== Compensation E2E (natural path) ==="

echo "Stopping mailpit to force inbox delivery failure..."
if [ "${MAILPIT_MODE}" = "k8s" ]; then
  kubectl -n "${K8S_NAMESPACE}" scale deployment/mailpit --replicas=0 >/dev/null
else
  if ! docker ps --format '{{.Names}}' | grep -qx "${MAILPIT_CONTAINER}"; then
    fail "mailpit container ${MAILPIT_CONTAINER} not running"
  fi
  docker stop "${MAILPIT_CONTAINER}" >/dev/null
fi
pass "mailpit stopped"
sleep 3

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

POLL_MAX="${E2E_COMPENSATION_POLL_MAX:-$(e2e_poll_max)}"
POLL_SLEEP="$(e2e_poll_sleep)"
echo "Waiting for outbox → inbox → compensation → user rollback (404), max ${POLL_MAX} attempts..."
for i in $(seq 1 "${POLL_MAX}"); do
  HTTP_CODE=$(curl -sS -o /dev/null -w "%{http_code}" \
    "${GATEWAY}/api/users/${USER_ID}" -H "Authorization: Bearer ${TOKEN}" || echo "000")
  if [ "${HTTP_CODE}" = "404" ]; then
    pass "user ${USER_ID} rolled back (404 — saga compensation)"
    echo "=== Compensation E2E passed ==="
    exit 0
  fi
  [ $((i % 10)) -eq 0 ] && echo "  ... still waiting (${i}/${POLL_MAX}), last HTTP ${HTTP_CODE}"
  sleep "${POLL_SLEEP}"
done
fail "user ${USER_ID} still exists after compensation (expected 404)"
