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

  --build-images   build/import images (--if-needed) then helm install

Requires Docker Desktop Kubernetes enabled (context: ${K8S_CONTEXT}).

Tip: heavy image import can cause IntelliJ "TLS handshake timeout".
      Close the Kubernetes tool window during --build-images, then refresh.
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
  "${ROOT}/scripts/k8s/load-images.sh" --if-needed
fi

echo "Checking Kubernetes API before Helm..."
for i in $(seq 1 36); do
  if kubectl cluster-info >/dev/null 2>&1; then
    break
  fi
  if [[ "${i}" -eq 36 ]]; then
    echo "Kubernetes API unavailable. Close IntelliJ Kubernetes tool window and retry." >&2
    exit 1
  fi
  echo "Waiting for Kubernetes API (${i}/36)..."
  sleep 5
done

kubectl apply -f - <<EOF
apiVersion: v1
kind: Namespace
metadata:
  name: ${NAMESPACE}
  labels:
    pod-security.kubernetes.io/enforce: baseline
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/warn: restricted
EOF

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
