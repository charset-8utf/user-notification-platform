# Kubernetes (kind + Helm)

## Docker Desktop и kind

Платформа использует **kind**-кластер `unp` (контейнер `unp-control-plane` в Docker Desktop).  
Это **отдельно** от встроенного Kubernetes в Docker Desktop.

| Что видите | Что это значит |
|------------|----------------|
| Красный «Kubernetes» в Docker Desktop | Встроенный K8s Docker Desktop выключен или сломан — **не влияет** на kind |
| Контейнер `unp-control-plane` Running | Ваш рабочий кластер kind |
| `kubectl config current-context` → `kind-unp` | Всё настроено правильно |

Проверка:

```bash
kind get clusters          # unp
kubectl cluster-info       # https://127.0.0.1:...
kubectl get nodes          # unp-control-plane Ready
```

Включать Kubernetes в Docker Desktop **не обязательно**, если используете kind.

## Требования

- Docker
- [kind](https://kind.sigs.k8s.io/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [Helm 3](https://helm.sh/)
- Образы в GitLab Registry или локально (`docker compose build` + `kind load`)

## Локальный кластер

```bash
kind create cluster --name unp --config scripts/k8s/kind-config.yaml

for img in user-service notification-service config-server api-gateway web-bff; do
  kind load docker-image "${img}:latest" --name unp
done

chmod +x scripts/k8s/install.sh scripts/k8s-smoke.sh
./scripts/k8s/install.sh

kubectl -n platform get pods
./scripts/k8s-smoke.sh   # Gateway :18080, BFF :18090
```

## Профили Helm

| values | Содержимое |
|--------|------------|
| `infra.enabled` | postgres, mongo, redis, kafka, mailpit |
| `platform.enabled` | config-server, user-service, notification-service |
| `edge.enabled` | api-gateway, web-bff (NodePort 30080/30090) |
| `observability.enabled` | (зарезервировано) |

## Service Discovery

Eureka не используется. Сервисы обнаруживаются через **Kubernetes DNS** и **Simple Discovery Client** (профиль `kubernetes`).

## Service links

Для pod'ов Kafka и Spring-сервисов включено `enableServiceLinks: false` — иначе Confluent Kafka и Spring Boot получают конфликтующие env-переменные (`KAFKA_PORT`, `EUREKA_SERVER_PORT`).

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
helm uninstall platform -n platform
kind delete cluster --name unp
```
