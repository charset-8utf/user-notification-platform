#!/usr/bin/env bash
# Refresh third-party image digests in docker-compose.yml.
# Usage: ./scripts/docker/pin-digests.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_FILE="${ROOT}/docker-compose.yml"

declare -A IMAGES=(
  ['postgres:17-alpine']='postgres'
  ['mongo:7']='mongo'
  ['redis:7-alpine']='redis'
  ['axllent/mailpit:latest']='mailpit'
  ['confluentinc/cp-kafka:7.4.0']='kafka'
  ['confluentinc/cp-schema-registry:7.4.0']='schema-registry'
  ['nginx:1.27-alpine']='nginx'
  ['quay.io/keycloak/keycloak:26.0']='keycloak'
  ['openzipkin/zipkin:3.4']='zipkin'
  ['prom/alertmanager:v0.28.0']='alertmanager'
  ['prom/prometheus:v3.0.1']='prometheus'
  ['grafana/loki:3.2.1']='loki'
  ['grafana/promtail:3.2.1']='promtail'
  ['grafana/grafana:11.4.0']='grafana'
)

tmp="$(mktemp)"
cp "${COMPOSE_FILE}" "${tmp}"

for ref in "${!IMAGES[@]}"; do
  digest="$(docker buildx imagetools inspect "${ref}" 2>/dev/null | awk '/Digest:/ {print $2; exit}')"
  if [[ -z "${digest}" ]]; then
    echo "Failed to resolve digest for ${ref}" >&2
    exit 1
  fi
  base="${ref%%@*}"
  pinned="${base}@${digest}"
  echo "${IMAGES[$ref]}: ${pinned}"
  sed -i '' "s|image: ${base}@sha256:[a-f0-9]*|image: ${pinned}|g" "${tmp}"
  sed -i '' "s|image: ${base}$|image: ${pinned}|g" "${tmp}"
done

mv "${tmp}" "${COMPOSE_FILE}"
echo "Updated ${COMPOSE_FILE}"
