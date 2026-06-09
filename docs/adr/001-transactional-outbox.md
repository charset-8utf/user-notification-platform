# ADR-001: Transactional Outbox (user-service)

## Status
Accepted

## Context
События о пользователях должны публиковаться в Kafka без потери при сбое между commit БД и send в broker.

## Decision
Запись в `notification_outbox` в той же транзакции, что и изменение пользователя; `KafkaOutboxRelay` публикует PENDING → Kafka.

## Consequences
At-least-once delivery; consumer-side inbox обеспечивает идемпотентность.
