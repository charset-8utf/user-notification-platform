#!/usr/bin/env bash
set -euo pipefail

# Docker Desktop создаёт контекст docker-desktop; переименовываем для IDEA/CLI.
NEW_CONTEXT="${K8S_CONTEXT:-user-service-platform}"
OLD_CONTEXT="${OLD_K8S_CONTEXT:-docker-desktop}"

if kubectl config get-contexts -o name | grep -qx "${NEW_CONTEXT}"; then
  kubectl config use-context "${NEW_CONTEXT}"
  echo "Kubernetes context: ${NEW_CONTEXT}"
  exit 0
fi

if ! kubectl config get-contexts -o name | grep -qx "${OLD_CONTEXT}"; then
  echo "Neither '${OLD_CONTEXT}' nor '${NEW_CONTEXT}' found in kubeconfig." >&2
  echo "Enable Kubernetes in Docker Desktop first." >&2
  exit 1
fi

kubectl config rename-context "${OLD_CONTEXT}" "${NEW_CONTEXT}"
kubectl config use-context "${NEW_CONTEXT}"

echo "Kubernetes context: ${NEW_CONTEXT}"
