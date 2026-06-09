#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

export APP_SERVICE_JWT_SECRET="${APP_SERVICE_JWT_SECRET:-e2e-smoke-service-jwt-secret-minimum-32b}"
export APP_SEED_ADMIN_PASSWORD="${APP_SEED_ADMIN_PASSWORD:-admin123}"
export APP_SEED_USER_PASSWORD="${APP_SEED_USER_PASSWORD:-user123}"

ci_health_wait_max() {
  if [ -n "${GITHUB_ACTIONS:-}${CI:-}${GITLAB_CI:-}" ]; then
    echo 120
  else
    echo 60
  fi
}

wait_container_healthy() {
  local name="$1"
  local max="${2:-$(ci_health_wait_max)}"
  local i
  for i in $(seq 1 "${max}"); do
    local status
    status="$(docker inspect --format='{{.State.Health.Status}}' "${name}" 2>/dev/null || echo none)"
    echo "[${i}/${max}] ${name}: ${status}"
    if [ "${status}" = "healthy" ]; then
      return 0
    fi
    sleep 5
  done
  docker compose logs "${name#unp-}" 2>/dev/null | tail -100 || true
  return 1
}

wait_http() {
  local url="$1"
  local max="${2:-60}"
  local i
  for i in $(seq 1 "${max}"); do
    if curl -fsS "${url}" >/dev/null 2>&1; then
      return 0
    fi
    sleep 5
  done
  return 1
}

cmd_fast() {
  ./gradlew check
}

cmd_build_e2e() {
  ./gradlew :platform-commons:jar :user-service:bootJar :notification-service:bootJar \
    :config-server:bootJar :api-gateway:bootJar :web-bff:bootJar -x test
}

cmd_e2e_up() {
  export NOTIFICATION_SERVICE_PROFILES="${NOTIFICATION_SERVICE_PROFILES:-rest,kafka,redis,management,docker}"
  export USER_SERVICE_PROFILES="${USER_SERVICE_PROFILES:-kafka,redis,jwt,management,docker}"
  cmd_build_e2e
  docker compose up -d --build
  wait_container_healthy unp-user-service 60
  wait_container_healthy unp-notification-service 60
}

cmd_e2e() {
  cmd_e2e_up
  ./scripts/platform-smoke.sh
}

cmd_e2e_cloud_up() {
  export USER_SERVICE_PROFILES="${USER_SERVICE_PROFILES:-kafka,redis,jwt,management,docker,cloud}"
  export NOTIFICATION_SERVICE_PROFILES="${NOTIFICATION_SERVICE_PROFILES:-rest,kafka,redis,management,docker,cloud}"
  cmd_build_e2e
  docker compose --profile cloud up -d --build
  wait_container_healthy unp-user-service "$(ci_health_wait_max)"
  wait_container_healthy unp-notification-service "$(ci_health_wait_max)"
  wait_container_healthy unp-nginx "$(ci_health_wait_max)"
  wait_http "http://localhost/actuator/health" "$(ci_health_wait_max)"
}

cmd_e2e_cloud() {
  cmd_e2e_cloud_up
  GATEWAY_HTTP="${GATEWAY_HTTP:-http://localhost}" BFF_HTTP="${BFF_HTTP:-http://localhost}" \
    ./scripts/platform-smoke-cloud.sh
}

cmd_e2e_cross() {
  cmd_e2e_cloud_up
  GATEWAY_HTTP="${GATEWAY_HTTP:-http://localhost}" \
    ./scripts/platform-e2e-cross-service.sh
}

cmd_e2e_compensation() {
  cmd_e2e_cloud_up
  GATEWAY_HTTP="${GATEWAY_HTTP:-http://localhost}" \
    ./scripts/platform-e2e-compensation.sh
}

cmd_e2e_oidc_up() {
  export USER_SERVICE_PROFILES="${USER_SERVICE_PROFILES:-kafka,redis,jwt,management,docker,cloud}"
  export NOTIFICATION_SERVICE_PROFILES="${NOTIFICATION_SERVICE_PROFILES:-rest,kafka,redis,management,docker,cloud}"
  export APP_JWT_ISSUER_URI="${APP_JWT_ISSUER_URI:-http://host.docker.internal:8180/realms/platform}"
  cmd_build_e2e
  docker compose --profile cloud --profile auth up -d --build
  wait_container_healthy unp-user-service 60
  wait_container_healthy unp-notification-service 60
  wait_container_healthy unp-nginx 30
  wait_http "http://localhost:8180/realms/platform" 90
  wait_http "http://localhost/actuator/health" 60
}

cmd_e2e_oidc() {
  cmd_e2e_oidc_up
  GATEWAY_HTTP="${GATEWAY_HTTP:-http://localhost}" \
    KEYCLOAK_HTTP="${KEYCLOAK_HTTP:-http://localhost:8180}" \
    ./scripts/platform-smoke-oidc.sh
}

cmd_observability_up() {
  export USER_SERVICE_PROFILES="${USER_SERVICE_PROFILES:-kafka,redis,jwt,management,docker,cloud}"
  export NOTIFICATION_SERVICE_PROFILES="${NOTIFICATION_SERVICE_PROFILES:-rest,kafka,redis,management,docker,cloud}"
  cmd_build_e2e
  docker compose --profile cloud --profile observability up -d --build
  wait_container_healthy unp-user-service 60
  wait_container_healthy unp-notification-service 60
  wait_http "http://localhost:9091/-/ready" 60
}

cmd_e2e_down() {
  docker compose --profile cloud --profile auth --profile observability down -v --remove-orphans 2>/dev/null || \
    docker compose --profile cloud --profile observability down -v --remove-orphans 2>/dev/null || \
    docker compose --profile cloud down -v --remove-orphans 2>/dev/null || \
    docker compose down -v --remove-orphans
}

cmd_security() {
  local images=(
    user-service:latest
    notification-service:latest
    config-server:latest
    api-gateway:latest
    web-bff:latest
  )
  if ! command -v trivy >/dev/null 2>&1; then
    echo "trivy not installed, skipping image scan"
  else
    docker compose build
    for img in "${images[@]}"; do
      trivy image --severity HIGH,CRITICAL --exit-code 1 "${img}"
    done
  fi
  if command -v gitleaks >/dev/null 2>&1; then
    gitleaks detect --source . --verbose
  else
    echo "gitleaks not installed, skipping secret scan"
  fi
}

cmd_e2e_cloud_suite() {
  cmd_e2e_cloud_up
  local gateway="${GATEWAY_HTTP:-http://localhost}"
  local bff="${BFF_HTTP:-http://localhost}"
  GATEWAY_HTTP="${gateway}" BFF_HTTP="${bff}" ./scripts/platform-smoke-cloud.sh
  GATEWAY_HTTP="${gateway}" ./scripts/platform-e2e-cross-service.sh
  GATEWAY_HTTP="${gateway}" ./scripts/platform-e2e-compensation.sh
}

cmd_full() {
  cmd_fast
  cmd_e2e
  cmd_e2e_cloud_suite
  cmd_e2e_down
  cmd_security
}

usage() {
  cat <<EOF
Usage: $(basename "$0") <command>

Commands:
  fast              ./gradlew check (unit + integration)
  build-e2e         Build JARs for Docker (skip tests)
  e2e-up            Build, compose up, wait healthy
  e2e               e2e-up + platform-smoke.sh
  e2e-cloud-up      Compose --profile cloud, wait healthy
  e2e-cloud         e2e-cloud-up + platform-smoke-cloud.sh
  e2e-cross         cloud stack + cross-service E2E
  e2e-compensation  cloud stack + compensation E2E
  e2e-oidc-up       cloud + auth profile, wait Keycloak
  e2e-oidc          e2e-oidc-up + platform-smoke-oidc.sh
  e2e-cloud-suite   smoke-cloud + e2e-cross + e2e-compensation
  observability-up  compose cloud + observability, wait prometheus
  e2e-down          Stop compose and remove volumes
  security          Trivy (fail on HIGH/CRITICAL) + gitleaks
  full              fast + e2e + cloud suite + e2e-down + security
EOF
}

main() {
  local cmd="${1:-}"
  case "${cmd}" in
    fast) cmd_fast ;;
    build-e2e) cmd_build_e2e ;;
    e2e-up) cmd_e2e_up ;;
    e2e) cmd_e2e ;;
    e2e-cloud-up) cmd_e2e_cloud_up ;;
    e2e-cloud) cmd_e2e_cloud ;;
    e2e-cross) cmd_e2e_cross ;;
    e2e-compensation) cmd_e2e_compensation ;;
    e2e-oidc-up) cmd_e2e_oidc_up ;;
    e2e-oidc) cmd_e2e_oidc ;;
    e2e-cloud-suite) cmd_e2e_cloud_suite ;;
    observability-up) cmd_observability_up ;;
    e2e-down) cmd_e2e_down ;;
    security) cmd_security ;;
    full) cmd_full ;;
    *) usage; exit 1 ;;
  esac
}

main "$@"
