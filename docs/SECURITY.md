# Security model

Платформа реализует несколько **различимых** механизмов аутентификации (по мотивам [7AuthConcepts](https://github.com/charset-8utf/7AuthConcepts)): каждый решает свою задачу и не подменяет OAuth2/OIDC целиком.

## Сравнение подходов

| Подход | Где используется | HTTP | Что проверяет сервер |
|--------|------------------|------|----------------------|
| **Basic Auth** | `user-service`, профиль `local` | `Authorization: Basic …` | username/password (только dev, поверх HTTPS) |
| **Bearer JWT** | Gateway, BFF, user-service, notification read API | `Authorization: Bearer …` | подпись, `exp`, `iss`, `roles` |
| **API Key** | notification-service write API | `X-API-Key: …` | credential клиента/интеграции (M2M) |
| **Service JWT** | notification-service write API | `Authorization: Bearer …` | `scope`, `aud`, `sub` (машинный клиент) |
| **Session-like refresh** | user-service | refresh token id → Redis | server-side state + rotation |
| **OAuth2 / OIDC** | опционально, профиль `auth` + Keycloak | OIDC code flow / JWKS | issuer, audience, identity claims |

> **JWT** — формат токена, **Bearer** — схема передачи. Фраза «используем JWT» без уточнения flow не описывает security-модель.

## Потоки

### Пользователь (cloud)

```text
POST /api/auth/login  →  user-service выдаёт access JWT (HS256) + refresh id (Redis)
GET  /api/users       →  nginx → gateway (Bearer validate) → TokenRelay → user-service
GET  /bff/me          →  nginx → BFF (Bearer validate) → gateway
```

### Машинный клиент (notification write)

```text
POST /api/notifications/email
  ├─ Authorization: Bearer <service-jwt>   (scope notifications:write)
  └─ X-API-Key: <integration-key>        (альтернатива для интеграций)
```

### OIDC (production profile)

```text
Keycloak (issuer) → access token (RS256, JWKS)
Gateway / BFF → validate issuer-uri + JWKS (без shared secret)
```

## Production checklist

| Требование | Dev | Prod (`values-prod.yaml`) |
|------------|-----|---------------------------|
| TLS edge | HTTP nginx | cert-manager + HTTPS ingress |
| TLS internal | `INSECURE_SSL=true` | `INSECURE_SSL=false`, truststore |
| Secrets | `.env` / Helm stringData | External Secrets Operator |
| JWT | HS256 shared secret | OIDC JWKS (Keycloak) или RS256 |
| DB | in-cluster | managed Postgres/Mongo (external URLs) |

См. также: [runbooks/SECURITY_INCIDENT.md](runbooks/SECURITY_INCIDENT.md), [adr/004-authentication-strategy.md](adr/004-authentication-strategy.md).
