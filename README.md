# user-notification-platform

Родительская папка **только для задания** (user-service + notification-service). Так в IDE и Cursor не попадают посторонние проекты.

## Состав

```text
user-notification-platform/
├── user-service/            ← REST user-service (PostgreSQL, отдельный git, Postman в postman/)
├── notification-service/    ← микросервис уведомлений (отдельный git, Postman в postman/)
├── docker-compose.yml       ← полный стек платформы
├── .env.example             ← секреты и профили (скопировать в .env)
├── docs/SECURITY-ROADMAP.md ← план безопасности (фазы 1–5)
├── docs/SECURITY-LAYERS.md  ← три слоя: user JWT, service JWT, Kafka
├── rename-user-service-directory.sh  ← однократно, если каталог ещё UserServiceSpringBoot
└── README.md
```

## Быстрый старт (compose)

```bash
cp .env.example .env   # при необходимости смените APP_SERVICE_JWT_SECRET и пароли
docker compose up -d --build
```

По умолчанию **Kafka** с SASL_SSL в compose (`APP_KAFKA_SECURITY_ENABLED=true`). Перед первым `docker compose up`: `infra/tls/generate-dev-certs.sh` и `infra/kafka/generate-kafka-certs.sh`. Юнит-тесты — PLAINTEXT Testcontainers без Kafka TLS.

### user-service: Swagger, HATEOAS, gRPC

| Компонент | URL / порт |
|-----------|------------|
| Swagger UI | `https://localhost:8443/swagger-ui.html` |
| OpenAPI JSON | `https://localhost:8443/v3/api-docs` |
| HATEOAS root | `GET https://localhost:8443/api` |
| gRPC | `localhost:9090` (plaintext в dev) |

Подробнее — [`user-service/README.md`](user-service/README.md).

### Observability (метрики)

```bash
docker compose --profile observability up -d --build
```

| Компонент | URL |
|-----------|-----|
| Prometheus | http://localhost:9091 |
| Grafana | http://localhost:3000 |
| Метрики user-service | http://localhost:8081/actuator/prometheus |

Подробнее — [docs/METRICS.md](docs/METRICS.md).

### Нагрузочный тест Kafka

```bash
./scripts/kafka/http_load_test.sh 100 10
```

Создаёт пользователей в user-service (outbox → Kafka) и проверяет доставку писем в Mailpit.

## Переименование каталога user-service

Если клон или копия проекта всё ещё в папке **`UserServiceSpringBoot`**, из **этой** директории выполните:

```bash
chmod +x ./rename-user-service-directory.sh
./rename-user-service-directory.sh
```

Скрипт переименует `UserServiceSpringBoot` → `user-service` и каталог Postman-коллекции в `user-service API-1`.

## Куда делись остальные проекты

Каталоги вроде `TinderBolt-v6`, `Aston_*`, старые `UserService*` и т.д. перенесены в **`/Users/igor/other`** (`~/other`).

## Что открыть в Cursor / IDEA

- **Cursor / VS Code:** **Open Folder** → `~/IdeaProjects/user-notification-platform`
- **IntelliJ:** **Open** → тот же каталог; при необходимости **Add as Maven Project** для обоих `pom.xml`

## Примечание

Актуальный путь к user-service: **`~/IdeaProjects/user-notification-platform/user-service`** (после выполнения скрипта переименования, если раньше папка называлась иначе).

Переоткрой workspace в IDE на эту родительскую папку.
