#!/usr/bin/env bash
set -euo pipefail

INGRESS_NS="${INGRESS_NS:-ingress-nginx}"
RELEASE="${INGRESS_RELEASE:-ingress-nginx}"

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx 2>/dev/null || true
helm repo update ingress-nginx >/dev/null

helm upgrade --install "${RELEASE}" ingress-nginx/ingress-nginx \
  --namespace "${INGRESS_NS}" \
  --create-namespace \
  --wait --timeout 10m \
  --set controller.watchIngressWithoutClass=true \
  --set controller.ingressClassResource.default=true \
  --set controller.service.type=LoadBalancer \
  --set controller.admissionWebhooks.enabled=false

echo "Ingress NGINX ready (namespace: ${INGRESS_NS})"
