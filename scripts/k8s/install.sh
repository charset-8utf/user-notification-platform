#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RELEASE="${RELEASE:-platform}"
NAMESPACE="${NAMESPACE:-platform}"
LOAD_IMAGES=false
KIND_CLUSTER="${KIND_CLUSTER:-unp}"

usage() {
  cat <<EOF
Usage: $(basename "$0") [--load-images] [helm upgrade extra args...]

  --load-images   docker compose build + kind load (local dev)
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --load-images) LOAD_IMAGES=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) break ;;
  esac
done

cd "${ROOT}"

if [ "${LOAD_IMAGES}" = true ]; then
  docker compose build user-service notification-service config-server api-gateway web-bff
  for img in user-service notification-service config-server api-gateway web-bff; do
    kind load docker-image "${img}:latest" --name "${KIND_CLUSTER}"
  done
fi

kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "${NAMESPACE}" create configmap "${RELEASE}-config-repo" \
  --from-file="${ROOT}/config-repo/" \
  --dry-run=client -o yaml | kubectl apply -f -

helm upgrade --install "${RELEASE}" deploy/helm/platform \
  --namespace "${NAMESPACE}" \
  --create-namespace \
  --wait --timeout 20m \
  "$@"

echo "Gateway: http://localhost:18080"
echo "BFF: http://localhost:18090"
