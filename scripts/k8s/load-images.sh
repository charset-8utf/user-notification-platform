#!/usr/bin/env bash
# Import local Docker images into Docker Desktop Kubernetes (containerd).
# Heavy imports can starve the API server — use --if-needed and avoid running
# while IntelliJ Kubernetes plugin is actively polling the cluster.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
K8S_CONTEXT="${K8S_CONTEXT:-user-service-platform}"
IMAGES=(user-service notification-service config-server api-gateway web-bff)

BUILD=true
IMPORT=true
FORCE=false
IF_NEEDED=false
PAUSE_BETWEEN_IMPORTS="${PAUSE_BETWEEN_IMPORTS:-10}"
KUBECTL_WAIT_ATTEMPTS="${KUBECTL_WAIT_ATTEMPTS:-36}"

usage() {
  cat <<EOF
Usage: $(basename "$0") [options]

  Build local images and import them into the K8s node (Docker Desktop / kind).

Options:
  --if-needed       Skip build/import when image already exists (recommended)
  --force           Always rebuild and re-import
  --skip-build      Import only (images must exist locally)
  --skip-import     Build only
  -h, --help        Show help

Env:
  PAUSE_BETWEEN_IMPORTS   Seconds between imports (default: 10)
  KUBECTL_WAIT_ATTEMPTS   API readiness retries, 5s each (default: 36)

Tip: if IntelliJ shows "TLS handshake timeout", close the Kubernetes tool window
     for the duration of this script, then refresh after it completes.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --if-needed) IF_NEEDED=true; shift ;;
    --force) FORCE=true; shift ;;
    --skip-build) BUILD=false; shift ;;
    --skip-import) IMPORT=false; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
  esac
done

wait_for_kubectl() {
  local attempt=1
  while ! kubectl cluster-info >/dev/null 2>&1; do
    if [[ "${attempt}" -ge "${KUBECTL_WAIT_ATTEMPTS}" ]]; then
      echo "Kubernetes API not reachable after ${KUBECTL_WAIT_ATTEMPTS} attempts." >&2
      echo "Ensure Docker Desktop → Kubernetes is Running." >&2
      echo "If using IntelliJ, close the Kubernetes tool window and retry." >&2
      exit 1
    fi
    echo "Waiting for Kubernetes API (${attempt}/${KUBECTL_WAIT_ATTEMPTS})..."
    sleep 5
    attempt=$((attempt + 1))
  done
}

image_exists_locally() {
  docker image inspect "$1:latest" >/dev/null 2>&1
}

image_exists_in_cluster() {
  local img="$1" node="$2"
  docker exec "${node}" ctr -n k8s.io images ls -q 2>/dev/null \
    | grep -Fq "${img}:latest" || \
  docker exec "${node}" ctr -n k8s.io images ls -q 2>/dev/null \
    | grep -Fq "docker.io/library/${img}:latest"
}

import_image() {
  local img="$1" node="$2"
  local tmp

  echo "=== Import ${img}:latest → ${node} ==="
  tmp="$(mktemp "/tmp/unp-${img}-XXXXXX.tar")"

  docker save "${img}:latest" -o "${tmp}"
  wait_for_kubectl

  echo "Importing via containerd (single image, no parallel load)..."
  cat "${tmp}" | docker exec -i "${node}" ctr -n k8s.io images import -
  rm -f "${tmp}"

  if ! image_exists_in_cluster "${img}" "${node}"; then
    echo "Import verification failed for ${img}:latest" >&2
    exit 1
  fi

  echo "Imported ${img}:latest. Pausing ${PAUSE_BETWEEN_IMPORTS}s for control-plane..."
  sleep "${PAUSE_BETWEEN_IMPORTS}"
}

cd "${ROOT}"
chmod +x "${ROOT}/scripts/k8s/setup-context.sh"
"${ROOT}/scripts/k8s/setup-context.sh" >/dev/null
wait_for_kubectl

if [[ "${BUILD}" == true ]]; then
  to_build=()
  for img in "${IMAGES[@]}"; do
    if [[ "${FORCE}" == true ]] || ! image_exists_locally "${img}"; then
      to_build+=("${img}")
    fi
  done
  if [[ "${#to_build[@]}" -gt 0 ]]; then
    echo "Building images: ${to_build[*]}"
    docker compose --profile cloud build "${to_build[@]}"
  else
    echo "Local images present (use --force to rebuild)."
  fi
fi

if [[ "${IMPORT}" == true ]]; then
  node="$(kubectl get nodes -o jsonpath='{.items[0].metadata.name}')"
  echo "Target node: ${node}"

  for img in "${IMAGES[@]}"; do
    if ! image_exists_locally "${img}"; then
      echo "Missing local image ${img}:latest. Run without --skip-build first." >&2
      exit 1
    fi
    if [[ "${IF_NEEDED}" == true && "${FORCE}" != true ]] && image_exists_in_cluster "${img}" "${node}"; then
      echo "Skipping import: ${img}:latest already in cluster."
      continue
    fi
    import_image "${img}" "${node}"
  done
fi

echo "Done. Images ready for imagePullPolicy: Never"
