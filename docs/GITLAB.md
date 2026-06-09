# GitLab CI/CD/CT

Пайплайн описан в [`.gitlab-ci.yml`](../.gitlab-ci.yml).

## Стадии

| Стадия | Job | Назначение |
|--------|-----|------------|
| **verify** | `verify` | `./gradlew check` — unit + integration + JaCoCo |
| **build** | `build-images` | Сборка Docker-образов |
| **security** | `gitleaks`, `trivy-images` | Секреты + CVE в образах (HIGH/CRITICAL fail) |
| **test** | `e2e-legacy`, `e2e-cloud`, `helm-lint` | Smoke в Compose + валидация Helm |
| **publish** | `publish` | Push в GitLab Container Registry |
| **deploy** | `deploy:staging`, `deploy:production` | `helm upgrade` (manual) |
| **ct** | `ct:smoke-k8s`, `ct:smoke-compose` | Post-deploy smoke |

## Переменные CI/CD (GitLab → Settings → CI/CD → Variables)

| Переменная | Описание |
|------------|----------|
| `KUBECONFIG` (file) | kubeconfig для deploy jobs |
| `APP_JWT_SECRET` | JWT secret (≥32 символов) |
| `APP_SERVICE_JWT_SECRET` | Service JWT (≥32 байт) |

Registry использует встроенный `${CI_REGISTRY_IMAGE}` — отдельная настройка не нужна.

## Локальный parity

```bash
make ci-fast          # = verify
make ci-e2e           # = e2e-legacy
make ci-e2e-cloud     # = e2e-cloud
make ci-full          # fast + e2e + security
```

## Deploy в Kubernetes

1. Настройте GitLab Kubernetes Agent или загрузите `KUBECONFIG`.
2. Запустите pipeline на `main` → manual job `deploy:staging`.
3. После deploy автоматически запустится `ct:smoke-k8s` (порты 18080/18090 для kind).

```bash
# Локально (без GitLab):
make k8s-create
docker compose build
for img in user-service notification-service config-server api-gateway web-bff; do
  kind load docker-image "${img}:latest" --name unp
done
make k8s-install
make k8s-smoke
```
