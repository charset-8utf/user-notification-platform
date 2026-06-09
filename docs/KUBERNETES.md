# Kubernetes (Docker Desktop + Helm)

## Локальный кластер

Платформа разворачивается в **встроенном Kubernetes Docker Desktop**.

| Параметр | Значение |
|----------|----------|
| Контекст kubectl / IDEA | `user-service-platform` |
| Kubeconfig | `~/.kube/config` |
| Нода | `desktop-control-plane` |
| Edge | **nginx ingress** → `http://localhost/` |

В Docker Desktop → **Kubernetes** должен быть статус **Running** (зелёный).

```bash
docker desktop kubernetes status
./scripts/k8s/setup-context.sh    # docker-desktop → user-service-platform
kubectl config current-context  # user-service-platform
kubectl get nodes
```

### IntelliJ IDEA

**Settings → Kubernetes → Kubeconfig:** `~/.kube/config`  
**Context:** `user-service-platform`

Если после перезапуска Docker Desktop снова появился `docker-desktop`:

```bash
./scripts/k8s/setup-context.sh
```

Если кластер не стартует:

```bash
kind delete cluster --name unp 2>/dev/null || true
docker desktop kubernetes reset-cluster
./scripts/k8s/setup-context.sh
```

## Требования

- Docker Desktop с включённым Kubernetes
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Helm 3](https://helm.sh/)
- Локальные образы: `docker compose build` + import в ноду (`scripts/k8s/load-images.sh`)

## Установка

```bash
make k8s-install
# или по шагам:
make k8s-up
make k8s-build
./scripts/k8s/install.sh --build-images

kubectl -n ingress-nginx get pods
kubectl -n platform get pods
./scripts/k8s-smoke.sh   # http://localhost/ через nginx
```

`install.sh` с `values-dev.yaml` автоматически:
1. устанавливает **ingress-nginx** (`ingress-nginx` namespace);
2. разворачивает платформу с Ingress (`api-gateway` → `/`, `web-bff` → `/bff`).

### Альтернатива: kind + NodePort

Для CI без ingress-nginx или отдельного kind-кластера:

- `values.yaml` (NodePort 30080/30090) + `scripts/k8s/kind-config.yaml`
- Smoke: `GATEWAY_HTTP=http://localhost:18080 BFF_HTTP=http://localhost:18090 ./scripts/k8s-smoke.sh`

Основной локальный путь — Docker Desktop + `values-dev.yaml` + ingress на `:80` (см. выше).

## Профили Helm

| values | Содержимое |
|--------|------------|
| `infra.enabled` | postgres, mongo, redis, kafka, mailpit |
| `platform.enabled` | config-server, user-service, notification-service |
| `edge.enabled` | api-gateway, web-bff (ClusterIP + Ingress в dev) |
| `observability.enabled` | (зарезервировано) |

## Service Discovery

Eureka не используется. Сервисы обнаруживаются через **Kubernetes DNS** и **Simple Discovery Client** (профиль `kubernetes`).

## Service links

Для pod'ов Kafka и Spring-сервисов включено `enableServiceLinks: false` — иначе Confluent Kafka и Spring Boot получают конфликтующие env-переменные (`KAFKA_PORT` и др.).

## GitLab CD

Deploy через manual job `deploy:staging` в GitLab CI. Образы из `${CI_REGISTRY_IMAGE}`:

```bash
helm upgrade --install platform deploy/helm/platform \
  --set global.registry="${CI_REGISTRY}" \
  --set global.imageOwner="${CI_PROJECT_PATH}" \
  --set global.imageTag="${CI_COMMIT_SHORT_SHA}"
```

## Удаление

```bash
make k8s-delete
helm uninstall ingress-nginx -n ingress-nginx 2>/dev/null || true
# полный сброс кластера Docker Desktop:
docker desktop kubernetes reset-cluster
./scripts/k8s/setup-context.sh
```
