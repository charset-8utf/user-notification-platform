#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

export APP_SERVICE_JWT_SECRET="${APP_SERVICE_JWT_SECRET:-e2e-smoke-service-jwt-secret-minimum-32b}"
export APP_SEED_ADMIN_PASSWORD="${APP_SEED_ADMIN_PASSWORD:-admin123}"
export APP_SEED_USER_PASSWORD="${APP_SEED_USER_PASSWORD:-user123}"

wait_container_healthy() {
  local name="$1"
  local max="${2:-60}"
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
  wait_container_healthy unp-user-service 60
  wait_container_healthy unp-notification-service 60
  wait_http "http://localhost:8080/actuator/health" 60
}

cmd_e2e_cloud() {
  cmd_e2e_cloud_up
  ./scripts/platform-smoke-cloud.sh
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

cmd_full() {
  cmd_fast
  cmd_e2e
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
  observability-up  compose cloud + observability, wait prometheus
  e2e-down          Stop compose and remove volumes
  security          Trivy (fail on HIGH/CRITICAL) + gitleaks
  full              fast + e2e + e2e-down + security
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
    observability-up) cmd_observability_up ;;
    e2e-down) cmd_e2e_down ;;
    security) cmd_security ;;
    full) cmd_full ;;
    *) usage; exit 1 ;;
  esac
}

main "$@"
