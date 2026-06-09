# ADR-002: Transactional Inbox (notification-service)

## Status
Accepted

## Context
Kafka consumer работает at-least-once; прямая обработка в listener без персистентности теряет события при crash после ack.

## Decision
`UserNotificationKafkaConsumer` пишет в `notification_inbox` (PENDING); `KafkaInboxRelay` доставляет email и ставит PROCESSED/FAILED.

## Consequences
Симметрия с outbox; метрики `app.inbox.*`; replay FAILED → PENDING.
