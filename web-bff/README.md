# web-bff – backend-for-frontend для UI

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)

## Описание

[Spring MVC BFF](https://martinfowler.com/articles/bff.html) — агрегирует данные из `user-service` и `notification-service` для фронтенда.

**Возможности:**

- `GET /bff/me` — профиль пользователя, профиль и последнее уведомление в одном ответе
- JWT resource server (HS256)
- Graceful degradation: при недоступности profile/notification возвращаются пустые/UNAVAILABLE summary
- Token relay: bearer-токен пробрасывается downstream

## Быстрый старт

```bash
./gradlew :web-bff:bootRun
```

Проверка:

```bash
curl http://localhost:8090/actuator/health
```

## Архитектура

```text
com.platform.bff
├── config/          # typed properties, security, RestClient beans
├── security/        # JwtDecoderFactory (HS256)
├── client/          # RestClient adapters к user/notification
├── aggregation/     # Strategy: graceful profile fetch
├── facade/          # MeFacade
├── service/         # MeAggregationService
├── controller/      # MeController
└── exception/       # GlobalExceptionHandler
```

### GoF-паттерны

| Паттерн            | Где                                                                |
|--------------------|--------------------------------------------------------------------|
| **Facade**         | `MeFacade` → `MeAggregationService`                                |
| **Strategy**       | `ProfileFetchStrategy` / `GracefulProfileFetchStrategy`            |
| **Factory Method** | `JwtDecoderFactory` → `BffJwtDecoderFactory`                       |
| **Adapter**        | `UserServiceClient`, `NotificationServiceClient` → downstream REST |

## Конфигурация

| Record                | Префикс            | Назначение                              |
|-----------------------|--------------------|-----------------------------------------|
| `BffClientProperties` | `app.bff`          | base URLs, load-balanced, insecure-ssl  |
| `BffApiProperties`    | `app.bff.api`      | security paths (single source of truth) |
| `BffJwtProperties`    | `app.security.jwt` | HS256 secret                            |

Legacy env aliases (`APP_BFF_*`, `APP_JWT_SECRET`) сохранены в `application.yml`.

## Тесты

```bash
./gradlew :web-bff:check
```

JaCoCo instruction coverage ≥ 70%.

## Автор

[charset-8utf](https://github.com/charset-8utf)
