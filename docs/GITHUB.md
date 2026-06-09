# GitHub Actions

Пайплайны в [`.github/workflows/`](../.github/workflows/).

| Workflow | Триггер | Jobs |
|----------|---------|------|
| **ci.yml** | push / PR (`main`, `develop`) | `gradlew check`, Helm lint |
| **e2e.yml** | push / PR / manual (`main`, `develop`) | legacy smoke → cloud suite → OIDC |
| **security.yml** | push / PR / weekly | Gitleaks, Trivy |
| **nightly.yml** | cron 02:00 UTC / manual | observability profile |

## Parity с GitLab

| GitLab job | GitHub workflow |
|------------|-----------------|
| `verify` | `ci.yml` → verify |
| `helm-lint` | `ci.yml` → helm-lint |
| `e2e-legacy` | `e2e.yml` → e2e-legacy |
| `e2e-cloud` | `e2e.yml` → e2e-cloud (suite) |
| `e2e-oidc` | `e2e.yml` → e2e-oidc |
| `gitleaks` | `security.yml` |
| `trivy-images` | `security.yml` |
| `nightly:observability` | `nightly.yml` |

Локально полный parity: `make ci-full` (= `./scripts/ci.sh full`).

Отдельные шаги: `./scripts/ci.sh fast`, `e2e`, `e2e-cloud-suite`, `e2e-oidc`.

## OIDC в Linux CI

Для `host.docker.internal` в GitHub Actions используется overlay [`docker-compose.ci-oidc.yml`](../docker-compose.ci-oidc.yml) через `COMPOSE_FILE`.

## Статус

После push откройте **Actions** в репозитории GitHub или badge в README (`branch=main`).
