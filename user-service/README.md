# user-service – REST-сервис управления пользователями

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green?logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![Hibernate](https://img.shields.io/badge/Hibernate-7.3.2.Final-purple?logo=hibernate)
![Maven](https://img.shields.io/badge/Maven-3.9.14-blue?logo=apachemaven)
![Liquibase](https://img.shields.io/badge/Liquibase-4.x-red?logo=liquibase)
![Caffeine](https://img.shields.io/badge/Caffeine-Cache-brightgreen)
![JUnit](https://img.shields.io/badge/JUnit%20Jupiter-6.0.3-green)
![Mockito](https://img.shields.io/badge/Mockito-5.23.0-orange)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
[![CI](https://github.com/charset-8utf/UserService/actions/workflows/UserServiceCI.yml/badge.svg)](https://github.com/charset-8utf/UserService/actions/workflows/UserServiceCI.yml)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

## Описание проекта

REST-сервис для управления пользователями с поддержкой операций **Create**, **Read**, **Update**, **Delete** (CRUD).  
Построен на **Spring Boot 4** с использованием **Spring Data JPA**, **PostgreSQL** в Docker и пула соединений **HikariCP**.  
**Архитектура:** трёхслойная (Controller → Service → Repository) с DTO и ручными мапперами.

Схема БД управляется миграциями **Liquibase** (`src/main/resources/db/changelog/`).  
Покрыт юнит-тестами (JUnit, Mockito) и интеграционными тестами (Testcontainers, H2).  
Настроен CI (GitHub Actions) с авто-тестами и сборкой Docker-образа.

При локальном запуске поднимаются PostgreSQL, Redis, Kafka и Zookeeper (`docker compose` в каталоге сервиса).

## Исходящие уведомления

При создании и удалении пользователя сервис формирует событие для **notification-service**:

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "operation": "USER_CREATED",
  "email": "user@example.com"
}
```

**Профиль `kafka` (по умолчанию):** transactional outbox — запись в `notification_outbox` в той же транзакции, что и пользователь; `KafkaOutboxRelay` вызывает `UserNotificationKafkaProducer` → топик `user-notifications`, **partition key = email**. Producer: `acks=all`, `enable.idempotence=true`.

**Профиль `rest`:** синхронный `POST /api/notifications/email` во внешний notification-service (Resilience4j Circuit Breaker).

Параллельно в **Redis** кэшируется срез пользователя (`user:{id}`):

```json
{ "id": 42, "email": "user@example.com", "status": "ACTIVE" }
```

TTL — `app.cache.redis.ttl` (по умолчанию `PT1H`).

### Spring-профили

| Профиль | Назначение |
|---------|------------|
| `kafka` | Outbox + Kafka producer (`application-kafka.yml`) |
| `rest` | HTTP-клиент в notification-service (`application-rest.yml`) |
| `redis` | Кэш пользователя в Redis (`application-redis.yml`) |
| `jwt` | OAuth2 Resource Server + `AuthController` (`application-jwt.yml`) |
| `local` | HTTP Basic **вместе** с JWT (удобно для Postman) |

Дефолт в compose платформы: `kafka,redis,jwt`. Для REST-режима: `SPRING_PROFILES_ACTIVE=rest,redis,jwt`.

Профили **`kafka` и `rest` взаимоисключающие** — при одновременной активации Spring завершит старт с `NoUniqueBeanDefinitionException`.

### REST-режим

```
POST {base-url}/api/notifications/email
Authorization: Bearer {service-jwt}
Content-Type: application/json

{
  "eventId": "660e8400-e29b-41d4-a716-446655440001",
  "operation": "USER_CREATED",
  "email": "user@example.com"
}
```

| Свойство | По умолчанию |
|----------|--------------|
| `APP_NOTIFICATION_REST_BASE_URL` | `https://notification-service:8443` |
| `APP_NOTIFICATION_REST_INSECURE_SSL` | `false` — проверка TLS через truststore; `true` только для отладки |
| `APP_NOTIFICATION_REST_TRUST_STORE` | `classpath:notification-truststore.p12` (CA dev PKI) |
| `APP_SERVICE_JWT_SECRET` | общий секрет с notification-service (HS256, ≥32 байт; см. `application-rest.yml`) |
| `APP_SERVICE_JWT_ISSUER` / `SUBJECT` / `AUDIENCE` / `SCOPE` | по умолчанию `user-notification-platform`, `user-service`, `notification-service`, `notifications:write` |

Перегенерация dev-сертификатов (compose SAN `notification-service`): `../infra/tls/generate-dev-certs.sh`
| `APP_NOTIFICATION_REST_CONNECT_TIMEOUT` | `PT2S` |
| `APP_NOTIFICATION_REST_READ_TIMEOUT` | `PT5S` |

При недоступности downstream notification-service срабатывает fallback: пользователь сохраняется, событие теряется (лог WARN).

## API-эндпоинты

| Метод  | Путь                                | Описание                            |
|--------|-------------------------------------|-------------------------------------|
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

- **Docker Desktop** (PostgreSQL, Redis, Kafka, интеграционные тесты)
- **Java 21**
- **Maven 3.9+**

## Быстрый старт через Docker

### 1. Клонирование

```bash
git clone https://github.com/charset-8utf/UserService.git user-service
cd user-service
```

### 2. Переменные окружения

```bash
cp .env.example .env
```

По умолчанию в `.env.example`: `DB_USER`, `DB_PASSWORD`, `KEYSTORE_PASSWORD`, seed-пароли, порты `8443` / `5432`.

> Без `APP_SEED_ADMIN_PASSWORD` и `APP_SEED_USER_PASSWORD` учётные записи не создаются — API вернёт 401.

### 3. Запуск

```bash
docker compose up --build -d
```

Приложение: **https://localhost:8443**

Режим разработки (только инфра в Docker, приложение через Maven):

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
mvn spring-boot:run
```

Сброс БД:

```bash
docker compose down -v && docker compose up --build -d
```

> Самоподписанный TLS: в браузере примите исключение; для API удобнее **curl** (`-k -u`) или **Postman**.

### 4. Аутентификация (JWT, профиль `jwt`)

Получение токена:

```bash
curl -k -X POST https://localhost:8443/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

Ответ: `accessToken`, `refreshToken`, `expiresIn`. Дальше: `Authorization: Bearer <accessToken>`.

| Эндпоинт | Назначение |
|----------|------------|
| `POST /api/auth/login` | Выдача пары токенов |
| `POST /api/auth/refresh` | Rotation refresh → новая пара |
| `POST /api/auth/logout` | Отзыв refresh (blacklist) |

| Переменная | По умолчанию |
|------------|--------------|
| `APP_JWT_SECRET` | мин. 32 символа (HS256) |
| `APP_JWT_ACCESS_TTL` | `PT15M` |
| `APP_JWT_REFRESH_TTL` | `P7D` |

Для Postman с HTTP Basic добавьте профиль **`local`** (`SPRING_PROFILES_ACTIVE=kafka,redis,jwt,local`).

Учётные записи seed:

| Логин   | Пароль (из .env)          | Роль         |
|---------|---------------------------|--------------|
| `admin` | `APP_SEED_ADMIN_PASSWORD` | ADMIN + USER |
| `user`  | `APP_SEED_USER_PASSWORD`  | USER         |

### 5. Проверка

```bash
curl -k https://localhost:8443/actuator/health
curl -k -u admin:admin123 https://localhost:8443/api/users
```

### 6. Остановка

```bash
docker compose down      # данные сохраняются
docker compose down -v   # полная очистка томов
```

## Локальный запуск (PostgreSQL в Docker)

```bash
docker run --name user-postgres \
  -e POSTGRES_DB=userdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:17-alpine

mvn spring-boot:run
# или
APP_SEED_ADMIN_PASSWORD=admin123 APP_SEED_USER_PASSWORD=user123 mvn spring-boot:run
```

## Архитектура проекта

```text
com.crud
├── config/       # безопасность, Kafka, REST-клиент, CORS, rate limit
├── controller/   # REST API
├── service/      # бизнес-логика
├── repository/   # Spring Data JPA
├── entity/       # JPA-сущности
├── dto/          # DTO
├── mapper/       # DTO ↔ Entity
├── notification/ # события, outbox, Kafka/REST-публикация
│   ├── kafka/    # UserNotificationKafkaProducer (отправка в топик)
│   └── outbox/   # transactional outbox + KafkaOutboxRelay
├── cache/        # Redis-кэш пользователя
├── exception/    # исключения и @RestControllerAdvice
└── security/     # учётные записи
```

## Тестирование

```bash
mvn test      # unit
mvn verify    # unit + integration
```

Интеграционные тесты используют **Testcontainers** (нужен Docker).

### Postman

- Коллекция: [`postman/collections/user-service API-1`](postman/collections/user-service%20API-1)
- Окружение: [`postman/environments/user-service local-1.environment.yaml`](postman/environments/user-service%20local-1.environment.yaml)

В Postman отключите **SSL certificate verification** для `localhost` и выберите окружение **user-service local-1**.

## CI

`.github/workflows/UserServiceCI.yml` — `mvn verify`, сборка Docker-образа, smoke-тест в compose.

## Особенности реализации

- **Spring Security** — JWT (профиль `jwt`), опционально HTTP Basic (`local`), роли USER / ADMIN
- **HTTPS** — TLS 1.2/1.3, порт 8443; actuator/prometheus — профиль `management`, порт 8081
- **Rate limit** — по `sub` JWT (не по заголовку Authorization целиком)
- **Оптимистичные блокировки** — `@Version` + `@Retryable`
- **Кэш 2-го уровня** — Caffeine + JCache
- **Rate limiting** — 20 запросов / 60 с (по умолчанию)
- **Actuator** — health, metrics, prometheus
- **Liquibase** — `ddl-auto: validate`

## Автор

[charset-8utf](https://github.com/charset-8utf)
