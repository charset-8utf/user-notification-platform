# DevOps: пайплайн и окружения

## Жизненный цикл

```text
commit → CI (gradlew check) → E2E smoke → security → publish (GitLab Registry) → CD (Helm) → CT (post-deploy smoke)
```

| Стадия | Инструмент | Что проверяется |
|--------|------------|-----------------|
| CI | GitLab `verify` | `./gradlew check` |
| CT legacy | GitLab `e2e-legacy` | compose + JWT + Kafka → Mailpit |
| CT cloud | GitLab `e2e-cloud` | Gateway + BFF |
| Security | GitLab `gitleaks`, `trivy-images` | Секреты + CVE (fail on HIGH/CRITICAL) |
| CD | GitLab `publish` + `deploy:*` | Registry → Helm |
| Nightly | GitLab `nightly:observability` | observability profile |

Подробнее: [GITLAB.md](GITLAB.md)

## Локальные команды

```bash
cp .env.example .env
make ci-fast
make ci-e2e
make ci-e2e-cloud
make up-full
./scripts/ci.sh fast
```

## Профили docker compose

| Профиль | Сервисов | Команда |
|---------|----------|---------|
| (default) | 9 | `docker compose up -d` |
| `cloud` | +2 (gateway, bff) | `--profile cloud` |
| `observability` | +5 | `--profile observability` |
| полный стек | 16 | `make up-full` |

## Переменные для E2E

- `APP_JWT_SECRET` (≥32 символов)
- `APP_SERVICE_JWT_SECRET` (≥32 байт)
- `APP_SEED_ADMIN_PASSWORD`, `APP_SEED_USER_PASSWORD`

## Сборка

```bash
./gradlew check
./gradlew bootJar
```

Java 21: задайте `org.gradle.java.home` в `gradle.properties` при необходимости.

## Мониторинг

Профиль `observability`: Prometheus `:9091`, Grafana `:3000`, Zipkin `:9411`.
