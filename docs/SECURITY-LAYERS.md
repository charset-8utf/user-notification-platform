# Три слоя безопасности платформы

Краткая карта: что защищает каждый канал и где настраивается.

## 1. Пользовательский API (user-service)

| | |
|---|---|
| **Назначение** | Браузер, Postman, внешние клиенты → CRUD пользователей |
| **Механизм** | JWT access/refresh (`POST /api/auth/login`), профиль `jwt` |
| **Секреты** | `APP_JWT_SECRET` (≥32 символа), опционально Redis для blacklist refresh |
| **Rate limit** | По `sub` из JWT (или IP для `/api/auth/*`) |
| **Документация** | [user-service/README.md](../user-service/README.md), фаза 1 в [SECURITY-ROADMAP.md](SECURITY-ROADMAP.md) |

## 2. Межсервисный REST (user-service → notification-service)

| | |
|---|---|
| **Назначение** | Профиль `rest`, синхронный `POST /api/notifications/email` |
| **Механизм** | Service JWT: `iss`, `sub`, `aud`, scope `notifications:write` |
| **Секреты** | `APP_SERVICE_JWT_SECRET` — **общий** для обоих сервисов; ротация: обновить secret, перезапустить оба сервиса, дождаться истечения TTL (по умолчанию 5 мин) |
| **Транспорт** | TLS + truststore (`notification-truststore.p12`), см. фазу 3 |
| **Resilience4j** | 401/403 от notification-service **не** размыкают circuit breaker |

## 3. Kafka (user-service → notification-service)

| | |
|---|---|
| **Назначение** | Основной путь уведомлений (outbox → топик `user-notifications`) |
| **Механизм** | `SASL_SSL` + `SCRAM-SHA-512`, ACL по пользователям брокера |
| **Учётки** | `user-service` (write), `notification-service` (read + DLT), **не** совпадают с REST JWT |
| **Секреты** | `KAFKA_USER_SERVICE_PASSWORD`, `KAFKA_NOTIFICATION_SERVICE_PASSWORD`; TLS: `infra/kafka/generate-kafka-certs.sh` |
| **Включение** | `APP_KAFKA_SECURITY_ENABLED=true` (compose по умолчанию) |

## Операционное hardening (фаза 5)

- **Actuator / Prometheus**: профиль `management` — отдельный порт `8081` (не публиковать в интернет; только внутренняя сеть / mesh).
- **Production**: секреты из Vault / K8s Secrets; не хранить в `.env` в репозитории.
- **Postman**: login → `accessToken`; notification-service — `serviceJwt` (см. `ServiceJwtSmokeToken`).
