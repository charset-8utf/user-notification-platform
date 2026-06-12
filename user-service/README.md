# user-service – REST-сервис управления пользователями

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green?logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![Hibernate](https://img.shields.io/badge/Hibernate-7.3.2.Final-purple?logo=hibernate)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)
![Liquibase](https://img.shields.io/badge/Liquibase-4.x-red?logo=liquibase)
![Caffeine](https://img.shields.io/badge/Caffeine-Cache-brightgreen)
![JUnit](https://img.shields.io/badge/JUnit%20Jupiter-6.0.3-green)
![Mockito](https://img.shields.io/badge/Mockito-5.23.0-orange)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
[![CI](https://github.com/charset-8utf/user-notification-platform/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/charset-8utf/user-notification-platform/actions/workflows/ci.yml)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

## Описание проекта

REST-сервис для управления пользователями с поддержкой операций **Create**, **Read**, **Update**, **Delete** (CRUD).  
Построен на **Spring Boot 4** с использованием **Spring Data JPA**, **PostgreSQL** в Docker и пула соединений **HikariCP**.  
**Архитектура:** трёхслойная (Controller → Service → Repository) с DTO и **MapStruct**-мапперами.

Схема БД управляется миграциями **Liquibase**: точка входа — `src/main/resources/db/changelog/db.changelog-master.yaml`, изменения — SQL-файлы в `src/main/resources/db/changelog/changes/` (*Liquibase formatted sql*).  
Покрыт юнит-тестами (JUnit, Mockito) и интеграционными тестами (Testcontainers, H2).  
CI/CD выполняется в GitHub Actions (монорепозиторий `user-notification-platform`).

При локальном запуске через `docker compose` из **корня monorepo** поднимаются **PostgreSQL**, **Redis**, **Kafka (KRaft)**, **Schema Registry**; приложение доступно по **HTTPS** на порту **8443**.

В составе платформы [`user-notification-platform`](../README.md) тот же сервис также доступен через **API Gateway** (`http://localhost:8080`) в профиле `cloud` — см. [platform README](../README.md).

При создании и удалении пользователя сервис публикует события во **notification-service** (профили `kafka` или `rest`).  
При сбое доставки email notification-service отправляет **compensation-событие** в Kafka (`notification-compensations`); user-service выполняет **choreography saga** — откат создания пользователя или signal-only для удаления.

## API-эндпоинты

| Метод  | Путь                                | Описание                            |
|--------|-------------------------------------|-------------------------------------|
| POST   | `/api/auth/login`                   | Выдача JWT (access + refresh)       |
| POST   | `/api/auth/refresh`                 | Обновление пары токенов             |
| POST   | `/api/auth/logout`                  | Отзыв refresh-токена                |
| POST   | `/api/users`                        | Создание пользователя               |
| GET    | `/api/users/{id}`                   | Получение пользователя по ID        |
| GET    | `/api/users`                        | Список пользователей (с пагинацией) |
| PUT    | `/api/users/{id}`                   | Обновление пользователя             |
| DELETE | `/api/users/{id}`                   | Удаление пользователя               |
| GET    | `/api/users/by-email?email=`        | Поиск по email                      |
| GET    | `/api/users/search?email=`          | Поиск по части email                |
| POST   | `/api/users/{userId}/notes`         | Создание заметки                    |
| GET    | `/api/users/{userId}/notes`         | Список заметок пользователя         |
| GET    | `/api/users/{userId}/notes/{id}`    | Получение заметки                   |
| PUT    | `/api/users/{userId}/notes/{id}`    | Обновление заметки                  |
| DELETE | `/api/users/{userId}/notes/{id}`    | Удаление заметки                    |
| POST   | `/api/profiles/user/{userId}`       | Создание профиля                    |
| GET    | `/api/profiles/user/{userId}`       | Получение профиля пользователя      |
| GET    | `/api/profiles`                     | Список профилей (с пагинацией)      |
| PUT    | `/api/profiles/user/{userId}`       | Обновление профиля                  |
| DELETE | `/api/profiles/user/{userId}`       | Удаление профиля                    |
| POST   | `/api/roles`                        | Создание роли                       |
| GET    | `/api/roles/{id}`                   | Получение роли по ID                |
| GET    | `/api/roles`                        | Список ролей (с пагинацией)         |
| PUT    | `/api/roles/{id}`                   | Обновление роли                     |
| DELETE | `/api/roles/{id}`                   | Удаление роли                       |
| POST   | `/api/roles/assign?userId=&roleId=` | Назначение роли пользователю        |
| POST   | `/api/roles/remove?userId=&roleId=` | Снятие роли с пользователя          |

## Требования к окружению

- **Docker Desktop** (для PostgreSQL, Redis, Kafka и интеграционных тестов)
- **Java 21** (Gradle toolchain в корневом `build.gradle.kts`)
- **Gradle 8.14+** — wrapper из корня `user-notification-platform` (`./gradlew`)

## Быстрый старт через Docker

### 1. Клонирование репозитория

```bash
git clone https://github.com/charset-8utf/user-notification-platform.git
cd user-notification-platform
cp .env.example .env
```

### 2. Переменные окружения

Скопируйте `.env.example` в `.env` (см. шаг 1). Ключевые значения:

```properties
DB_USER=postgres
DB_PASSWORD=postgres
KEYSTORE_PASSWORD=changeit
APP_SEED_ADMIN_PASSWORD=admin123
APP_SEED_USER_PASSWORD=user123
SPRING_PROFILES_ACTIVE=kafka,redis,jwt
APP_JWT_SECRET=dev-jwt-secret-change-in-production-min-32-chars
APP_HTTPS_PORT=8443
POSTGRES_PUBLISH_PORT=5432
```

Порты приложения и Postgres на хосте можно переопределить через `APP_HTTPS_PORT` и `POSTGRES_PUBLISH_PORT`.

> **Важно:** без `APP_SEED_ADMIN_PASSWORD` и `APP_SEED_USER_PASSWORD` seed-учётки не создаются — API вернёт **401**.

### 3. Запуск PostgreSQL, Redis, Kafka и приложения

Из **корня** monorepo:

При первом запуске нужна сборка образа:

```bash
docker compose up --build -d
```

Повторный старт: `docker compose up -d`.

Приложение: **https://localhost:8443** (если не меняли `APP_HTTPS_PORT`).

**Режим разработки** (инфраструктура в Docker, приложение локально) — из **корня** `user-notification-platform`:

```bash
./gradlew :user-service:bootRun
```

Полностью новая БД (сброс тома Postgres и миграций Liquibase):

```bash
docker compose down -v && docker compose up --build -d
```

В compose также поднимаются **Schema Registry** (`:8085`) и **Avro** по умолчанию для Kafka (`APP_KAFKA_SERIALIZATION=avro`). См. [platform README](../README.md#kafka).

> Браузер покажет предупреждение о самоподписанном сертификате. Для API удобнее **curl** (`-k`).

### 4. Аутентификация

По умолчанию активен профиль **`jwt`**: access/refresh JWT (HS256).

```bash
curl -k -X POST https://localhost:8443/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

Ответ: `accessToken`, `refreshToken`, `expiresIn`. Дальнейшие запросы: `Authorization: Bearer <accessToken>`.

| Переменная            | По умолчанию    |
|-----------------------|-----------------|
| `APP_JWT_SECRET`      | мин. 32 символа |
| `APP_JWT_ACCESS_TTL`  | `PT15M`         |
| `APP_JWT_REFRESH_TTL` | `P7D`           |

Опционально профиль **`local`** — HTTP Basic поверх JWT (dev):  
`SPRING_PROFILES_ACTIVE=kafka,redis,jwt,local`.

Seed-учётки:

| Логин   | Пароль (из .env)          | Роль         |
|---------|---------------------------|--------------|
| `admin` | `APP_SEED_ADMIN_PASSWORD` | ADMIN + USER |
| `user`  | `APP_SEED_USER_PASSWORD`  | USER         |

### 5. Проверка

Health (без авторизации, management-порт внутри контейнера — при пробросе `8081`):

```bash
curl -k https://localhost:8443/actuator/health
```

Список пользователей (JWT):

```bash
TOKEN=$(curl -ks -X POST https://localhost:8443/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
curl -k -H "Authorization: Bearer $TOKEN" https://localhost:8443/api/users
```

### 6. Остановка и очистка

```bash
docker compose down
docker compose down -v
```

## Безопасность

| Механизм            | Описание                                                                           |
|---------------------|------------------------------------------------------------------------------------|
| **HTTPS**           | TLS 1.2/1.3 на порту **8443** (`keystore.p12`); management — **8081**              |
| **User JWT**        | Профиль `jwt`: login/refresh/logout, HS256, rate limit на `/api/auth/*`            |
| **Service JWT**     | Профиль `rest`: исходящие вызовы `notification-service` (`notifications:write`)    |
| **Kafka**           | По умолчанию PLAINTEXT в dev; **SASL_SSL** — `APP_KAFKA_SECURITY_ENABLED=true`     |
| **Cloud / Gateway** | Клиент ходит на HTTP Gateway; до user-service — HTTPS с dev trust (`insecure-ssl`) |

Порт **9090** в compose зарезервирован под будущий **gRPC** — в текущей версии не используется.

## Исходящие уведомления (опционально)

Формат события:

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "operation": "USER_CREATED",
  "email": "user@example.com"
}
```

| Профиль | Канал                                                                       |
|---------|-----------------------------------------------------------------------------|
| `kafka` | Transactional outbox → топик `user-notifications` (partition key = `email`) |
| `rest`  | Синхронный `POST /api/notifications/email` с **service JWT** и HTTPS        |

Профили **`kafka` и `rest` взаимоисключающие** — не включайте оба одновременно.

Параллельно (профиль `redis`) в Redis кэшируется срез пользователя: ключ `user:{id}`, TTL — `app.cache.redis.ttl` (по умолчанию `PT1H`). Refresh-токены JWT хранятся в Redis (`jwt & redis`) или in-memory (`jwt & !redis`).

## Saga compensation (входящие события)

При ошибке доставки email **notification-service** публикует JSON в топик **`notification-compensations`**. user-service (профиль `kafka`) потребляет их через `NotificationCompensationConsumer`:

| Операция       | Действие                                                   |
|----------------|------------------------------------------------------------|
| `USER_CREATED` | **Rollback** — удаление пользователя и eviction Redis-кэша |
| `USER_DELETED` | **Signal-only** — пользователь уже удалён, метрика + лог   |

Профиль **`rest`**: тот же сценарий синхронно через `NotificationDeliveryFailureRecorder` при ошибке HTTP-вызова.

E2E сценарий платформы: `make e2e-compensation` (из корня monorepo).  
Подробнее о публикации compensation: [notification-service/README.md](../notification-service/README.md).

Метрики: `app.notification.compensation.*` (Micrometer). Операционные логи compensation и security — на **русском языке**.

## Локальный запуск с PostgreSQL в контейнере

```bash
docker run --name user-postgres \
  -e POSTGRES_DB=userdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:17-alpine

# из корня user-notification-platform
./gradlew :user-service:bootJar
java -jar user-service/build/libs/user-service-1.0.0.jar
```

Или:

```bash
APP_SEED_ADMIN_PASSWORD=admin123 APP_SEED_USER_PASSWORD=user123 \
  SPRING_PROFILES_ACTIVE=kafka,redis,jwt \
  ./gradlew :user-service:bootRun
```

## Архитектура проекта

```text
com.crud
├── config/           # общая конфигурация, properties
│   ├── kafka/        # producers, consumers, topics, SASL/SSL
│   ├── security/     # JWT, HTTP security, authorization
│   ├── ratelimit/    # Chain of Responsibility для ключей rate limit
│   └── rest/         # REST-клиент к notification-service
├── controller/   # REST API, AuthController
├── service/      # бизнес-логика, retry при optimistic lock
├── repository/   # Spring Data JPA
├── entity/       # JPA-сущности, @Version, L2 cache
├── dto/          # DTO и auth-запросы
├── mapper/       # MapStruct: DTO ↔ Entity (@MapperConfig)
├── notification/ # события, outbox, Kafka/REST, compensation
│   ├── kafka/        # UserNotificationKafkaProducer
│   ├── outbox/       # notification_outbox, KafkaOutboxRelay
│   └── compensation/ # NotificationCompensationConsumer, UserCompensationService
├── cache/        # Redis UserCachePort
├── exception/    # @RestControllerAdvice
└── security/     # JWT, AuthService, ApiOutputSanitizer
```

## Тестирование

### Запуск тестов

Только unit + integration (из корня платформы):

```bash
./gradlew :user-service:check
```

Из каталога `user-service` (если открыт как модуль):

```bash
./gradlew check
```

Интеграционные тесты используют профили **`test`**, **`it`**, **`jwt`** (`application-test.yml`, `application-it.yml`) и **H2** / **Testcontainers** (нужен Docker).

### Типы тестов

**Модульные (JUnit + Mockito):** сервисы, контроллеры, мапперы, JWT, исключения.

**Интеграционные (H2 / Testcontainers):** репозитории, Kafka/outbox, REST resilience, auth.

### Покрытие кода (JaCoCo)

Порог **80% инструкций** для пакетов, кроме `com.crud` и `com.crud.entity`.

Тесты запускаются в GitHub Actions при push и pull request.

### Ручная проверка API (curl)

Примеры — в разделе «Проверка» выше; для полного стека платформы: `./scripts/platform-smoke.sh` из корня `user-notification-platform`.

### Service JWT для smoke / ручных вызовов notification REST

```bash
# из корня user-notification-platform
./gradlew :user-service:serviceJwtSmokeToken -q
```

## CI

В monorepo платформы: `./gradlew :user-service:check` (workflow [`ci.yml`](../.github/workflows/ci.yml), JDK **21**).  
E2E и compensation: [`e2e.yml`](../.github/workflows/e2e.yml), `make e2e-compensation`.

## Сборка (`build.gradle.kts`)

- Зависимости сгруппированы по конфигурациям (`implementation`, `testImplementation`, `errorprone`, …)
- Версии вне Spring BOM — через каталог [`gradle/libs.versions.toml`](../gradle/libs.versions.toml) (`libs.*`)
- **Error Prone** + **NullAway** на этапе компиляции; плагин — `alias(libs.plugins.errorprone)`
- Java **21** — toolchain из корневого `build.gradle.kts`

## Логирование

**SLF4J** (Spring Boot). Уровни и шаблон консоли — в `application.yml`.

## Особенности реализации

- **5 сущностей:** User, Profile, Note, Role, Credential (связи OneToOne / OneToMany / ManyToMany)
- **Spring Security** — JWT (`jwt`), опционально HTTP Basic (`local`), роли USER / ADMIN
- **HTTPS** — TLS 1.2/1.3, PKCS12, порт 8443; профиль `management` — actuator на порту **8081**
- **Оптимистичные блокировки** — `@Version` + `@Retryable` (3 попытки, backoff 100 ms)
- **Кэш 2-го уровня** — Caffeine + JCache (Hibernate L2 + query cache)
- **Rate limiting** — по `sub` из JWT или IP для `/api/auth/*`
- **Санитизация ответов** — `ApiOutputSanitizer` в контроллерах
- **CORS** — настраиваемые allowed origins
- **Пагинация** — Spring Data Pageable (макс. 100)
- **Kafka outbox** — таблица `notification_outbox`, at-least-once дelivery; опционально SASL_SSL (`APP_KAFKA_SECURITY_ENABLED=true`)
- **Saga compensation** — consumer топика `notification-compensations`, идемпотентный rollback
- **REST к notification-service** — service JWT (`@DefaultValue` в `ServiceJwtProperties`), TLS truststore, Resilience4j + Bulkhead, `RestClient` → `https://notification-service:8443`
- **Liquibase** — `spring.jpa.hibernate.ddl-auto: validate`
- **Healthcheck** в Docker через Actuator

## Автор

[charset-8utf](https://github.com/charset-8utf)
