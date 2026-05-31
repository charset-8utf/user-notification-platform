# user-notification-platform – платформа микросервисов (Spring Cloud)

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.1-green?logo=spring)
![Eureka](https://img.shields.io/badge/Eureka-Discovery-blue)
![Resilience4j](https://img.shields.io/badge/Resilience4j-2.4.0-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue?logo=apachemaven)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

## Описание проекта

Монорепозиторий **учебной платформы** вокруг двух бизнес-сервисов:

- **user-service** — CRUD пользователей, JWT, outbox → Kafka
- **notification-service** — email-уведомления (REST + Kafka), MongoDB

Платформенные модули на **Spring Cloud Oakwood** (Spring Boot 4) реализуют паттерны лекции по микросервисам:

| Паттерн                                    | Реализация                                                                |
|--------------------------------------------|---------------------------------------------------------------------------|
| **API Gateway**                            | `api-gateway/` — north-south edge, JWT, rate limit, TokenRelay            |
| **Service Discovery**                      | `eureka-server/` + Eureka Client в сервисах, `lb://` в маршрутах          |
| **Circuit Breaker**                        | Resilience4j на Gateway и в `NotificationRestClient` (user-service)       |
| **Externalized Configuration**             | `config-server/` + `config-repo/`                                         |
| **Backend for Frontend (API Composition)** | `web-bff/` — агрегация `/bff/me`                                          |
| **Chassis / shared library**               | `platform-commons/` — metrics, tracing defaults, Eureka defaults          |
| **Observability**                          | Prometheus, Grafana, Zipkin, Loki + Promtail (профиль `observability`)    |
| **Strangler Application**                  | legacy (`:8443` / `:8444`) и cloud (Gateway) в одном `docker-compose.yml` |

Бизнес-сервисы (`user-service/`, `notification-service/`) — отдельные git-репозитории; в этой папке они лежат рядом для удобства IDE и задания.

## Точки входа

| Режим                         | Куда ходить                   | Порт                   |
|-------------------------------|-------------------------------|------------------------|
| **Legacy** (по умолчанию)     | user-service напрямую         | https://localhost:8443 |
| **Legacy**                    | notification-service напрямую | https://localhost:8444 |
| **Cloud** (`--profile cloud`) | API Gateway                   | http://localhost:8080  |
| **Cloud**                     | web-bff                       | http://localhost:8090  |
| Config Server                 | внешняя конфигурация          | http://localhost:8888  |
| Eureka Dashboard              | service registry              | http://localhost:8761  |

### Маршруты API Gateway (профиль `cloud`)

| Маршрут                                              | Сервис                      | Фильтры                                       |
|------------------------------------------------------|-----------------------------|-----------------------------------------------|
| `/api/auth/**`                                       | `lb://user-service`         | Rate limit по IP                              |
| `/api/users/**`, `/api/roles/**`, `/api/profiles/**` | `lb://user-service`         | JWT, TokenRelay, Circuit Breaker, Retry (GET) |
| `/api/notifications/logs/**`                         | `lb://notification-service` | JWT, TokenRelay, Circuit Breaker              |

### BFF

| Метод | Путь      | Описание                                              |
|-------|-----------|-------------------------------------------------------|
| GET   | `/bff/me` | Агрегация user + profile + последний лог notification |

Подробнее по API бизнес-сервисов: [`user-service/README.md`](user-service/README.md), [`notification-service/README.md`](notification-service/README.md).

## Требования к окружению

- **Docker Desktop** (полный стек, E2E, Testcontainers)
- **Java 21**
- **Maven 3.9+**
- Перед первым `docker compose up` (Kafka SASL_SSL):  
  `infra/tls/generate-dev-certs.sh` и `infra/kafka/generate-kafka-certs.sh`

## Быстрый старт через Docker

### 1. Клонирование и открытие в IDE

```bash
git clone <url-репозитория-платформы>
cd user-notification-platform
```

### 2. Настройка переменных окружения

```bash
cp .env.example .env
```

Минимально важные значения:

```properties
APP_JWT_SECRET=dev-jwt-secret-change-in-production-min-32-chars
APP_SERVICE_JWT_SECRET=dev-service-jwt-secret-change-in-production-min-32b
APP_SEED_ADMIN_PASSWORD=admin123
APP_SEED_USER_PASSWORD=user123
DB_USER=postgres
DB_PASSWORD=postgres
KEYSTORE_PASSWORD=changeit
```

> Без `APP_SEED_*` user-service не создаст учётки — API вернёт **401**.

### 3. Сборка platform-модулей (первый раз после клона)

```bash
mvn install -pl platform-commons,config-server,eureka-server,api-gateway,web-bff -am
```

### 4. Запуск стека

**Legacy** — прямой HTTPS к сервисам:

```bash
docker compose up -d --build
```

- user-service: **https://localhost:8443**
- notification-service: **https://localhost:8444**
- Mailpit UI: **http://localhost:8025**

**Cloud** — Gateway + BFF + Eureka + Config:

```bash
# В .env добавьте профиль cloud к сервисам, например:
# USER_SERVICE_PROFILES=kafka,redis,jwt,management,docker,cloud
# NOTIFICATION_SERVICE_PROFILES=rest,kafka,redis,management,docker,cloud

docker compose --profile cloud up -d --build
```

- API Gateway: **http://localhost:8080**
- web-bff: **http://localhost:8090**

Legacy-режим (`:8443` / `:8444`) остаётся доступен параллельно — трафик постепенно переводится на Gateway.

**Observability** (метрики, tracing, логи):

```bash
docker compose --profile observability up -d --build
```

| Компонент  | URL                   |
|------------|-----------------------|
| Prometheus | http://localhost:9091 |
| Grafana    | http://localhost:3000 |
| Zipkin     | http://localhost:9411 |

### 5. Проверка

Smoke всей платформы (legacy):

```bash
./scripts/platform-smoke.sh
```

Smoke через Gateway (cloud):

```bash
./scripts/platform-smoke-cloud.sh
```

JWT login (legacy):

```bash
curl -k -X POST https://localhost:8443/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

JWT через Gateway (cloud):

```bash
curl -fsS -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

### 6. Остановка и очистка

```bash
docker compose --profile cloud down
docker compose down -v   # с удалением томов БД
```

## Состав репозитория

```text
user-notification-platform/
├── pom.xml                  # Parent BOM (Spring Cloud, общие версии, JaCoCo)
├── platform-commons/        # Chassis: metrics, tracing defaults, Eureka defaults
├── config-server/           # Spring Cloud Config Server (:8888)
├── config-repo/             # YAML для Native/Git backend
├── eureka-server/           # Netflix Eureka (:8761)
├── api-gateway/             # Spring Cloud Gateway (:8080)
├── web-bff/                 # Backend for Frontend (:8090)
├── user-service/            # Бизнес-сервис (отдельный git)
├── notification-service/    # Бизнес-сервис (отдельный git)
├── docker-compose.yml       # Postgres, Mongo, Kafka, Redis, Mailpit, сервисы
├── .env.example
├── scripts/                 # platform-smoke.sh, kafka load test
└── infra/                   # TLS, Kafka certs, observability
```

## Архитектура платформы

```text
Клиент
  ├─ legacy ──► user-service :8443 / notification-service :8444
  └─ cloud  ──► api-gateway :8080 ──lb://──► user-service
                              │              notification-service
                              └──► web-bff :8090 (агрегация /bff/me)

config-server ◄── pull ── user-service, notification-service, gateway, bff
eureka-server ◄── register ── все Spring Cloud клиенты

user-service ── outbox ──► Kafka ──► notification-service ──► Mailpit / SMTP
```

## Сборка и локальная разработка

Только platform-модули:

```bash
mvn clean install -pl platform-commons,config-server,eureka-server,api-gateway,web-bff -am
```

Бизнес-сервисы (из корня или из каталога сервиса):

```bash
mvn -f user-service/pom.xml clean package -DskipTests
mvn -f notification-service/pom.xml clean package -DskipTests
```

Инфраструктура в Docker, приложение через Maven:

```bash
docker compose up -d postgres mongo redis kafka mailpit config-server eureka-server
cd user-service && mvn spring-boot:run
```

## Тестирование

### Platform-модули

```bash
mvn test -pl api-gateway,platform-commons
```

### Бизнес-сервисы

| Сервис               | Unit                                       | Unit + IT + E2E                              |
|----------------------|--------------------------------------------|----------------------------------------------|
| user-service         | `mvn -f user-service/pom.xml test`         | `mvn -f user-service/pom.xml verify`         |
| notification-service | `mvn -f notification-service/pom.xml test` | `mvn -f notification-service/pom.xml verify` |

**user-service:** порог JaCoCo **80%** инструкций по пакетам (кроме `com.crud`, `com.crud.entity`, `com.crud.util`).

Интеграционные тесты требуют **Docker** (Testcontainers).

### E2E в CI

Workflow `.github/workflows/PlatformE2E.yml` — сборка jar, `docker compose up`, healthcheck, smoke JWT/Kafka/Mailpit, Prometheus.

## CI

| Workflow                    | Назначение                                  |
|-----------------------------|---------------------------------------------|
| `PlatformE2E.yml`           | E2E smoke всей платформы в docker compose   |
| `UserServiceCI.yml`         | CI user-service (в его репозитории)         |
| `NotificationServiceCI.yml` | CI notification-service (в его репозитории) |

## Особенности реализации

- **Parent POM** — единые версии Spring Cloud, Resilience4j 2.4 (`spring-boot4`), overrides транзитивных CVE (Bouncy Castle, httpclient, Eureka)
- **Database per service** — PostgreSQL (user) / MongoDB (notification), без shared DB
- **Transactional Outbox** — `notification_outbox` → `KafkaOutboxRelay` → топик `user-notifications`
- **Saga (choreography)** — DLT → `notification-compensations` → user-service
- **Три слоя безопасности** — user JWT (Gateway + user-service), service JWT (REST east-west), Kafka SCRAM (compose)
- **Strangler** — `--profile cloud` не ломает legacy `:8443` / `:8444`
- **Observability** — Micrometer, Zipkin (W3C), Loki + Promtail, дашборд Grafana «Platform Overview»
- **BFF** — `GET /bff/me` через Gateway в cloud-режиме

## Автор

[charset-8utf](https://github.com/charset-8utf)
