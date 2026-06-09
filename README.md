# User Notification Platform

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.1-green?logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)
![GitLab CI](https://img.shields.io/badge/GitLab%20CI/CD-Enabled-orange?logo=gitlab)

Микросервисная платформа для управления пользователями и доставки email-уведомлений.  
Монорепозиторий на **Spring Cloud** (Spring Boot 4, Java 21).

## Сервисы

| Сервис | Назначение | Хранилище |
|--------|------------|-----------|
| **user-service** | CRUD пользователей, JWT-аутентификация, transactional outbox → Kafka | PostgreSQL |
| **notification-service** | Email-уведомления (REST + Kafka), аудит доставки | MongoDB |

## Платформенные компоненты

| Компонент | Роль |
|-----------|------|
| **api-gateway** | Единая точка входа: JWT, rate limit, circuit breaker, TokenRelay |
| **web-bff** | API Composition — агрегация `GET /bff/me` |
| **config-server** | Централизованная конфигурация (Spring Cloud Config) |
| **platform-commons** | Shared chassis: метрики, tracing, discovery defaults |

Service discovery: **Kubernetes DNS + Simple Discovery Client** (без Eureka).  
В Docker Compose и Kubernetes сервисы резолвятся по имени (`user-service`, `notification-service`).

## Точки входа

| Режим | Endpoint | Порт |
|-------|----------|------|
| **Direct** (legacy) | user-service | https://localhost:8443 |
| **Direct** (legacy) | notification-service | https://localhost:8444 |
| **Cloud** | API Gateway | http://localhost:8080 |
| **Cloud** | web-bff | http://localhost:8090 |

В cloud-режиме BFF агрегирует данные через API Gateway (`http://api-gateway:8080`), а не напрямую в микросервисы.
| Config Server | — | http://localhost:8888 |

### API Gateway (профиль `cloud`)

| Маршрут | Сервис | Фильтры |
|---------|--------|---------|
| `/api/auth/**` | user-service | Rate limit по IP |
| `/api/users/**`, `/api/roles/**`, `/api/profiles/**` | user-service | JWT, TokenRelay, CB, Retry (GET) |
| `/api/notifications/logs/**` | notification-service | JWT, TokenRelay, CB |

### BFF

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/bff/me` | User + profile + последнее уведомление |

## Архитектура

```text
Клиент
  ├─ direct ──► user-service :8443 / notification-service :8444
  └─ cloud  ──► api-gateway :8080 ──► user-service / notification-service
                web-bff :8090 (агрегация /bff/me)

config-server ◄── pull ── все Spring-сервисы

user-service ── outbox ──► Kafka ──► notification-service ──► SMTP / Mailpit
```

Подробнее: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

## Быстрый старт

### Требования

- Docker Desktop
- Java 21
- Gradle 8.14+ (wrapper: `./gradlew`)

### 1. Конфигурация

```bash
cp .env.example .env
```

Минимальные переменные:

```properties
APP_JWT_SECRET=dev-jwt-secret-change-in-production-min-32-chars
APP_SERVICE_JWT_SECRET=dev-service-jwt-secret-change-in-production-min-32b
APP_SEED_ADMIN_PASSWORD=admin123
APP_SEED_USER_PASSWORD=user123
```

### 2. Сборка

```bash
./gradlew build
```

### 3. Запуск

**Legacy** (прямой HTTPS):

```bash
docker compose up -d --build
```

**Cloud** (Gateway + BFF):

```bash
docker compose --profile cloud up -d --build
```

**Observability** (Prometheus, Grafana, Zipkin, Loki):

```bash
docker compose --profile observability up -d --build
```

### 4. Проверка

```bash
./scripts/platform-smoke.sh          # legacy
./scripts/platform-smoke-cloud.sh    # cloud
```

### 5. Остановка

```bash
docker compose --profile cloud down
docker compose down -v   # с удалением томов
```

## Структура репозитория

```text
user-notification-platform/
├── settings.gradle.kts
├── build.gradle.kts
├── .gitlab-ci.yml              # CI / CD / CT
├── platform-commons/
├── config-server/
├── config-repo/
├── api-gateway/
├── web-bff/
├── user-service/
├── notification-service/
├── deploy/helm/platform/       # Helm chart (Kubernetes)
├── docker-compose.yml
├── scripts/                    # ci.sh, smoke, k8s
├── docs/
└── infra/                      # TLS, Kafka certs, observability
```

## Kubernetes

```bash
make k8s-create
# build + kind load images (см. docs/KUBERNETES.md)
make k8s-install
make k8s-smoke    # Gateway :18080, BFF :18090
```

Документация: [docs/KUBERNETES.md](docs/KUBERNETES.md)

## CI/CD

Пайплайн в GitLab: [`.gitlab-ci.yml`](.gitlab-ci.yml), документация: [docs/GITLAB.md](docs/GITLAB.md).

| Стадия | Что проверяется |
|--------|-----------------|
| **CI** | `./gradlew check`, Helm lint |
| **CT** | E2E smoke (Compose + K8s post-deploy) |
| **CD** | Push образов в GitLab Registry → Helm deploy |

Локально:

```bash
make ci-fast          # тесты
make ci-e2e           # compose + smoke
make ci-e2e-cloud     # gateway smoke
make ci-full          # полный цикл
```

## Безопасность

| Слой | Legacy | Cloud |
|------|--------|-------|
| Клиент → сервис | HTTPS :8443/:8444 | HTTP Gateway :8080 (dev) |
| Gateway → микросервисы | — | HTTPS :8443 |
| Пользовательский API | JWT (HS256) | JWT на Gateway + TokenRelay |
| Сервис → сервис | Service JWT + HTTPS | Service JWT |
| Асинхронно | Kafka (SASL_SSL опционально) | Kafka |

## Ключевые паттерны

- **Database per service** — PostgreSQL / MongoDB
- **Transactional Outbox** — `notification_outbox` → Kafka → notification-service
- **Saga compensation** — DLT → `notification-compensations` → user-service помечает `notificationDeliveryStatus=FAILED`
- **Outbox replay** — FAILED записи автоматически возвращаются в PENDING
- **Strangler** — legacy direct + cloud Gateway параллельно
- **Observability** — Micrometer, Zipkin, Loki, Grafana

## Автор

[charset-8utf](https://github.com/charset-8utf)
