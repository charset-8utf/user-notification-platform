# GitHub Actions

Пайплайны в [`.github/workflows/`](../.github/workflows/).

| Workflow | Триггер | Jobs |
|----------|---------|------|
| **ci.yml** | push / PR (`main`, `develop`) | `gradlew check`, Helm lint |
| **e2e.yml** | push / PR / manual (`main`, `develop`) | legacy smoke → cloud suite → OIDC |
| **security.yml** | push / PR / weekly | Gitleaks, Trivy |
| **nightly.yml** | cron 02:00 UTC / manual | observability profile |

## Jobs

| Job | Workflow | Локальный аналог |
|-----|----------|------------------|
| Gradle check | `ci.yml` | `make ci-fast` |
| Helm lint | `ci.yml` | `helm lint deploy/helm/platform` |
| E2E legacy | `e2e.yml` | `make ci-e2e` |
| E2E cloud suite | `e2e.yml` | `make ci-e2e-cloud-suite` |
| E2E OIDC | `e2e.yml` | `./scripts/ci.sh e2e-oidc` |
| Gitleaks | `security.yml` | `gitleaks detect` |
| Trivy images | `security.yml` | `./scripts/ci.sh security` |
| Observability | `nightly.yml` | `./scripts/ci.sh observability-up` |

Локально полный прогон: `make ci-full` (= `./scripts/ci.sh full`).

## OIDC в Linux CI

Для `host.docker.internal` в GitHub Actions используется overlay [`docker-compose.ci-oidc.yml`](../docker-compose.ci-oidc.yml) через `COMPOSE_FILE`.

## Статус

После push откройте **Actions** в репозитории GitHub или badge в README (`branch=main`).
