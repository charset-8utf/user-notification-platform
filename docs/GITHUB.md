# GitHub Actions

Пайплайны в [`.github/workflows/`](../.github/workflows/).

| Workflow | Триггер | Jobs |
|----------|---------|------|
| **ci.yml** | push / PR (`main`, `develop`, `microservice-feature`) | `gradlew check`, Helm lint |
| **e2e.yml** | push / PR / manual | legacy smoke → cloud suite → OIDC (optional) |
| **security.yml** | push / PR / weekly | Gitleaks, Trivy (образы на `main`) |
| **nightly.yml** | cron 02:00 UTC / manual | observability profile |

## Parity с GitLab

| GitLab job | GitHub workflow |
|------------|-----------------|
| `verify` | `ci.yml` → verify |
| `helm-lint` | `ci.yml` → helm-lint |
| `e2e-legacy` | `e2e.yml` → e2e-legacy |
| `e2e-cloud` | `e2e.yml` → e2e-cloud (suite) |
| `e2e-oidc` | `e2e.yml` → e2e-oidc (`continue-on-error`) |
| `gitleaks` | `security.yml` |
| `trivy-images` | `security.yml` (не на PR) |
| `nightly:observability` | `nightly.yml` |

Локально те же сценарии: `./scripts/ci.sh fast`, `e2e`, `e2e-cloud-suite`, `full`.

## OIDC в Linux CI

Для `host.docker.internal` в GitHub Actions используется overlay [`docker-compose.ci-oidc.yml`](../docker-compose.ci-oidc.yml) через `COMPOSE_FILE`.

## Статус

После push откройте **Actions** в репозитории GitHub или badge в README.
