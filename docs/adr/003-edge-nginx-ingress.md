# ADR-003: NGINX как edge (Compose + K8s)

## Status
Accepted

## Context
Единая точка входа для cloud-режима; симметрия Docker Compose и Kubernetes.

## Decision
- Compose: контейнер `nginx` (профиль `cloud`) проксирует `/` и `/bff/`.
- K8s: ingress-nginx + Helm Ingress.

## Consequences
Smoke-тесты используют `http://localhost/`; direct gateway :8080 остаётся для отладки.
