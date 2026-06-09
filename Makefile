.PHONY: help up up-full down smoke smoke-cloud smoke-oidc e2e-cross e2e-compensation chaos-kafka ci-fast ci-e2e ci-e2e-cloud ci-e2e-cloud-suite ci-full ci-down k8s-up k8s-build k8s-install k8s-smoke k8s-delete docker-prune

K8S_CONTEXT ?= user-service-platform

help:
	@echo "Targets:"
	@echo "  up           docker compose up -d --build"
	@echo "  up-full      compose with cloud + observability profiles"
	@echo "  down         compose down -v"
	@echo "  smoke        platform-smoke.sh (stack must be running)"
	@echo "  smoke-cloud  platform-smoke-cloud.sh"
	@echo "  smoke-oidc   Keycloak OIDC token via gateway (profiles cloud+auth)"
	@echo "  e2e-compensation  DLT → compensation → user FAILED"
	@echo "  ci-fast      ./gradlew check"
	@echo "  ci-e2e       build, compose up, legacy smoke"
	@echo "  ci-e2e-cloud compose cloud profile + gateway smoke"
	@echo "  ci-e2e-cloud-suite  smoke + cross-service + compensation"
	@echo "  ci-full      fast + legacy e2e + cloud suite + OIDC + security"
	@echo "  ci-down      stop all compose profiles"
	@echo "  k8s-up       ensure Docker Desktop Kubernetes is running"
	@echo "  k8s-build    docker compose build + import images into K8s node"
	@echo "  k8s-install  helm install (Docker Desktop K8s, values-dev)"
	@echo "  k8s-smoke    smoke test (nginx :80 → gateway + /bff)"
	@echo "  k8s-delete   helm uninstall platform release"

up:
	docker compose up -d --build

up-full:
	docker compose --profile cloud --profile observability up -d --build

down:
	./scripts/ci.sh e2e-down

smoke:
	./scripts/platform-smoke.sh

smoke-cloud:
	./scripts/platform-smoke-cloud.sh

smoke-oidc:
	chmod +x scripts/platform-smoke-oidc.sh
	./scripts/platform-smoke-oidc.sh

e2e-cross:
	chmod +x scripts/platform-e2e-cross-service.sh
	./scripts/platform-e2e-cross-service.sh

e2e-compensation:
	chmod +x scripts/platform-e2e-compensation.sh
	./scripts/platform-e2e-compensation.sh

chaos-kafka:
	chmod +x scripts/chaos/kafka-bounce.sh
	./scripts/chaos/kafka-bounce.sh

ci-fast:
	./scripts/ci.sh fast

ci-e2e:
	./scripts/ci.sh e2e

ci-e2e-cloud:
	./scripts/ci.sh e2e-cloud

ci-e2e-cloud-suite:
	./scripts/ci.sh e2e-cloud-suite

ci-full:
	./scripts/ci.sh full

ci-down:
	./scripts/ci.sh e2e-down

k8s-up:
	@docker desktop kubernetes status 2>/dev/null | grep -q 'State:.*running' \
	  || docker desktop kubernetes start
	@chmod +x scripts/k8s/setup-context.sh
	@K8S_CONTEXT=$(K8S_CONTEXT) ./scripts/k8s/setup-context.sh >/dev/null

k8s-build: k8s-up
	chmod +x scripts/k8s/load-images.sh
	./scripts/k8s/load-images.sh

k8s-install: k8s-up
	chmod +x scripts/k8s/install.sh scripts/k8s-smoke.sh scripts/k8s/load-images.sh
	./scripts/k8s/load-images.sh
	./scripts/k8s/install.sh

k8s-smoke:
	./scripts/k8s-smoke.sh

k8s-delete:
	helm uninstall platform -n platform 2>/dev/null || true

docker-prune:
	docker container prune -f
	docker image prune -af --filter "until=168h"
	docker builder prune -af
	docker volume prune -f
