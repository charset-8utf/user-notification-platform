# Kafka

## Текущая схема

- Формат сообщений: **JSON** (`NotificationEmailRequest`)
- Топик: `user-notifications`
- DLT: `user-notifications.DLT`
- Compensation: `notification-compensations`

## Outbox / Inbox

| Сторона | Таблица/коллекция | Relay |
|---------|-------------------|-------|
| Producer (user-service) | `notification_outbox` | `KafkaOutboxRelay` |
| Consumer (notification-service) | `notification_inbox` | `KafkaInboxRelay` |

## Compensation (saga)

```text
inbox relay delivery failure → notification-compensations
  → NotificationCompensationConsumer (user-service)
  → notificationDeliveryStatus = FAILED

Kafka consumer errors (редко) → DLT (user-notifications.DLT)
  → UserNotificationDltListener → notification-compensations
```

E2E: `make e2e-compensation` (cloud stack + Kafka running).

## Schema Registry

В `docker-compose.yml` сервис **schema-registry** (Confluent 7.4) доступен на хосте:

```text
http://localhost:8085/subjects
```

Переменная: `SCHEMA_REGISTRY_PORT` (по умолчанию `8085`, чтобы не конфликтовать с management-портом user-service).

Сериализаторы пока **JSON**; Registry подготовлен для эволюции контрактов:

1. Зарегистрировать Avro/Protobuf схему для `NotificationEmailRequest`
2. Перейти на `KafkaAvroSerializer` / `KafkaAvroDeserializer`
3. CI: проверка backward compatibility (`maven-metadata` / `schema-registry` plugin)

Для dev/demo JSON + интеграционные тесты идемпотентности достаточны.
