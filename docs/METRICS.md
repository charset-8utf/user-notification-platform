# Метрики и observability

## Быстрый старт

```bash
docker compose --profile observability up -d --build
```

| Компонент | URL |
|-----------|-----|
| Prometheus | http://localhost:9091 |
| Grafana | http://localhost:3000 (admin / admin) |
| user-service metrics | http://localhost:8081/actuator/prometheus |
| notification-service metrics | http://localhost:8082/actuator/prometheus |

Проверка после подъёма:

```bash
./scripts/platform-smoke.sh
```

## Архитектура сбора

Prometheus скрейпит **management-порт 8081** (HTTP, без TLS) внутри Docker-сети:

- `user-service:8081/actuator/prometheus`
- `notification-service:8081/actuator/prometheus`

Основной HTTPS-порт (8443) для метрик не используется.

## Кастомные метрики

### user-service

| Метрика | Тип | Описание |
|---------|-----|----------|
| `app.outbox.pending` | Gauge | Строки outbox в статусе PENDING |
| `app.outbox.relay.published` | Counter | Успешная публикация из outbox |
| `app.outbox.relay.failed` | Counter | Ошибки relay |
| `app.notification.events.published` | Counter | События, отправленные Kafka producer |
| `app.ratelimit.rejected` | Counter | Ответы 429 |
| `app.grpc.server.requests` | Timer | Длительность gRPC-вызовов |

### notification-service

| Метрика | Тип | Описание |
|---------|-----|----------|
| `app.notification.email.sent` | Counter | Успешные письма (тег `operation`) |
| `app.notification.email.failed` | Counter | Ошибки доставки |
| `app.notification.duplicate.skipped` | Counter | Пропуск дубликатов |
| `app.kafka.consumer.lag.total` | Gauge | Суммарный lag consumer group |

## Автоматические метрики

- JVM (memory, GC, threads)
- HTTP `http.server.requests` (RPS, latency, status)
- HikariCP / DataSource
- Redis, Kafka client (при активных профилях)
- Resilience4j circuit breaker (профиль `rest` у user-service)

## Алерты

Файл [infra/observability/prometheus/alerts.yml](../infra/observability/prometheus/alerts.yml):

- сервис down
- высокая доля HTTP 5xx
- outbox backlog > 100
- Kafka lag > 1000
- circuit breaker open

Для production подключите Alertmanager.

## Grafana

Дашборд **Platform Overview** провижинится из [infra/observability/grafana/provisioning/dashboards/json/](../infra/observability/grafana/provisioning/dashboards/json/).

## Конфигурация

- Профиль `management` в compose (`SPRING_PROFILES_ACTIVE` включает `management`)
- Теги: `application`, `environment` (`METRICS_ENV`)
- Порт Prometheus на хосте: `PROMETHEUS_PORT` (по умолчанию **9091**, чтобы не конфликтовать с gRPC `:9090`)

## Production

- Не публиковать management-порт в интернет
- Секреты и `METRICS_ENV=prod` через Vault / K8s
- Не использовать high-cardinality labels (email, userId)
