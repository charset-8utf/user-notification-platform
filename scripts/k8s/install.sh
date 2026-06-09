#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RELEASE="${RELEASE:-platform}"
NAMESPACE="${NAMESPACE:-platform}"
K8S_CONTEXT="${K8S_CONTEXT:-user-service-platform}"
VALUES_FILE="${VALUES_FILE:-deploy/helm/platform/values-dev.yaml}"
BUILD_IMAGES=false

usage() {
  cat <<EOF
Usage: $(basename "$0") [--build-images] [helm upgrade extra args...]

  --build-images   docker compose build + import into K8s node (Docker Desktop)

Requires Docker Desktop Kubernetes enabled (context: ${K8S_CONTEXT}).
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --build-images|--load-images) BUILD_IMAGES=true; shift ;;
    -h|--help) usage; exit 0 ;;
    *) break ;;
  esac
done

cd "${ROOT}"

chmod +x "${ROOT}/scripts/k8s/setup-context.sh"
"${ROOT}/scripts/k8s/setup-context.sh"

if [[ "${VALUES_FILE}" == *values-dev* ]]; then
  chmod +x "${ROOT}/scripts/k8s/install-ingress-nginx.sh"
  "${ROOT}/scripts/k8s/install-ingress-nginx.sh"
fi

if [ "${BUILD_IMAGES}" = true ]; then
  "${ROOT}/scripts/k8s/load-images.sh"
fi

kubectl create namespace "${NAMESPACE}" --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "${NAMESPACE}" create configmap "${RELEASE}-config-repo" \
  --from-file="${ROOT}/config-repo/" \
  --dry-run=client -o yaml | kubectl apply -f -

helm upgrade --install "${RELEASE}" deploy/helm/platform \
  --namespace "${NAMESPACE}" \
  --create-namespace \
  --values "${VALUES_FILE}" \
  --wait --timeout 20m \
  "$@"

if [[ "${VALUES_FILE}" == *values-dev* ]]; then
  echo "Gateway: http://localhost/ (nginx ingress)"
  echo "BFF: http://localhost/bff/"
else
  echo "Gateway: NodePort / LoadBalancer (see kubectl -n ${NAMESPACE} get svc)"
fi
echo "(CI/kind: GATEWAY_HTTP=http://localhost:18080 BFF_HTTP=http://localhost:18090)"
