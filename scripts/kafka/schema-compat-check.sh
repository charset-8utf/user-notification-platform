#!/usr/bin/env bash
# Verifies Avro schema backward compatibility via Schema Registry API.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SCHEMA_FILE="${ROOT}/schemas/avro/notification-email-message.avsc"
REGISTRY="${SCHEMA_REGISTRY_URL:-http://localhost:8085}"
SUBJECT="${SCHEMA_SUBJECT:-user-notifications-value}"

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required for schema-compat-check.sh"
  exit 1
fi

SCHEMA_JSON="$(jq -c . "${SCHEMA_FILE}")"
ESCAPED_SCHEMA="$(echo "${SCHEMA_JSON}" | jq -Rs .)"

echo "=== Schema Registry compatibility check ==="
echo "Registry: ${REGISTRY}"
echo "Subject:  ${SUBJECT}"

HTTP_CODE="$(curl -sS -o /tmp/schema-compat-response.json -w "%{http_code}" \
  -X POST "${REGISTRY}/compatibility/subjects/${SUBJECT}/versions/latest" \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d "{\"schema\": ${ESCAPED_SCHEMA}}")"

if [ "${HTTP_CODE}" = "404" ]; then
  echo "No existing schema — registering initial version..."
  curl -fsS -X POST "${REGISTRY}/subjects/${SUBJECT}/versions" \
    -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    -d "{\"schema\": ${ESCAPED_SCHEMA}}" >/dev/null
  echo "OK  initial schema registered"
  exit 0
fi

if [ "${HTTP_CODE}" != "200" ]; then
  echo "FAIL compatibility check HTTP ${HTTP_CODE}"
  cat /tmp/schema-compat-response.json
  exit 1
fi

RESULT="$(jq -r '.is_compatible // false' /tmp/schema-compat-response.json)"
if [ "${RESULT}" = "true" ]; then
  echo "OK  schema is backward compatible"
  exit 0
fi

echo "FAIL schema is NOT backward compatible"
cat /tmp/schema-compat-response.json
exit 1
