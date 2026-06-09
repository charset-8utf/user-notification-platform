# Демонстрация платформы

## 1. CI (1 мин)

Показать зелёный workflow **CI** в GitHub Actions: `./gradlew check`.

## 2. E2E в Docker (2 мин)

```bash
make ci-e2e
```

JWT login → create user → Mailpit → метрики Prometheus.

## 3. Cloud / Gateway (1 мин)

```bash
make ci-e2e-cloud
```

Login через nginx `http://localhost`, `GET /bff/me`.

## 4. DevOps (1 мин)

- GitHub `security.yml`: Gitleaks, Trivy
- Docker-образы: `./scripts/ci.sh build-docker-images`
- Helm: `make k8s-install` (локальный K8s)

## 5. Kubernetes (2 мин)

```bash
make k8s-up
make k8s-install
make k8s-smoke
```

## Тезисы

- Database per service, outbox, saga signaling, Strangler (direct + cloud).
- Service discovery через K8s DNS (без Eureka).
- Gradle + GitHub Actions CI/CD.
