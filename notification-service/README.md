# notification-service – микросервис email-уведомлений

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-black?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Maven](https://img.shields.io/badge/Maven-3.9.14-blue?logo=apachemaven)
![JUnit](https://img.shields.io/badge/JUnit%20Jupiter-6.0.3-green)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

## Описание проекта

Микросервис отправки email-уведомлений о событиях пользователей (**создание** / **удаление** аккаунта).  
Принимает события по **REST** и из **Kafka**, пишет аудит в **MongoDB**, отправляет письма через SMTP (в dev — **Mailpit**).  
Опционально обогащает контекст из **Redis** (ключи `user:email:{email}`).

Стек: **Java 21**, **Spring Boot 4**, **Spring Kafka 4** (Jackson 3 / `JsonMapper`), **Spring Data MongoDB**, **Spring Data Redis** (Lettuce).  
Покрыт модульными и интеграционными тестами (Testcontainers). CI — GitHub Actions.

## Входящие события

Формат JSON (REST и Kafka):

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "operation": "USER_CREATED",
  "email": "user@example.com"
}
```

| `operation`     | Письмо              |
|-----------------|---------------------|
| `USER_CREATED`  | «Аккаунт создан»    |
| `USER_DELETED`  | «Аккаунт удалён»    |

**Kafka:** `UserNotificationKafkaConsumer` (manual ack, логирование topic/partition/offset/key), concurrency = числу partitions (по умолчанию 3), идемпотентность по `eventId` (профиль `kafka`), retry и DLT (`user-notifications.DLT`).

**REST:** `POST /api/notifications/email` → `204 No Content`. Требуется **service JWT** в `Authorization: Bearer <token>` (фаза 2).

**Redis (профиль `redis`):** чтение `user:email:{email}` (JSON: `id`, `email`, `status`) для debug-обогащения в логах; отправка письма не зависит от наличия кэша.

## Spring-профили

| Профиль | Назначение                                                                                                |
|---------|-----------------------------------------------------------------------------------------------------------|
| `rest`  | `NotificationController` — HTTP API                                                                       |
| `kafka` | `UserNotificationKafkaConsumer`, DLT, `NotificationIdempotencyService`, `KafkaConsumerLagHealthIndicator` |
| `redis` | `RedisUserLookupPort` (`application-redis.yml`)                                                           |

Дефолт в `application.yml`: **`kafka`**.

| Сценарий                              | `SPRING_PROFILES_ACTIVE` |
|---------------------------------------|--------------------------|
| Kafka + Redis lookup                  | `kafka,redis`            |
| Kafka + REST API (E2E, ручные вызовы) | `rest,kafka,redis`       |
| Без Redis                             | `kafka` или `rest,kafka` |

Профили **`rest` и `kafka`** можно комбинировать: один процесс обрабатывает и HTTP, и топик.

## Безопасность (фаза 2 — service JWT)

- `/api/notifications/email` — OAuth2 Resource Server (HS256): проверяются `iss`, `sub`, `aud` и scope `notifications:write`.
- Пользовательский access token с user-service **не принимается** (другой `iss` / claims).
- Без заголовка или с невалидным JWT — **401**; верный JWT без scope — **403**.
- `/actuator/health`, `/actuator/info`, `/actuator/prometheus` — без авторизации.

Для ручных вызовов выпустите токен (тот же `APP_SERVICE_JWT_SECRET`, что в compose):

```bash
cd ../user-service
export APP_SERVICE_JWT_SECRET=dev-service-jwt-secret-change-in-production-min-32b
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp "target/test-classes:target/classes:$(cat cp.txt)" com.crud.support.ServiceJwtSmokeToken
```

> Подключение к Mongo: **`spring.mongodb.uri`** / `SPRING_MONGODB_URI` (Spring Boot 4).

## Конфигурация

### Переменные окружения (основные)

| Переменная                                                  | Назначение                                                      | По умолчанию                                          |
|-------------------------------------------------------------|-----------------------------------------------------------------|-------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`                                    | Профили Spring                                                  | `kafka`                                               |
| `APP_SERVICE_JWT_SECRET`                                    | Секрет HS256 (≥32 байт), общий с user-service в профиле `rest`  | `dev-service-jwt-secret-change-in-production-min-32b` |
| `SPRING_MONGODB_URI`                                        | MongoDB                                                         | `mongodb://localhost:27017/notification`              |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS`                            | Kafka                                                           | `localhost:9092`                                      |
| `SPRING_DATA_REDIS_HOST` / `PORT` / `DATABASE` / `PASSWORD` | Redis (профиль `redis`)                                         | `localhost:6379`, DB `0`                              |
| `REDIS_PASSWORD`                                            | Пароль Redis                                                    | пусто                                                 |
| `APP_NOTIFICATION_SITE_NAME`                                | Имя сайта в тексте письма                                       | `ваш сайт`                                            |
| `APP_NOTIFICATION_MAIL_FROM`                                | From в SMTP                                                     | `noreply@localhost`                                   |
| `KEYSTORE_PASSWORD`                                         | PKCS12 для HTTPS (`keystore.p12`, alias `notification-service`) | `changeit`                                            |

Dev-сертификаты (SAN `notification-service`, `localhost`): `../infra/tls/generate-dev-certs.sh`
| `APP_HTTPS_PORT` | Порт на хосте (docker compose) | `8444` |

### Kafka (JSON)

Producer и consumer используют общий Spring bean **`JsonMapper`** (Jackson 3):

- producer: `JacksonJsonSerializer.noTypeInfo()`
- consumer: `JacksonJsonDeserializer<>(NotificationEmailRequest.class, jsonMapper, false)`

Сообщения в топике — чистый JSON **без** заголовков `__TypeId__`. Топик по умолчанию: `user-notifications` (3 partitions; partition key у producer — обычно `email`).

## API-эндпоинты

| Метод | Путь                       | Описание                                                       |
|-------|----------------------------|----------------------------------------------------------------|
| POST  | `/api/notifications/email` | Отправить уведомление (JSON с `eventId`, `operation`, `email`) |
| GET   | `/actuator/health`         | Health; при `kafka` — contributor `kafkaConsumerLag`           |
| GET   | `/actuator/prometheus`     | Метрики Prometheus                                             |

### Пример запроса

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

Ответы: **204** — успех, **400** — валидация, **401** — нет/неверный Bearer, **503** — ошибка SMTP.

## Требования к окружению

- **Docker Desktop** (для Testcontainers и compose)
- **Java 21**
- **Maven 3.9+**

## Быстрый старт через Docker

### 1. Клонирование

```bash
git clone <URL-репозитория> notification-service
cd notification-service
```

### 2. Только инфраструктура (приложение через Maven)

```bash
docker compose up -d
mvn spring-boot:run
```

Сервис: **https://localhost:8443** (локальный `server.port` в `application.yml`)  
Mailpit UI: **http://localhost:8025** (SMTP — `localhost:1025`)

### 3. Приложение в контейнере

```bash
docker compose --profile app up --build -d
```

HTTPS на хосте: **https://localhost:8444** (проброс `APP_HTTPS_PORT`).  
Профили в `docker-compose.yml`: `rest,kafka`. Для Redis добавьте профиль `redis` и переменные `SPRING_DATA_REDIS_*` (отдельный контейнер Redis или внешний инстанс).

### 4. Проверка

```bash
curl -k https://localhost:8444/actuator/health
# или при spring-boot:run:
curl -k https://localhost:8443/actuator/health
```

### 5. Остановка

```bash
docker compose down
docker compose down -v   # с очисткой тома Mongo
```

## Локальный запуск

```bash
docker compose up -d   # mongo, mailpit, kafka, zookeeper
# Redis (профиль redis): docker run -d -p 6379:6379 redis:7-alpine
mvn spring-boot:run -Dspring-boot.run.profiles=kafka,redis
```

## Архитектура проекта

```text
com.notification
├── config/        # Security, Kafka consumer/producer, Redis
├── controller/    # REST API (профиль rest)
├── service/       # NotificationServiceImpl — email + аудит Mongo
├── repository/    # MongoDB
├── entity/        # NotificationLog, операции
├── dto/           # NotificationEmailRequest
├── mapper/        # DTO → документ Mongo
├── idempotency/   # дедупликация по eventId (профиль kafka)
├── kafka/         # UserNotificationKafkaConsumer, KafkaConsumerLagHealthIndicator
├── lookup/        # UserLookupPort / RedisUserLookupPort (профиль redis)
└── exception/     # @RestControllerAdvice
```

## Тестирование

```bash
mvn test      # unit: WebMvcTest (service JWT)
mvn verify    # unit + *IntegrationTest (Testcontainers, нужен Docker)
```

Покрытие безопасности: отсутствие Bearer, неверный/пустой токен, схема `bearer` в нижнем регистре.

### Postman

- Коллекция: [`postman/collections/notification-service API-1`](postman/collections/notification-service%20API-1)
- Окружение: [`postman/environments/notification-service local-1.environment.yaml`](postman/environments/notification-service%20local-1.environment.yaml)

Папки: **Мониторинг**, **Email уведомления**, **Валидация и ошибки**. После `204` проверьте письмо в Mailpit (`{{mailpitUrl}}`).

## CI

`.github/workflows/NotificationServiceCI.yml` — `mvn verify`, сборка Docker-образа.

## Особенности реализации

- Единый `NotificationService` для REST и Kafka; зависимости через **constructor injection**
- **Идемпотентность** (`NotificationIdempotencyService`) — только при профиле `kafka`, внедряется как `Optional`
- **Manual ack** — `enable-auto-commit: false`
- **DLT** — после исчерпания retry сообщение уходит в `user-notifications.DLT`
- **Actuator** + Prometheus; **kafkaConsumerLag** — lag consumer group по топику `user-notifications`
- **Redis readiness** — при профиле `redis` включён в `/actuator/health/readiness`

## Автор

[charset-8utf](https://github.com/charset-8utf)
