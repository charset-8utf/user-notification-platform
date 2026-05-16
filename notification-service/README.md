# notification-service – микросервис email-уведомлений

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-black?logo=apachekafka)
![Maven](https://img.shields.io/badge/Maven-3.9.14-blue?logo=apachemaven)
![JUnit](https://img.shields.io/badge/JUnit%20Jupiter-6.0.3-green)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

## Описание проекта

Микросервис отправки email-уведомлений о событиях пользователей (**создание** / **удаление** аккаунта).  
Принимает события по **REST** и из **Kafka**, пишет аудит в **MongoDB**, отправляет письма через SMTP (в dev — **Mailpit**).

Стек: **Java 21**, **Spring Boot 4**, **Spring Kafka 4** (Jackson 3), **Spring Data MongoDB**.  
Покрыт модульными и интеграционными тестами (Testcontainers). CI — GitHub Actions.

Пара **user-service** + **notification-service** входит в монорепозиторий `user-notification-platform` с общим корневым `docker-compose`.

## Интеграция с user-service

События из **user-service** (топик `user-notifications` или REST):

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

**Kafka:** consumer group `notification-service`, manual commit offset, идемпотентность по `eventId`, retry и DLT (`user-notifications.DLT`).

**REST:** `POST /api/notifications/email` → `204 No Content`.

## Spring-профили

| Профиль | Назначение |
|---------|------------|
| `rest`  | `NotificationController` — HTTP API |
| `kafka` | `UserNotificationListener`, топики, DLT, lag health |
| `redis` | чтение кэша `user:email:{email}` из Redis user-service |

Дефолт: `rest,kafka`. Только REST: `SPRING_PROFILES_ACTIVE=rest`.

> Подключение к Mongo: **`spring.mongodb.uri`** / `SPRING_MONGODB_URI` (Spring Boot 4).

## API-эндпоинты

| Метод | Путь | Описание |
|-------|------|----------|
| POST  | `/api/notifications/email` | Отправить уведомление (JSON с `eventId`, `operation`, `email`) |
| GET   | `/actuator/health` | Health (в т.ч. `kafkaConsumerLag` при профиле `kafka`) |
| GET   | `/actuator/prometheus` | Метрики Prometheus |

### Пример запроса

```bash
curl -X POST http://localhost:8081/api/notifications/email \
  -H 'Content-Type: application/json' \
  -d '{
    "eventId": "990e8400-e29b-41d4-a716-446655440099",
    "operation": "USER_CREATED",
    "email": "user@example.com"
  }'
```

Ответы: **204** — успех, **400** — валидация, **503** — ошибка SMTP.

## Требования к окружению

- **Docker Desktop**
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

Сервис: **http://localhost:8081**  
Mailpit UI: **http://localhost:8025** (SMTP приложения — `localhost:1025`)

### 3. Приложение в контейнере

```bash
docker compose --profile app up --build -d
```

### 4. Полный стек с user-service

Из родительского каталога монорепозитория:

```bash
cd ~/IdeaProjects/user-notification-platform
docker compose up -d --build
```

### 5. Проверка

```bash
curl http://localhost:8081/actuator/health
```

### 6. Остановка

```bash
docker compose down
docker compose down -v   # с очисткой томов Mongo
```

## Локальный запуск

```bash
docker compose up -d   # mongo, mailpit, kafka, zookeeper
mvn spring-boot:run
```

## Архитектура проекта

```text
com.notification
├── config/        # Kafka consumer/producer, топики
├── controller/    # REST API
├── listener/      # @KafkaListener
├── service/       # отправка email + аудит
├── repository/    # MongoDB
├── entity/        # NotificationLog, операции
├── dto/           # NotificationEmailRequest
├── mapper/        # DTO → документ Mongo
├── idempotency/   # дедупликация по eventId
├── kafka/         # health: consumer lag
├── lookup/        # обогащение из Redis (профиль redis)
└── exception/     # @RestControllerAdvice
```

## Тестирование

```bash
mvn test      # unit (WebMvcTest)
mvn verify    # unit + *IntegrationTest (Testcontainers, нужен Docker)
```

### Postman

- Коллекция: [`postman/collections/notification-service API-1`](postman/collections/notification-service%20API-1)
- Окружение: [`postman/environments/notification-service local-1.environment.yaml`](postman/environments/notification-service%20local-1.environment.yaml)

Папки: **Мониторинг**, **Email уведомления**, **Валидация и ошибки**. После `204` проверьте письмо в Mailpit (`{{mailpitUrl}}`).

## CI

`.github/workflows/NotificationServiceCI.yml` — `mvn verify`, сборка Docker-образа.

## Особенности реализации

- Единый `NotificationService` для REST и Kafka
- **Manual ack** — `enable-auto-commit: false`
- **Идемпотентность** — коллекция `processed_notification_events`
- **DLT** — после исчерпания retry сообщение уходит в `user-notifications.DLT`
- Топик **3 partitions**; partition key с стороны user-service — `email`
- **Actuator** + Prometheus

## Автор

[charset-8utf](https://github.com/charset-8utf)
