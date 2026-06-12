# platform-commons – общая библиотека платформы

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)

## Описание

Переиспользуемая библиотека для всех сервисов [`user-notification-platform`](../README.md).  
Подключается как Gradle-зависимость `project(":platform-commons")` и активируется через Spring Boot AutoConfiguration.

**Возможности:**

- **Observability** — common tags Micrometer (`application`, `environment`), HTTP 5xx-метрики, отключение Zipkin при `TRACING_ENABLED=false`
- **Logging** — паттерн консоли с `traceId`/`spanId` в MDC
- **Audit** — `@AuditLog` AOP-аннотация для структурированного аудита
- **OpenAPI** — базовая схема (JWT + API Key) для наследования в сервисах
- **Defaults YAML** — `platform/tracing-defaults.yml`, `platform/discovery-defaults.yml`
- **Kafka security** — SASL_SSL + SCRAM-SHA-512 для Kafka-клиентов (`app.kafka.security.*`)

## Подключение

```kotlin
dependencies {
    implementation(project(":platform-commons"))
}
```

Импорт defaults в `application.yml`:

```yaml
spring:
  config:
    import:
      - optional:classpath:platform/tracing-defaults.yml
```

OpenAPI в сервисе:

```java
@Configuration
public class OpenApiConfiguration extends PlatformOpenApiConfiguration {
}
```

## Конфигурация

Все свойства — typed record `PlatformProperties` (`platform.*`), без `@Value` в коде библиотеки.

| Префикс                      | Record                       | Назначение                                                          |
|------------------------------|------------------------------|---------------------------------------------------------------------|
| `platform.environment`       | `PlatformProperties`         | Тег `environment` в Micrometer (fallback: `spring.profiles.active`) |
| `platform.logging.trace-mdc` | `PlatformProperties.Logging` | Включить traceId/spanId в console pattern                           |
| `platform.openapi.*`         | `PlatformProperties.OpenApi` | description, version для OpenAPI                                    |
| `platform.tracing.enabled`   | `PlatformProperties.Tracing` | Tracing on/off (alias: env `TRACING_ENABLED`)                       |
| `app.kafka.security.*`       | `KafkaSecurityProperties`    | SASL_SSL / trust store (профиль `kafka`, `enabled=true`)            |

Пример:

```yaml
platform:
  environment: cloud
  logging:
    trace-mdc: true
  openapi:
    description: User Notification Platform API
    version: 1.0.0
  tracing:
    enabled: false
```

## Архитектура

```text
com.platform.commons
├── config/           # PlatformProperties, AbstractOrderedEnvironmentPostProcessor
├── io/               # SecureTempFiles
├── kafka/            # KafkaSecuritySupport, KafkaSecurityAutoConfiguration
├── observability/    # метрики, tracing EPP, servlet filter
├── logging/          # trace MDC EPP
├── audit/            # @AuditLog AOP
└── openapi/          # PlatformOpenApiConfiguration (abstract)
```

### GoF-паттерны

| Паттерн                     | Где                                                                                                                                                                 | Назначение                                          |
|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| **Facade**                  | `PlatformCommonsAutoConfiguration`, `PlatformOpenApiConfiguration`                                                                                                  | Единая точка входа автоконфигурации                 |
| **Template Method**         | `AbstractOrderedEnvironmentPostProcessor`                                                                                                                           | Каркас EPP с `shouldApply()` / `apply()`            |
| **Strategy**                | `AuditActorResolver`, `EnvironmentTagResolver`, `TracingEnabledResolver`, `TraceMdcEnabledResolver`, `ZipkinAutoConfigurationExcludeMerger`, `KafkaSecuritySupport` | Аудит, метрики, tracing/logging EPP, Kafka SASL_SSL |
| **Proxy**                   | `AuditLogInvocationProxy`                                                                                                                                           | Обёртка вызова метода с записью аудита              |
| **Chain of Responsibility** | `ServerErrorMetricsHandler` → `ObservabilityFilterHandler`                                                                                                          | Цепочка servlet-фильтра                             |
| **Adapter**                 | `StructuredAuditLogWriter`                                                                                                                                          | Запись аудита в SLF4J-логгер `AUDIT`                |

## Аудит

```java
@AuditLog(action = "USER_CREATE", resourceType = "user")
public UserResponse createUser(CreateUserRequest request) { ... }
```

Лог пишется в logger `AUDIT` с полями: `action`, `resourceType`, `actor`, `method`, `traceId`, `spanId`.

## Тестирование

```bash
./gradlew :platform-commons:check
```

JaCoCo: порог покрытия **≥ 70%** (instruction).

## Автор

[charset-8utf](https://github.com/charset-8utf)
