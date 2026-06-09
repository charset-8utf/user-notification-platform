# Runbook: Outbox / Inbox backlog

## Симптомы
- Алерт `OutboxBacklog` или `InboxBacklog`
- Письма не приходят, Kafka lag растёт

## Диагностика

```bash
# метрики
curl -s http://localhost:8081/actuator/prometheus | grep app_outbox
curl -s http://localhost:8082/actuator/prometheus | grep app_inbox

# Mongo inbox
kubectl -n platform exec deploy/mongo -- mongosh notification --eval 'db.notification_inbox.aggregate([{$group:{_id:"$status",c:{$sum:1}}}])'
```

## Действия
1. Проверить Kafka / Mailpit / SMTP
2. Проверить pod'ы `user-service`, `notification-service`
3. FAILED записи requeue автоматически каждые 30s; при необходимости рестарт relay pod
4. При длительном инциденте — scale HPA (prod)
