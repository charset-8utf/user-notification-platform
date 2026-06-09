# GitLab CI/CD/CT

Пайплайн описан в [`.gitlab-ci.yml`](../.gitlab-ci.yml).

Репозиторий на GitHub: для GitLab CI настройте **push** в GitLab (импорт из GitHub даёт старый снимок — после изменений на GitHub нужен `git push`).

## Первый запуск (один раз)

### 1. SSH-ключ в GitLab

На машине, с которой пушите (тот же ключ, что для GitHub, если удобно):

```bash
cat ~/.ssh/id_ed25519.pub
ssh -T git@gitlab.com   # после добавления ключа: Welcome to GitLab, @username!
```

В GitLab: **Preferences → SSH Keys** (или **User Settings → SSH Keys**) → вставить публичный ключ → **Add key**.

Для push в проект нужен ключ **пользователя** с правом **Maintainer/Developer**, не только Deploy Key (deploy key — read-only, если не включить write).

### 2. Проект и default branch

- Проект: `charset-8utf/user-notification-platform` (или свой namespace — тогда задайте `GITLAB_URL` при push)
- **Settings → Repository → Default branch** → `main`

### 3. Shared runners

В `.gitlab-ci.yml` указано `tags: [docker]`. Нужны **shared runners** с Docker (GitLab.com SaaS обычно даёт их по умолчанию).

**Settings → CI/CD → Runners** — убедитесь, что есть активный shared runner.

### 4. Push `main` и pipeline

```bash
chmod +x scripts/push-gitlab-mirror.sh
./scripts/push-gitlab-mirror.sh main
```

Проверка: **CI/CD → Pipelines** — ожидаемая цепочка на `main`:

`verify` → `e2e-legacy` / `e2e-cloud` / `e2e-oidc` / `helm-lint` → `build-images` → `gitleaks` / `trivy-images` → `publish`

`deploy:staging` и `deploy:production` — **manual** (нужен `KUBECONFIG` в CI/CD Variables).

### 5. Другой URL проекта

```bash
GITLAB_URL=git@gitlab.com:<group>/<project>.git ./scripts/push-gitlab-mirror.sh main
```

## Зеркало GitHub → GitLab (опционально)

**Settings → Repository → Mirroring repositories** — pull-зеркало с GitHub (автосинхронизация без локального push).

После push pipeline стартует автоматически по `.gitlab-ci.yml` (jobs на `$CI_DEFAULT_BRANCH`, обычно `main`).

Параллельно на GitHub работают [GitHub Actions](GITHUB.md).

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
3. `platform-e2e-compensation.sh` — compensation → `notificationDeliveryStatus=FAILED`

### OIDC (`e2e-oidc`)

Профили `cloud` + `auth` (Keycloak), overlay `docker-compose.ci-oidc.yml`, gateway с `APP_JWT_ISSUER_URI`.
Job обязателен на `main` (parity с GitHub `e2e.yml`).

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
make ci-e2e-cloud         # smoke-cloud only
./scripts/ci.sh e2e-cloud-suite   # smoke + cross + compensation
./scripts/ci.sh e2e-oidc          # Keycloak OIDC (cloud + auth)
make ci-full              # fast + legacy + cloud suite + OIDC + security
```

## Deploy в Kubernetes

**Рекомендуемый локальный путь:** Docker Desktop Kubernetes, контекст `user-service-platform`, ingress-nginx → `http://localhost/`.

1. Настройте GitLab Kubernetes Agent или загрузите `KUBECONFIG`.
2. Запустите pipeline на `main` → manual job `deploy:staging`.
3. После deploy — `ct:smoke-k8s` (`GATEWAY_HTTP=http://localhost` через ingress-nginx).

```bash
make k8s-up
make k8s-build
make k8s-install
make k8s-smoke
```

Подробнее: [KUBERNETES.md](KUBERNETES.md).

**Альтернатива (kind + NodePort):** для CI без ingress задайте `GATEWAY_HTTP=http://localhost:18080`, `BFF_HTTP=http://localhost:18090` (см. `scripts/k8s/kind-config.yaml`, базовый `values.yaml`).
