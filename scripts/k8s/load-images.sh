#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
K8S_CONTEXT="${K8S_CONTEXT:-user-service-platform}"
IMAGES=(user-service notification-service config-server api-gateway web-bff)

cd "${ROOT}"
chmod +x "${ROOT}/scripts/k8s/setup-context.sh"
"${ROOT}/scripts/k8s/setup-context.sh" >/dev/null

docker compose build "${IMAGES[@]}"

node="$(kubectl get nodes -o jsonpath='{.items[0].metadata.name}')"
for img in "${IMAGES[@]}"; do
  echo "Loading ${img}:latest into ${node}..."
  docker save "${img}:latest" | docker exec -i "${node}" ctr -n k8s.io images import -
done
