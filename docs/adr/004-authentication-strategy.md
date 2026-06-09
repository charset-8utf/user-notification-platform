# ADR-004: Authentication strategy

## Status
Accepted

## Context
Нужны различимые механизмы auth (см. 7AuthConcepts) без подмены OAuth2/OIDC термином «JWT».

## Decision

| Сценарий | Механизм |
|----------|----------|
| Пользователь (dev) | Bearer JWT HS256, issuer `user-service`, refresh в Redis |
| Пользователь (prod) | OIDC (Keycloak), JWKS на gateway |
| M2M write notification | Service JWT **или** `X-API-Key` |
| Локальная отладка | Basic Auth (`local` profile) |

## Consequences
Документация: [SECURITY.md](../SECURITY.md). Prod secrets — External Secrets, не plain Helm values.
