#!/usr/bin/env bash
# Нагрузочный тест: создание пользователей → outbox → Kafka → notification-service → Mailpit.
#
# Использование:
#   ./scripts/kafka/http_load_test.sh [число_запросов] [параллелизм]
#   ./scripts/kafka/http_load_test.sh 20 5
#
# При 429 увеличьте лимит: APP_RATE_LIMIT_MAX_REQUESTS=500 docker compose up -d user-service

set -euo pipefail

REQUESTS="${1:-100}"
CONCURRENCY="${2:-10}"
BASE_URL="${BASE_URL:-https://localhost:8443}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"
MAILPIT_URL="${MAILPIT_URL:-http://localhost:8025}"
USE_BASIC="${USE_BASIC:-false}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"

ACCESS_TOKEN=""

obtain_jwt() {
  local resp
  resp=$(curl -fsSk -X POST "${BASE_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}")
  ACCESS_TOKEN=$(echo "${resp}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
  if [ -z "${ACCESS_TOKEN}" ]; then
    echo "Не удалось получить accessToken (проверьте seed-пароли и профиль jwt/local)" >&2
    exit 1
  fi
}

create_user() {
  local n="$1"
  local email="loadtest-${n}-$(date +%s%N)@example.com"
  if [ "${USE_BASIC}" = "true" ]; then
    curl -fsSk -u "${ADMIN_USER}:${ADMIN_PASS}" \
      -H "Content-Type: application/json" \
      -X POST "${BASE_URL}/api/users" \
      -d "{\"name\":\"Load Test ${n}\",\"email\":\"${email}\",\"age\":25}" \
      -o /dev/null -w "%{http_code}\n" | grep -q '^201$'
  else
    curl -fsSk \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${ACCESS_TOKEN}" \
      -X POST "${BASE_URL}/api/users" \
      -d "{\"name\":\"Load Test ${n}\",\"email\":\"${email}\",\"age\":25}" \
      -o /dev/null -w "%{http_code}\n" | grep -q '^201$'
  fi
}

export -f create_user
export BASE_URL ADMIN_USER ADMIN_PASS ACCESS_TOKEN USE_BASIC

echo "=== Kafka load test: ${REQUESTS} POST /api/users, concurrency=${CONCURRENCY} ==="
echo "user-service: ${BASE_URL}"

if [ "${USE_BASIC}" != "true" ]; then
  echo "Получение JWT..."
  obtain_jwt
fi

START=$(date +%s)
seq 1 "${REQUESTS}" | xargs -P "${CONCURRENCY}" -I {} bash -c 'create_user "$@"' _ {}
END=$(date +%s)
ELAPSED=$((END - START))

echo "Запросы отправлены за ${ELAPSED}s. Ожидание outbox relay и Mailpit (до 90s)..."
for i in $(seq 1 45); do
  total=$(curl -fsS "${MAILPIT_URL}/api/v1/messages" 2>/dev/null | sed -n 's/.*"total":\([0-9]*\).*/\1/p' || echo 0)
  echo "  [${i}] Mailpit total=${total:-0} (ожидаем >= ${REQUESTS})"
  if [ "${total:-0}" -ge "${REQUESTS}" ]; then
    echo "OK: получено не меньше ${REQUESTS} писем."
    exit 0
  fi
  sleep 2
done

echo "WARN: за 90s в Mailpit меньше ${REQUESTS} писем. Проверьте логи:"
echo "  docker compose -f ${ROOT_DIR}/docker-compose.yml logs user-service notification-service kafka --tail 80"
exit 1
