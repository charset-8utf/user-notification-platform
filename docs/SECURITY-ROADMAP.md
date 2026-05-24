# План усиления безопасности (фазы 1–5)

Фаза **0** реализована в коде: service Bearer token на notification-service, заголовок в RestClient user-service, профили и секреты в compose.

Фазы **1–4** реализованы (JWT, service JWT, TLS REST, Kafka SASL_SSL). Фаза **5** — operational hardening (rate limit, Resilience4j, management port, документация).

Ниже — последующие этапы. Каждая фаза — отдельный инкремент; порядок рекомендуемый.

---

## Фаза 1 — JWT для пользователей (user-service) ✅

**Цель:** заменить HTTP Basic на access/refresh JWT для клиентов API.

| Задача | Статус |
|--------|--------|
| Resource Server | ✅ `spring-boot-starter-oauth2-resource-server`, HS256 (`app.security.jwt.secret`) |
| Выдача токена | ✅ `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout` |
| Claims | ✅ `sub`, `roles` → `ROLE_USER` / `ROLE_ADMIN` |
| TTL | ✅ access `PT15M` (env `APP_JWT_ACCESS_TTL`), refresh `P7D` + rotation |
| Отзыв refresh | ✅ blacklist в Redis (`jwt & redis`) или in-memory (`jwt` без redis) |
| Совместимость | ✅ профиль `local`: HTTP Basic **дополнительно** к JWT (Postman) |
| Тесты | ✅ интеграционные через `JwtAuthTestSupport`, unit-тесты auth/jwt |

**Профили:** `kafka,redis,jwt` в compose; без `jwt` — прежний HTTP Basic (юнит-тесты `@WithMockUser`).

**Не затрагивает:** Kafka, notification-service (фаза 2).

---

## Фаза 2 — Service JWT между сервисами (вместо static Bearer) ✅

**Цель:** эволюция фазы 0: подписанный токен с `aud`, `scope`, коротким TTL.

| Задача | Статус |
|--------|--------|
| Issuer / claims | ✅ `iss: user-notification-platform`, `sub: user-service`, `aud: notification-service` |
| Scope | ✅ `notifications:write` на `POST /api/notifications/email` |
| user-service | ✅ `ServiceJwtTokenProvider` + interceptor в RestClient (`@Profile("rest")`) |
| notification-service | ✅ OAuth2 Resource Server, проверка `iss` / `sub` / `aud` + scope |
| Запрет user JWT | ✅ пользовательский access token отклоняется (другой `iss` / scope) |
| Секрет | ✅ `APP_SERVICE_JWT_SECRET` (≥32 байт), общий в compose для обоих сервисов |
| Тесты / E2E | ✅ `ServiceJwtTestSupport`, smoke через `ServiceJwtSmokeToken` |

---

## Фаза 3 — TLS между сервисами (east-west) ✅

**Цель:** шифрование трафика внутри сети (JWT не заменяет TLS).

| Задача | Статус |
|--------|--------|
| notification-service HTTPS | ✅ PKCS12 `keystore.p12`, alias `notification-service`, SAN: `notification-service`, `localhost` |
| Dev PKI | ✅ `infra/tls/generate-dev-certs.sh` — CA + серверные cert + `notification-truststore.p12` |
| user-service RestClient | ✅ truststore по умолчанию (`insecure-ssl=false`); escape hatch `APP_NOTIFICATION_REST_INSECURE_SSL=true` |
| Compose | ✅ `APP_NOTIFICATION_REST_BASE_URL=https://notification-service:8443`, проверка TLS включена |
| Prod K8s | 📋 Ingress TLS / mesh (mTLS) — вне scope dev-скрипта |

При профиле **только `kafka`** — REST/TLS не используется.

---

## Фаза 4 — Kafka: SSL + SASL ✅

**Цель:** только авторизованные клиенты публикуют/читают топики.

| Задача | Статус |
|--------|--------|
| Шифрование | ✅ `SASL_SSL` (TLS + SCRAM-SHA-512), broker `kafka:29093` |
| Аутентификация | ✅ SCRAM users в `infra/kafka/secrets/kafka_server_jaas.conf` |
| ACL | ✅ `kafka-init`: user-service WRITE + IdempotentWrite; notification-service READ + DLT WRITE + group READ |
| Учётки | ✅ `user-service` / `notification-service` (отдельно от REST JWT) |
| Spring | ✅ `app.kafka.security.enabled` + `KafkaSecurityConfiguration`; тесты — PLAINTEXT (Testcontainers) |
| Dev PKI | ✅ `infra/kafka/generate-kafka-certs.sh` → `kafka-truststore.p12` в обоих сервисах |

---

## Фаза 5 — Согласованность и hardening ✅

| Задача | Статус |
|--------|--------|
| Rate limit | ✅ `RateLimitKeyResolver`: `sub` из JWT / username / IP |
| Resilience4j | ✅ 401/403 в `ignore-exceptions` для circuit `notification-service` |
| Actuator | ✅ профиль `management`, порт `8081` (compose + E2E) |
| Секреты | ✅ ротация и слои в [SECURITY-LAYERS.md](SECURITY-LAYERS.md) |
| E2E / Postman | ✅ JWT login smoke; Postman Bearer + login |
| Документация | ✅ [SECURITY-LAYERS.md](SECURITY-LAYERS.md) — user / service / Kafka |

---

## Приоритеты

```text
P0  — фаза 0 (сделано)
P1  — фаза 4 (Kafka в prod)
P2  — фаза 1 (JWT пользователей)
P3  — фаза 2 (service JWT)
P4  — фаза 3 (HTTPS east-west / mesh)
P5  — фаза 5 (операционный hardening)
```

---

## Матрица профилей (целевая)

| Среда | user-service | notification-service | Межсервис |
|-------|--------------|----------------------|-----------|
| local | `kafka,redis,jwt` + optional `local` auth | `kafka,redis` + `rest` для ручных тестов API | service JWT (фаза 2) |
| staging | `kafka,redis,jwt` | `kafka,redis` + OAuth2 RS | HTTPS + service JWT + truststore |
| prod | `kafka,redis` + JWT | `kafka,redis` | Kafka SASL/SSL; REST выключен |
