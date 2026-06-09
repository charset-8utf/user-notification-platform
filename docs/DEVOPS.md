# DevOps: пайплайн и окружения

## Жизненный цикл

```text
commit → CI (gradlew check) → E2E smoke → security → CD (Helm, manual) → post-deploy smoke
```

| Стадия | Инструмент | Что проверяется |
|--------|------------|-----------------|
| CI | GitHub `ci.yml` | `./gradlew check`, Helm lint |
| CT legacy | GitHub `e2e.yml` → `e2e-legacy` | compose + JWT + Kafka → Mailpit |
| CT cloud | GitHub `e2e.yml` → `e2e-cloud` | nginx → gateway/BFF + cross-service + compensation |
| CT OIDC | GitHub `e2e.yml` → `e2e-oidc` | Keycloak token → gateway JWKS |
| Security | GitHub `security.yml` | Gitleaks, Trivy (HIGH/CRITICAL fail) |
| Nightly | GitHub `nightly.yml` | observability profile |

Подробнее: [GITHUB.md](GITHUB.md)

## Локальные команды

```bash
cp .env.example .env
make ci-fast
make ci-e2e
make ci-e2e-cloud-suite
./scripts/ci.sh e2e-oidc
make ci-full
make up-full
./scripts/ci.sh fast
```

## Профили docker compose

| Профиль | Сервисов | Команда |
|---------|----------|---------|
| (default) | 9 | `docker compose up -d` |
| `cloud` | +nginx, gateway, bff | `--profile cloud` |
| `auth` | +Keycloak | `--profile auth` |
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
