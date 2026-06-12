# api-gateway – edge API платформы

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-2025.1-green)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)

## Описание

[Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/) — единая точка входа в [`user-notification-platform`](../README.md).

**Возможности:**

- Маршрутизация к `user-service` и `notification-service`
- JWT validation на edge (HS256 dev / OIDC prod)
- `TokenRelay` — проброс bearer-токена downstream
- Rate limiting (Redis) по IP и JWT `sub`
- Circuit breaker + fallback (`/fallback/user`, `/fallback/notification`)

## Быстрый старт

```bash
./gradlew :api-gateway:bootRun
```

Проверка:

```bash
curl http://localhost:8080/actuator/health
```

## Профили

| Профиль           | Назначение                                          |
|-------------------|-----------------------------------------------------|
| `cloud` (default) | Production-like: security, routes, Redis rate limit |
| `cloud-it`        | Integration tests: security disabled                |
| `gateway-sec-it`  | Security integration tests с JWT                    |

## Маршруты (профиль `cloud`)

| Route                       | Path                                                 | Rate limit key |
|-----------------------------|------------------------------------------------------|----------------|
| `user-service-auth`         | `/api/auth/**`                                       | IP             |
| `user-service-api`          | `/api/users/**`, `/api/roles/**`, `/api/profiles/**` | JWT sub        |
| `notification-service-logs` | `/api/notifications/logs/**`                         | JWT sub        |

## Архитектура

```text
com.platform.gateway
├── config/          # typed properties, security, rate limit beans
├── security/        # ReactiveJwtDecoderFactory, JwtValidatorComposer
├── ratelimit/       # Strategy: IP / JWT sub
├── filter/          # TokenRelayGatewayFilterFactory
└── fallback/        # Template Method: GatewayFallbackTemplate
```

### GoF-паттерны

| Паттерн                     | Где                                                          |
|-----------------------------|--------------------------------------------------------------|
| **Strategy**                | `GatewayRateLimitKeyStrategy`, `JwtValidatorComposer`        |
| **Factory Method**          | `ReactiveJwtDecoderFactory`                                  |
| **Template Method**         | `GatewayFallbackTemplate`                                    |
| **Adapter**                 | `JwtSubKeyResolver` → Spring `KeyResolver`                   |
| **Strategy**                | `TokenRelayExchangeResolver` → JWT relay из security context |
| **Chain of Responsibility** | `JwtGatewayRateLimitKeyStrategy` → IP fallback               |

## Конфигурация

| Record                       | Префикс                  | Назначение                              |
|------------------------------|--------------------------|-----------------------------------------|
| `GatewayApiProperties`       | `app.gateway.api`        | Security paths (single source of truth) |
| `GatewayRateLimitProperties` | `app.gateway.rate-limit` | replenish/burst per route               |
| `GatewaySslProperties`       | `app.gateway.ssl`        | downstream TLS trust                    |
| `GatewayJwtProperties`       | `app.security.jwt`       | HS256 secret / OIDC issuer              |

Legacy env aliases (`APP_GATEWAY_*`, `APP_JWT_*`) сохранены в `application-cloud.yml`.

## Тестирование

```bash
./gradlew :api-gateway:check
```

- Unit: `*Test.java`
- Integration: `*IntegrationTest.java` (WireMock downstream)

JaCoCo: **≥ 70%**.

## Автор

[charset-8utf](https://github.com/charset-8utf)
