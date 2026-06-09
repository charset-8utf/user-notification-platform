#!/usr/bin/env bash
# OIDC smoke: Keycloak token → gateway JWKS validation → protected API
# Prerequisites:
#   docker compose --profile cloud --profile auth up -d
#   APP_JWT_ISSUER_URI=http://host.docker.internal:8180/realms/platform  (gateway must reach JWKS)
set -euo pipefail

GATEWAY="${GATEWAY_HTTP:-http://localhost}"
KEYCLOAK="${KEYCLOAK_HTTP:-http://localhost:8180}"
REALM="${KEYCLOAK_REALM:-platform}"
CLIENT_ID="${OIDC_CLIENT_ID:-api-gateway}"
OIDC_USER="${OIDC_USER:-admin}"
OIDC_PASS="${OIDC_PASS:-admin123}"

pass() { echo "  OK  $*"; }
fail() { echo "  FAIL $*"; exit 1; }

echo "=== OIDC smoke (Keycloak → gateway) ==="

curl -fsS "${KEYCLOAK}/realms/${REALM}" | grep -q '"realm"' && pass "keycloak realm" || fail "keycloak unreachable"

TOKEN_RESPONSE=$(curl -sS -X POST "${KEYCLOAK}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "username=${OIDC_USER}" \
  -d "password=${OIDC_PASS}") || fail "OIDC token request failed"
ACCESS_TOKEN=$(echo "${TOKEN_RESPONSE}" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
if [ -n "${ACCESS_TOKEN}" ]; then
  pass "OIDC token from Keycloak"
else
  echo "  token response: ${TOKEN_RESPONSE}"
  fail "OIDC token"
fi

curl -fsS "${GATEWAY}/api/users?page=0&size=1" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" | head -c 120 >/dev/null \
  && pass "GET /api/users with OIDC Bearer" \
  || fail "gateway rejected OIDC token (set APP_JWT_ISSUER_URI on api-gateway and restart)"

echo "=== OIDC smoke passed ==="
