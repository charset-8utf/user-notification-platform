# notification-service – микросервис email-уведомлений

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green?logo=springsecurity)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

## Описание

Микросервис отправки email-уведомлений о событиях пользователей (**создание** / **удаление** аккаунта).

- **Write:** REST (`POST /api/notifications/email`) и Kafka (`user-notifications`) — service JWT или API Key.
- **Read:** REST (`GET /api/notifications/logs/latest`) — user JWT (свой email или роль `ADMIN`).
- **Аудит:** MongoDB (`notification_logs`, `notification_inbox`).
- **SMTP:** Mailpit в dev (`localhost:1025`, UI — `http://localhost:8025`).
- **Redis** (опционально): обогащение из ключей `user:email:{email}`.

Стек: **Java 21**, **Spring Boot 4**, **Spring Kafka** (JSON по умолчанию, опционально Avro), **Spring Data MongoDB**, **Spring Data Redis**, **MapStruct**, **JSpecify** + **NullAway**.

Модуль входит в монорепозиторий [`user-notification-platform`](../README.md).  
REST с edge: **api-gateway** (`/api/notifications/**`) или напрямую **HTTPS** `:8444` (проброс с контейнерного `:8443`).

Зависимости модулей: `:platform-commons`, `:kafka-contracts`.

---

## Входящие события

Формат JSON (REST и Kafka):

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "operation": "USER_CREATED",
  "email": "user@example.com"
}
```

| `operation`    | Письмо           |
|----------------|------------------|
| `USER_CREATED` | «Аккаунт создан» |
| `USER_DELETED` | «Аккаунт удалён» |

### Kafka-поток

```text
UserNotificationKafkaConsumer → notification_inbox (PENDING)
                              → KafkaInboxRelay (atomic claim → PROCESSING)
                              → NotificationService → email → PROCESSED
                                                         ↘ FAILED → notification-compensations
DLT (user-notifications.DLT) → UserNotificationDltListener → compensation topic
```

- **Manual ack** после записи в inbox.
- **Идемпотентность** по `eventId` — дубликаты с `InboxStatus.PROCESSED` не обрабатываются повторно.
- Зависшие `PROCESSING` (старше `stale-processing-timeout-ms`) возвращаются в `PENDING`.
- Сериализация: `app.kafka.serialization=json` (по умолчанию) или `avro` (+ Schema Registry).

---

## API

| Метод | Путь                                | Auth                         | Описание                                      |
|-------|-------------------------------------|------------------------------|-----------------------------------------------|
| POST  | `/api/notifications/email`          | Service JWT или `X-API-Key`  | Отправить уведомление → **204**               |
| GET   | `/api/notifications/logs/latest`    | User JWT (`USER` / `ADMIN`)  | Последний лог по `?email=` → JSON summary     |
| GET   | `/actuator/health`                  | —                            | Health; при `kafka` — `kafkaConsumerLag`      |
| GET   | `/actuator/info`                    | —                            | Info                                          |
| GET   | `/actuator/prometheus`              | —                            | Метрики Prometheus                            |
| GET   | `/swagger-ui.html`                  | — (dev)                      | OpenAPI UI (springdoc)                        |

Путь write-эндпоинта настраивается: `app.notification.api.email-path` (используется в контроллере и `SecurityConfig`).

### Пример: отправка email

```bash
curl -k -X POST https://localhost:8444/api/notifications/email \
  -H 'Authorization: Bearer <service-jwt>' \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": "990e8400-e29b-41d4-a716-446655440099",
    "operation": "USER_CREATED",
    "email": "user@example.com"
  }'
```

Ответы: **204** — успех, **400** — валидация, **401** / **403** — auth, **503** — ошибка SMTP.

### Пример: последний лог

```bash
curl -k 'https://localhost:8444/api/notifications/logs/latest?email=user@example.com' \
  -H 'Authorization: Bearer <user-jwt>'
```

Пользователь видит только свой email; `ADMIN` — любой. **403**, если email в query не совпадает с claim `email` в JWT.

---

## Spring-профили

| Профиль      | Назначение                                                        |
|--------------|-------------------------------------------------------------------|
| `rest`       | `NotificationController`, `NotificationLogController`             |
| `kafka`      | Consumer, inbox relay, DLT, idempotency, lag health, compensation |
| `redis`      | `RedisUserLookupPort`                                             |
| `management` | Actuator на отдельном порту **8081** (без TLS)                    |

Дефолт в `application.yml`: **`kafka`**.

| Сценарий              | `SPRING_PROFILES_ACTIVE`      |
|-----------------------|-------------------------------|
| Только Kafka          | `kafka`                       |
| Kafka + REST          | `rest,kafka`                  |
| Kafka + Redis         | `kafka,redis`                 |
| Полный локальный стек | `rest,kafka,redis,management` |

Без `redis` Redis auto-config отключён (`application.yml`).

---

## Безопасность

Две цепочки `SecurityFilterChain` (`config/security/SecurityConfig.java`):

| Цепочка | Matcher                         | Механизм                                              |
|---------|---------------------------------|-------------------------------------------------------|
| Read    | `/api/notifications/logs/**`    | User JWT (`APP_JWT_*`), роли `USER` / `ADMIN`         |
| Write   | остальное API + actuator rules  | Service JWT (`APP_SERVICE_JWT_*`) или API Key         |

| Механизм        | Описание                                                                     |
|-----------------|------------------------------------------------------------------------------|
| **HTTPS**       | TLS **8443** в контейнере; хост **8444** (`NOTIFICATION_SERVICE_HTTPS_PORT`) |
| **Service JWT** | Scope `notifications:write`; отклонение user access-токенов                  |
| **API Key**     | `APP_API_KEY_ENABLED=true`, заголовок `X-API-Key`                            |
| **Kafka**       | PLAINTEXT в dev; SASL_SSL — `APP_KAFKA_SECURITY_ENABLED=true`                |

Actuator (`health`, `info`, `prometheus`) — без auth на write-цепочке. С профилем `management` слушает **8081**.

---

## Архитектура

```text
controller/          REST (профиль rest)
dto/                 API records (JSpecify @NullMarked)
domain/              enum'ы: operation, channel, status, inbox status
service/
  port/              EmailDeliveryPort, UserLookupPort
  email/             Strategy — subject/body по operation
  …                  Facade, Template Method, inbox, idempotency, log query
mapper/              MapStruct (NotificationLogMapper + DetailResolver)
entity/              NotificationLog, NotificationInbox (@Document)
repository/          Spring Data + atomic inbox claim (custom impl)
delivery/            JavaMailEmailDeliveryAdapter
lookup/              RedisUserLookupPort / NoOpUserLookupPort
kafka/
  json/, avro/       serializers / consumer factory builders
  …                  consumer, inbox relay, DLT, compensation, lag monitor
config/
  kafka/             topics, producers, consumers, typed properties
  security/          SecurityFilterChain
security/            JWT decoders, API Key filter, access policies
metrics/, exception/
```

Паттерны: **Facade**, **Template Method**, **Strategy**, **Adapter**, **Transactional Inbox**.

Конфигурация — `@ConfigurationProperties` records, без `@Value` в main-коде.  
Опционально: Spring Cloud Config (`CONFIG_SERVER_URI`, профиль `cloud`).

---

## Быстрый старт

### Требования

- **Java 21** (Gradle JVM — см. комментарий в корневом `gradle.properties`, если default JDK > 21)
- **Docker** (compose и Testcontainers)
- `./gradlew` из корня `user-notification-platform`

### 1. Инфраструктура

```bash
git clone https://github.com/charset-8utf/user-notification-platform.git
cd user-notification-platform
docker compose up -d
```

Опционально `.env` (см. `.env.example`): `KEYSTORE_PASSWORD`, `APP_SERVICE_JWT_SECRET`, `SPRING_PROFILES_ACTIVE=rest,kafka,redis,management`.

### 2. Приложение локально

```bash
./gradlew :notification-service:bootRun
```

Или в Docker (из корня monorepo):

```bash
docker compose up -d --build notification-service
```

| Сервис        | URL                                      |
|---------------|------------------------------------------|
| HTTPS API     | https://localhost:8444                   |
| Mailpit UI    | http://localhost:8025                    |
| Actuator      | http://localhost:8081/actuator/health    |

### 3. Service JWT для write

Секрет `APP_SERVICE_JWT_SECRET` (≥32 символа) должен совпадать у сервисов-вызывателей.

```bash
./gradlew :user-service:serviceJwtSmokeToken -q
```

### 4. Проверка

```bash
curl -k https://localhost:8444/actuator/health
```

---

## Тестирование

```bash
./gradlew :notification-service:check
```

| Тип          | Примеры                                                                 |
|--------------|-------------------------------------------------------------------------|
| Unit / slice | `*WebMvcTest`, `NotificationLogMapperTest`, `KafkaInboxRelayTest`       |
| Integration  | `NotificationKafka*IntegrationTest`, `NotificationEmailIntegrationTest` |

Testcontainers: MongoDB, Kafka. Покрытие JaCoCo — минимум **70%** instructions (`check`).

---

## CI

Монорепозиторий: [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) — `./gradlew check` на JDK 21.  
Отдельного workflow для notification-service нет.

---

## Конфигурация

| Префикс                  | Record                        | Назначение                              |
|--------------------------|-------------------------------|-----------------------------------------|
| `app.notification`       | `NotificationProperties`      | `site-name`, `mail-from`                |
| `app.notification.api`   | `NotificationApiProperties`   | `email-path`                            |
| `app.notification.kafka` | `NotificationKafkaProperties` | topic, inbox, retry, listener, DLT      |
| `app.kafka`              | `AppKafkaProperties`          | `serialization`, schema-registry, SASL  |
| `app.security.*`         | JWT / `ApiKeyProperties`      | auth                                    |

Профили: `application-kafka.yml`, `application-redis.yml`, `application-management.yml`, `application-cloud.yml`.  
Централизованные overrides — [`config-repo/`](../config-repo/).

---

## Автор

[charset-8utf](https://github.com/charset-8utf)
