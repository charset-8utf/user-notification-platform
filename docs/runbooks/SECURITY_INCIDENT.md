# Runbook: Security incident

## Утечка JWT secret / API key
1. Ротировать `APP_JWT_SECRET`, `APP_SERVICE_JWT_SECRET`, `APP_API_KEYS` в Vault / External Secrets
2. `helm upgrade` с новыми секретами
3. Invalidate refresh tokens: flush Redis `refresh:*` (user-service)
4. Перевыпустить Keycloak client secrets при OIDC

## Подозрение на компрометацию gateway
1. Отключить ingress (`edge.ingress.enabled=false`) или scale gateway to 0
2. Анализ access log nginx / gateway
3. Включить только OIDC (отключить HS256 fallback)

## Чеклист prod
- [ ] TLS на ingress (cert-manager)
- [ ] `APP_GATEWAY_INSECURE_SSL=false`
- [ ] External Secrets enabled
- [ ] Dev пароли не в values.yaml
