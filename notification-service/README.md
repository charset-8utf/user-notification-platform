# notification-service – микросервис email-уведомлений

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green?logo=springsecurity)
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

Стек: **Java 21**, **Spring Boot 4**, **Spring Kafka 4** (Jackson 3 / `JsonMapper`), **Spring Data MongoDB**, **Spring Data Redis**.  
Покрыт модульными и интеграционными тестами (Testcontainers).  
Настроен CI (GitHub Actions) с авто-тестами и сборкой Docker-образа.  
Приложение и зависимости (MongoDB, Mailpit, Kafka) запускаются через `docker compose`.

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

**Kafka:** `UserNotificationKafkaConsumer` — manual ack, идемпотентность по `eventId`, retry и DLT (`user-notifications.DLT`).

**REST:** `POST /api/notifications/email` → **204**. Требуется **service JWT** в `Authorization: Bearer <token>`.

## API-эндпоинты

| Метод | Путь                       | Описание                                                       |
|-------|----------------------------|----------------------------------------------------------------|
| POST  | `/api/notifications/email` | Отправить уведомление (`eventId`, `operation`, `email`)        |
| GET   | `/actuator/health`         | Health; при `kafka` — contributor `kafkaConsumerLag`           |
| GET   | `/actuator/info`           | Информация о приложении                                        |
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

Ответы: **204** — успех, **400** — валидация, **401** / **403** — JWT, **503** — ошибка SMTP.

## Требования к окружению

- **Docker Desktop** (Testcontainers и compose)
- **Java 21**
- **Maven 3.9+**

## Быстрый старт через Docker

### 1. Клонирование репозитория

```bash
git clone https://github.com/charset-8utf/notification-service.git
cd notification-service
```

### 2. Настройка переменных окружения

При необходимости создайте `.env` (см. `docker-compose.yml`):

```properties
KEYSTORE_PASSWORD=changeit
APP_SERVICE_JWT_SECRET=dev-service-jwt-secret-change-in-production-min-32b
APP_HTTPS_PORT=8444
SPRING_PROFILES_ACTIVE=rest,kafka,redis,management
```

> Секрет `APP_SERVICE_JWT_SECRET` (≥32 байт) должен совпадать у **всех сервисов**, которые вызывают REST API с service JWT.

### 3. Запуск инфраструктуры и приложения

Только Mongo, Mailpit, Kafka (приложение через Maven):

```bash
docker compose up -d
mvn spring-boot:run
```

Сервис: **https://localhost:8443**  
Mailpit UI: **http://localhost:8025** (SMTP — `localhost:1025`)

Приложение в контейнере:

```bash
docker compose --profile app up --build -d
```

HTTPS на хосте: **https://localhost:8444** (`APP_HTTPS_PORT`).

### 4. Аутентификация (service JWT)

Эндпоинт `/api/notifications/email` защищён OAuth2 Resource Server (HS256). Проверяются `iss`, `sub`, `aud` и scope `notifications:write`.

Для ручных вызовов сгенерируйте токен (тот же `APP_SERVICE_JWT_SECRET`):

```bash
export APP_SERVICE_JWT_SECRET=dev-service-jwt-secret-change-in-production-min-32b
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp "target/test-classes:target/classes:$(cat cp.txt)" \
  com.notification.support.ServiceJwtSmokeToken
```

`/actuator/health`, `/actuator/info`, `/actuator/prometheus` — без авторизации (профиль `management`, порт **8081** внутри контейнера).

### 5. Проверка

```bash
curl -k https://localhost:8444/actuator/health
curl -k https://localhost:8443/actuator/health
```

После успешного POST проверьте письмо в Mailpit: http://localhost:8025

### 6. Остановка и очистка

```bash
docker compose down
docker compose down -v
```

## Spring-профили

| Профиль      | Назначение                                                        |
|--------------|-------------------------------------------------------------------|
| `rest`       | `NotificationController` — HTTP API                               |
| `kafka`      | `UserNotificationKafkaConsumer`, DLT, идемпотентность, lag health |
| `redis`      | `RedisUserLookupPort` — чтение `user:email:{email}`               |
| `management` | Actuator и Prometheus на отдельном порту                          |

Дефолт в `application.yml`: **`kafka`**.

| Сценарий              | `SPRING_PROFILES_ACTIVE`      |
|-----------------------|-------------------------------|
| Только Kafka          | `kafka`                       |
| Kafka + REST          | `rest,kafka`                  |
| Kafka + Redis         | `kafka,redis`                 |
| Полный локальный стек | `rest,kafka,redis,management` |

## Локальный запуск

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=kafka,redis,management
```

## Архитектура проекта

```text
com.notification
├── config/        # Security, Kafka, Redis
├── controller/    # REST API (профиль rest)
├── service/       # отправка email + аудит Mongo
├── repository/    # MongoDB
├── entity/        # NotificationLog
├── dto/           # NotificationEmailRequest
├── mapper/        # DTO → документ
├── idempotency/   # дедупликация по eventId (kafka)
├── kafka/         # consumer, lag health
├── lookup/        # UserLookupPort / Redis
├── security/      # service JWT Resource Server
└── exception/     # @RestControllerAdvice
```

## Тестирование

### Запуск тестов

```bash
mvn test
mvn verify
```

### Ручная проверка API (curl)

```bash
export APP_SERVICE_JWT_SECRET=dev-service-jwt-secret-change-in-production-min-32b
mvn -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=cp.txt
SVC_JWT=$(java -cp "target/test-classes:target/classes:$(cat cp.txt)" \
  com.notification.support.ServiceJwtSmokeToken)

curl -k -X POST https://localhost:8444/api/notifications/email \
  -H "Authorization: Bearer $SVC_JWT" \
  -H 'Content-Type: application/json' \
  -d '{"eventId":"990e8400-e29b-41d4-a716-446655440099","operation":"USER_CREATED","email":"user@example.com"}'
```

Письмо проверьте в Mailpit: http://localhost:8025

## CI

Файл `.github/workflows/NotificationServiceCI.yml`:

- JDK 21, `mvn clean verify`
- Сборка Docker-образа
- Загрузка артефактов тестов

## Логирование

**SLF4J** (Spring Boot). Уровни — в `application.yml`.

## Особенности реализации

- Единый `NotificationService` для REST и Kafka; **constructor injection**
- **Идемпотентность** по `eventId` (профиль `kafka`)
- **Manual ack** Kafka, **DLT** после исчерпания retry
- **Service JWT** — отклонение пользовательских access-токенов с чужим `iss` / scope
- **HTTPS** — `keystore.p12`, alias `notification-service`
- **Actuator** + **Prometheus**; **kafkaConsumerLag** — lag consumer group
- **Redis readiness** в `/actuator/health/readiness` (профиль `redis`)
- Опционально **Kafka SASL_SSL** — `APP_KAFKA_SECURITY_ENABLED=true`, truststore `kafka-truststore.p12`
- Сообщения Kafka — JSON без `__TypeId__` (Jackson 3 `JsonMapper`)

## Автор

[charset-8utf](https://github.com/charset-8utf)
