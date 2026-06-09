# User Notification Platform

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.1-green?logo=spring)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green?logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![NGINX](https://img.shields.io/badge/NGINX-Edge-009639?logo=nginx)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5?logo=kubernetes)
![Helm](https://img.shields.io/badge/Helm-3-0F1689?logo=helm)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)
![GitHub Actions](https://github.com/charset-8utf/user-notification-platform/actions/workflows/ci.yml/badge.svg?branch=main)
![GitLab CI](https://img.shields.io/badge/GitLab%20CI/CD-Mirror-orange?logo=gitlab)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3-green?logo=swagger)

Микросервисная платформа для **управления пользователями** и **асинхронной доставки email-уведомлений**.  
Монорепозиторий на **Spring Cloud** (Spring Boot 4, Java 21, Gradle).

Покрыта unit/integration тестами, smoke/E2E сценариями, observability и Helm-чартом для Kubernetes.

---

## Описание

| Сервис | Назначение | Хранилище |
|--------|------------|-----------|
| [**user-service**](user-service/README.md) | CRUD, JWT, transactional **outbox** → Kafka | PostgreSQL |
| [**notification-service**](notification-service/README.md) | Email REST/Kafka, transactional **inbox** | MongoDB |
| **api-gateway** | Edge: JWT/OIDC, rate limit, TokenRelay | — |
| **web-bff** | API Composition `GET /bff/me` | — |
| **config-server** | Spring Cloud Config | Git/native repo |
| **platform-commons** | Tracing, audit, OpenAPI defaults | — |

**Discovery:** Kubernetes DNS + Simple Discovery Client (без Eureka).  
**Edge:** NGINX (Compose `cloud` profile + K8s ingress-nginx).

---

## Security model

Реализованы различимые механизмы аутентификации (см. [docs/SECURITY.md](docs/SECURITY.md)):

| Механизм | Где |
|----------|-----|
| **Bearer JWT** | Пользовательский API через gateway/BFF |
| **API Key** (`X-API-Key`) | M2M write в notification-service |
| **Service JWT** | Альтернатива API key для REST write |
| **Session-like refresh** | Refresh token id в Redis (user-service) |
| **OAuth2/OIDC** | Опционально: Keycloak, профиль `auth` |
| **Basic Auth** | Только `local` profile (dev) |

Подробно: [docs/SECURITY.md](docs/SECURITY.md) · ADR: [docs/adr/004-authentication-strategy.md](docs/adr/004-authentication-strategy.md)

---

## Архитектура

```text
Клиент
  └─ nginx :80 ──┬─ /      → api-gateway → user-service / notification-service
                 └─ /bff/  → web-bff

user-service ── outbox ──► Kafka ──► inbox ──► notification-service ──► Mailpit
config-server ◄── pull ── все Spring-сервисы
```

C4: [docs/c4/CONTEXT.md](docs/c4/CONTEXT.md) · Потоки: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## Точки входа

| Режим | URL | Команда |
|-------|-----|---------|
| **Cloud** (рекомендуется) | `http://localhost/` | `docker compose --profile cloud up -d` |
| Direct legacy | `:8443`, `:8444` | `docker compose up -d` |
| Kubernetes | `http://localhost/` (ingress) | `make k8s-install` |
| OpenAPI | `/swagger-ui.html` | на каждом сервисе |
| Keycloak (OIDC) | `:8180` | `--profile auth` |

---

## Быстрый старт

### Требования

- Docker Desktop (+ Kubernetes для K8s-сценария)
- Java 21
- `./gradlew` (Gradle wrapper)

### 1. Конфигурация

```bash
cp .env.example .env
```

### 2. Сборка

```bash
./gradlew build
```

### 3. Запуск (cloud + nginx)

```bash
docker compose --profile cloud up -d --build
```

### 4. Проверка

```bash
./scripts/platform-smoke-cloud.sh
make e2e-cross              # login → user → notification
make e2e-compensation       # DLT → compensation → user FAILED
make k8s-smoke              # при поднятом K8s
```

### 5. OIDC (опционально)

```bash
docker compose --profile cloud --profile auth up -d
# Gateway (JWKS из контейнера): APP_JWT_ISSUER_URI=http://host.docker.internal:8180/realms/platform
make smoke-oidc
```

Schema Registry (dev): `http://localhost:8085/subjects` — см. [docs/KAFKA.md](docs/KAFKA.md).

---

## Kubernetes

```bash
./scripts/k8s/setup-context.sh    # user-service-platform
make k8s-install
make k8s-smoke
```

| Профиль Helm | Назначение |
|--------------|------------|
| `values-dev.yaml` | локальный Docker Desktop K8s |
| `values-prod.yaml` | External Secrets, HPA, TLS, managed DB |

Документация: [docs/KUBERNETES.md](docs/KUBERNETES.md)

---

## CI/CD

| Платформа | Конфиг | Документация |
|-----------|--------|--------------|
| **GitHub Actions** | [`.github/workflows/`](.github/workflows/) | [docs/GITHUB.md](docs/GITHUB.md) |
| **GitLab CI** | [`.gitlab-ci.yml`](.gitlab-ci.yml) | [docs/GITLAB.md](docs/GITLAB.md) |

```bash
make ci-fast
make ci-e2e-cloud-suite
make ci-full
./scripts/push-gitlab-mirror.sh main   # GitLab pipeline
```

---

## Observability

Профиль `observability`: Prometheus, Grafana, Loki, Zipkin.

- Dashboard **Outbox/Inbox**: `infra/observability/grafana/provisioning/dashboards/json/outbox-inbox.json`
- Runbooks: [docs/runbooks/](docs/runbooks/)
- Алерты: `infra/observability/prometheus/alerts.yml`

---

## Ключевые паттерны

- **Transactional Outbox** + **Transactional Inbox**
- **Saga compensation** (inbox failure / DLT → compensation topic)
- **Strangler** (direct HTTPS + cloud gateway)
- **BFF** + **API Gateway**
- **NGINX edge** (Compose + K8s)

ADR: [docs/adr/](docs/adr/)

---

## Структура репозитория

```text
user-notification-platform/
├── user-service/          # PostgreSQL, outbox, JWT
├── notification-service/  # MongoDB, inbox, API key
├── api-gateway/           # Edge security
├── web-bff/
├── config-server/ + config-repo/
├── platform-commons/
├── deploy/helm/platform/  # HPA, ExternalSecret, Ingress TLS
├── infra/                 # nginx, keycloak, observability
├── scripts/               # smoke, e2e, k8s, chaos
└── docs/                  # SECURITY, ADR, C4, runbooks
```

---

## Production readiness

| Область | Dev | Prod (`values-prod.yaml`) |
|---------|-----|---------------------------|
| Secrets | `.env` | External Secrets (Vault) |
| TLS | HTTP | cert-manager + HTTPS ingress |
| Auth | HS256 JWT | OIDC JWKS (Keycloak) |
| Scale | 1 replica | HPA 2–8 |
| DB | in-cluster | managed Postgres/Mongo URLs |

Перед выкладкой в prod: [docs/runbooks/SECURITY_INCIDENT.md](docs/runbooks/SECURITY_INCIDENT.md)

---

## Автор

[charset-8utf](https://github.com/charset-8utf)
