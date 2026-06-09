# GitLab CI/CD/CT

Пайплайн описан в [`.gitlab-ci.yml`](../.gitlab-ci.yml).

## Стадии

| Стадия | Job | Назначение |
|--------|-----|------------|
| **verify** | `verify` | `./gradlew check` — unit + integration + JaCoCo |
| **build** | `build-images` | Сборка Docker-образов |
| **security** | `gitleaks`, `trivy-images` | Секреты + CVE в образах (HIGH/CRITICAL fail) |
| **test** | `e2e-legacy`, `e2e-cloud`, `e2e-oidc`, `helm-lint` | Smoke в Compose + валидация Helm |
| **publish** | `publish` | Push в GitLab Container Registry |
| **deploy** | `deploy:staging`, `deploy:production` | `helm upgrade` (manual) |
| **ct** | `ct:smoke-k8s`, `ct:smoke-compose` | Post-deploy smoke |

### E2E cloud (`e2e-cloud`)

Последовательно на поднятом стеке `--profile cloud` (nginx `:80`):

1. `platform-smoke-cloud.sh` — health, login, users, BFF
2. `platform-e2e-cross-service.sh` — user → Kafka → notification
3. `platform-e2e-compensation.sh` — DLT → compensation → `notificationDeliveryStatus=FAILED`

### OIDC (`e2e-oidc`)

Профили `cloud` + `auth` (Keycloak), gateway с `APP_JWT_ISSUER_URI`. Job с `allow_failure: true` на `main`.

## Переменные CI/CD (GitLab → Settings → CI/CD → Variables)

| Переменная | Описание |
|------------|----------|
| `KUBECONFIG` (file) | kubeconfig для deploy jobs |
| `APP_JWT_SECRET` | JWT secret (≥32 символов) |
| `APP_SERVICE_JWT_SECRET` | Service JWT (≥32 байт) |
| `GATEWAY_HTTP` / `BFF_HTTP` | По умолчанию `http://localhost` (nginx ingress) |

Registry использует встроенный `${CI_REGISTRY_IMAGE}` — отдельная настройка не нужна.

## Локальный parity

```bash
make ci-fast              # = verify
make ci-e2e               # = e2e-legacy
make ci-e2e-cloud         # smoke-cloud
./scripts/ci.sh e2e-cloud-suite   # smoke + cross + compensation
./scripts/ci.sh e2e-oidc          # Keycloak OIDC (cloud + auth)
make ci-full              # fast + legacy e2e + cloud suite + security
```

## Deploy в Kubernetes

1. Настройте GitLab Kubernetes Agent или загрузите `KUBECONFIG`.
2. Запустите pipeline на `main` → manual job `deploy:staging`.
3. После deploy — `ct:smoke-k8s` (`GATEWAY_HTTP=http://localhost` через ingress-nginx).

```bash
# Локально (Docker Desktop Kubernetes):
make k8s-up
make k8s-build
make k8s-install
make k8s-smoke
```

Для **kind** с NodePort задайте в CI: `GATEWAY_HTTP=http://localhost:18080`, `BFF_HTTP=http://localhost:18090` (см. `scripts/k8s/kind-config.yaml`).
