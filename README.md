# User Notification Platform

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.1-green?logo=spring)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-2025.1-green?logo=spring)
![Spring Cloud Config](https://img.shields.io/badge/Spring%20Cloud%20Config-5.0.3-green?logo=spring)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green?logo=springsecurity)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Kafka](https://img.shields.io/badge/Kafka-7.4%20KRaft-black?logo=apachekafka)
![Avro](https://img.shields.io/badge/Avro-Schema%20Registry-0078D4?logo=apache)
![Liquibase](https://img.shields.io/badge/Liquibase-4.x-red?logo=liquibase)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5?logo=kubernetes)
![Helm](https://img.shields.io/badge/Helm-3-0F1689?logo=helm)
![NGINX](https://img.shields.io/badge/NGINX-Edge-009639?logo=nginx)
![Keycloak](https://img.shields.io/badge/Keycloak-26%20OIDC-000000?logo=keycloak&logoColor=white)
![Argo CD](https://img.shields.io/badge/Argo%20CD-GitOps-EF7B4D?logo=argo&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-3.0-E6522C?logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-11-FF9900?logo=grafana&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3-green?logo=swagger)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
![JaCoCo](https://img.shields.io/badge/JaCoCo-0.8.14-blue)
![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-red)
![Resilience4j](https://img.shields.io/badge/Resilience4j-2.4.0-blueviolet)

[![CI](https://github.com/charset-8utf/user-notification-platform/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/charset-8utf/user-notification-platform/actions/workflows/ci.yml)
[![E2E](https://github.com/charset-8utf/user-notification-platform/actions/workflows/e2e.yml/badge.svg?branch=main)](https://github.com/charset-8utf/user-notification-platform/actions/workflows/e2e.yml)
[![Security](https://github.com/charset-8utf/user-notification-platform/actions/workflows/security.yml/badge.svg?branch=main)](https://github.com/charset-8utf/user-notification-platform/actions/workflows/security.yml)

Микросервисная платформа для **управления пользователями** и **асинхронной доставки email-уведомлений**.  
Монорепозиторий на **Spring Cloud** (Spring Boot 4, Java 21, Gradle).

Покрыта unit/integration тестами, smoke/E2E сценариями, observability, Helm-чартом для Kubernetes, GitOps (Argo CD) и supply-chain проверками в CI.

---

## Сервисы

| Сервис                                                     | Назначение                                                             | Хранилище         |
|------------------------------------------------------------|------------------------------------------------------------------------|-------------------|
| [**user-service**](user-service/README.md)                 | CRUD, JWT, transactional **outbox** → Kafka, **compensation consumer** | PostgreSQL        |
| [**notification-service**](notification-service/README.md) | Email REST/Kafka, transactional **inbox**, compensation publisher      | MongoDB           |
| [**api-gateway**](api-gateway/README.md)                   | Edge: JWT/OIDC, rate limit, TokenRelay, circuit breaker                | —                 |
| [**web-bff**](web-bff/README.md)                           | API Composition `GET /bff/me`                                          | —                 |
| [**config-server**](config-server/README.md)               | Spring Cloud Config (native / git)                                     | Git / native repo |
| [**platform-commons**](platform-commons/README.md)         | Tracing, audit, OpenAPI defaults, Kafka SASL                           | —                 |
| **kafka-contracts**                                        | Avro-схемы и serde для Kafka                                           | —                 |

**Discovery:** Kubernetes DNS + Simple Discovery Client (без Eureka).  
**Edge:** NGINX (Compose `cloud` profile + K8s ingress-nginx).

---

## Архитектура

```text
Клиент
  └─ nginx :80 ──┬─ /      → api-gateway → user-service / notification-service
                 └─ /bff/  → web-bff

user-service ── outbox ──► Kafka (user-notifications) ──► inbox ──► notification-service ──► Mailpit
       ▲                                                          │
       │              notification-compensations ◄────────────────┘
       └──────── compensation consumer (choreography saga rollback)

config-server ◄── pull ── все Spring-сервисы
Schema Registry (:8085) — Avro в compose
Kafka — KRaft (single-node dev), без Zookeeper
```

### Поток: создание пользователя

1. `POST /api/users` → user-service (JWT)
2. Транзакция + запись в `notification_outbox`
3. `KafkaOutboxRelay` → топик `user-notifications`
4. Kafka consumer → `notification_inbox` (PENDING)
5. `KafkaInboxRelay` → email → Mailpit → PROCESSED

### Saga compensation

При сбое доставки email **notification-service** публикует в **`notification-compensations`**. **user-service** выполняет компенсацию:

| Операция       | Действие                                              |
|----------------|-------------------------------------------------------|
| `USER_CREATED` | Rollback — удаление пользователя, eviction Redis-кэша |
| `USER_DELETED` | Signal-only — метрика + лог                           |

Источники: failed inbox relay, DLT (`user-notifications.DLT`), синхронный REST (профиль `rest`).  
E2E: `make e2e-compensation`

---

## Security

| Механизм                  | Где                                     |
|---------------------------|-----------------------------------------|
| **Bearer JWT**            | Пользовательский API через gateway/BFF  |
| **API Key** (`X-API-Key`) | M2M write в notification-service        |
| **Service JWT**           | Альтернатива API key для REST write     |
| **Session-like refresh**  | Refresh token id в Redis (user-service) |
| **OAuth2/OIDC**           | Keycloak, профиль `auth`                |
| **Basic Auth**            | Только `local` profile (dev)            |

Production: TLS на ingress (cert-manager), External Secrets, OIDC JWKS вместо HS256, `INSECURE_SSL=false`.

---

## Kafka

| Топик                        | Назначение           | Формат (compose)       |
|------------------------------|----------------------|------------------------|
| `user-notifications`         | События пользователя | Avro + Schema Registry |
| `notification-compensations` | Saga compensation    | JSON                   |
| `user-notifications.DLT`     | Dead Letter Queue    | как основной топик     |

| Паттерн | Сторона              | Хранилище                          |
|---------|----------------------|------------------------------------|
| Outbox  | user-service         | `notification_outbox` (PostgreSQL) |
| Inbox   | notification-service | `notification_inbox` (MongoDB)     |

Сериализация: `APP_KAFKA_SERIALIZATION=avro|json` (compose — avro по умолчанию).  
Avro-схема: [`schemas/avro/notification-email-message.avsc`](schemas/avro/notification-email-message.avsc).  
Schema Registry: `http://localhost:8085/subjects`.  
Проверка compat: `./scripts/kafka/schema-compat-check.sh`.

---

## Точки входа

| Режим                     | URL                           | Команда                                |
|---------------------------|-------------------------------|----------------------------------------|
| **Cloud** (рекомендуется) | `http://localhost/`           | `docker compose --profile cloud up -d` |
| Direct legacy             | `:8443`, `:8444`              | `docker compose up -d`                 |
| Kubernetes                | `http://localhost/` (ingress) | `make k8s-install-build`               |
| OpenAPI                   | `/swagger-ui.html`            | на каждом сервисе                      |
| Keycloak (OIDC)           | `:8180`                       | `--profile auth`                       |

---

## Быстрый старт

### Требования

- Docker Desktop (+ Kubernetes для K8s-сценария)
- **Java 21** (Gradle toolchain; для daemon — JDK 21, не новее)
- `./gradlew` (Gradle wrapper 8.14+)

### 1. Конфигурация

```bash
cp .env.example .env
```

Ключевые переменные: JWT-секреты, seed-пароли, `NGINX_HTTP_PORT`, `APP_KAFKA_SERIALIZATION`.

### 2. Сборка

```bash
./gradlew build
./gradlew :user-service:check   # один модуль
```

- Версии — [`gradle/libs.versions.toml`](gradle/libs.versions.toml)
- **Error Prone** + **NullAway** (JSpecify) — `user-service`, `notification-service`
- **MapStruct** + Lombok; Avro — [`kafka-contracts/`](kafka-contracts/)

### 3. Запуск

```bash
docker compose --profile cloud up -d --build
```

Полный стек: `make up-full` (cloud + observability).

| Профиль         | Добавляет                                       |
|-----------------|-------------------------------------------------|
| *(default)*     | infra + apps (9 сервисов)                       |
| `cloud`         | nginx, api-gateway, web-bff                     |
| `auth`          | Keycloak                                        |
| `observability` | Prometheus, Grafana, Loki, Zipkin, Alertmanager |

### 4. Проверка

> **Docker Desktop + K8s:** ingress-nginx занимает `:80`. Compose nginx — **`:8088`** (`NGINX_HTTP_PORT=8088`).

```bash
GATEWAY_HTTP=http://localhost:8088 BFF_HTTP=http://localhost:8088 ./scripts/platform-smoke-cloud.sh
make e2e-cross
make e2e-compensation
make k8s-smoke
```

Инфра: PostgreSQL, MongoDB, Redis, Kafka (KRaft), Schema Registry, Mailpit.  
Опционально Kafka SASL_SSL: `APP_KAFKA_SECURITY_ENABLED=true` (сертификаты в `infra/tls/`, `infra/kafka/`).

### 5. OIDC (опционально)

```bash
COMPOSE_FILE=docker-compose.yml:docker-compose.local-oidc.yml \
  docker compose --profile cloud --profile auth up -d
GATEWAY_HTTP=http://localhost:8088 make smoke-oidc
```

---

## Kubernetes

```bash
./scripts/k8s/setup-context.sh    # контекст user-service-platform
make k8s-install-build
make k8s-smoke
make k8s-e2e-cross
make k8s-e2e-compensation
```

Helm chart: `deploy/helm/platform/`

| values             | Назначение                                              |
|--------------------|---------------------------------------------------------|
| `values-dev.yaml`  | Docker Desktop K8s, локальные образы                    |
| `values-prod.yaml` | External Secrets, HPA, TLS, managed DB, GHCR-образы     |

| Флаг Helm | Содержимое |
|-----------|------------|
| `infra.enabled` | postgres, mongo, redis, kafka, schema-registry, mailpit |
| `platform.enabled` | config-server, user-service, notification-service |
| `edge.enabled` | api-gateway, web-bff, Ingress |
| `networkPolicy.enabled` | default-deny + явные правила между сервисами |
| `observability.enabled` | ServiceMonitor (Prometheus Operator) |

Ручной deploy:

```bash
helm upgrade --install platform deploy/helm/platform \
  --namespace platform -f deploy/helm/platform/values-dev.yaml
```

### GitOps (Argo CD)

Требуется [Argo CD](https://argo-cd.readthedocs.io/) и Helm 3 в кластере.

```bash
make gitops-dev                                              # dev
kubectl apply -f deploy/gitops/argocd/platform-prod.yaml     # prod (обновите repoURL при fork)

argocd app get platform-dev
argocd app sync platform-dev
```

| Манифест | Назначение |
|----------|------------|
| `deploy/gitops/argocd/platform-dev.yaml` | dev: infra + platform + ingress |
| `deploy/gitops/argocd/platform-prod.yaml` | prod: external DB, HPA, NetworkPolicy, ServiceMonitor |

Prod: CI пушит образы в GHCR с тегом `git sha` → обновить `global.imageTag` в `values-prod.yaml` (или override в Argo CD).

### Kyverno

Policy-as-code (audit): `deploy/kyverno/policies.yaml` — requests/limits, non-root, запрет `:latest`.

```bash
make k8s-kyverno
```

Удаление: `make k8s-delete`

---

## CI/CD

Workflows: [`.github/workflows/`](.github/workflows/)

| Workflow       | Проверки                                   |
|----------------|--------------------------------------------|
| `ci.yml`       | `./gradlew check`, Helm lint               |
| `e2e.yml`      | legacy → cloud suite → OIDC, schema compat |
| `security.yml` | Gitleaks, Hadolint, Trivy, SBOM, Cosign    |
| `nightly.yml`  | observability profile                      |

```bash
make ci-fast
make ci-e2e-cloud-suite
make ci-full          # паритет с GHA (nginx на :80)
make help
```

> **`ci-full` на :80:** если K8s ingress занял порт — `helm uninstall platform -n platform` или отключите K8s в Docker Desktop. Для разработки — `NGINX_HTTP_PORT=8088`.

---

## Observability

Профиль `observability`: Prometheus `:9091`, Grafana `:3000`, Zipkin `:9411`.

- Dashboard outbox/inbox: `infra/observability/grafana/provisioning/dashboards/json/outbox-inbox.json`
- Алерты: `infra/observability/prometheus/alerts.yml`

---

## Паттерны

- Transactional **Outbox** + **Inbox**
- **Saga compensation** (choreography)
- **Avro** + Schema Registry
- **Strangler** (direct HTTPS + cloud gateway)
- **BFF** + **API Gateway**
- **Null-safety** — JSpecify + NullAway

---

## Структура репозитория

```text
user-notification-platform/
├── user-service/
├── notification-service/
├── api-gateway/
├── web-bff/
├── config-server/ + config-repo/
├── platform-commons/
├── kafka-contracts/           # schemas/avro/
├── docker/spring-service/     # общий multi-stage Dockerfile (build-arg MODULE)
├── deploy/helm/platform/
├── deploy/gitops/argocd/
├── deploy/kyverno/
├── infra/                     # nginx, keycloak, observability, tls
└── scripts/                   # smoke, e2e, k8s, ci
```

---

## Автор

[charset-8utf](https://github.com/charset-8utf)
