.PHONY: help up up-full down smoke smoke-cloud ci-fast ci-e2e ci-e2e-cloud ci-full ci-down k8s-create k8s-build k8s-install k8s-smoke k8s-delete docker-prune

help:
	@echo "Targets:"
	@echo "  up           docker compose up -d --build"
	@echo "  up-full      compose with cloud + observability profiles"
	@echo "  down         compose down -v"
	@echo "  smoke        platform-smoke.sh (stack must be running)"
	@echo "  smoke-cloud  platform-smoke-cloud.sh"
	@echo "  ci-fast      ./gradlew check"
	@echo "  ci-e2e       build, compose up, legacy smoke"
	@echo "  ci-e2e-cloud compose cloud profile + gateway smoke"
	@echo "  ci-full      fast + e2e + down + security"
	@echo "  ci-down      stop all compose profiles"

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

ci-fast:
	./scripts/ci.sh fast

ci-e2e:
	./scripts/ci.sh e2e

ci-e2e-cloud:
	./scripts/ci.sh e2e-cloud

ci-full:
	./scripts/ci.sh full

ci-down:
	./scripts/ci.sh e2e-down

k8s-create:
	kind create cluster --name unp --config scripts/k8s/kind-config.yaml

k8s-build:
	docker compose build user-service notification-service config-server api-gateway web-bff
	for img in user-service notification-service config-server api-gateway web-bff; do \
	  kind load docker-image "$$img:latest" --name unp; \
	done

k8s-install:
	chmod +x scripts/k8s/install.sh scripts/k8s-smoke.sh
	./scripts/k8s/install.sh --load-images

k8s-smoke:
	./scripts/k8s-smoke.sh

k8s-delete:
	helm uninstall platform -n platform 2>/dev/null || true
	kind delete cluster --name unp 2>/dev/null || true

docker-prune:
	docker container prune -f
	docker image prune -af --filter "until=168h"
	docker builder prune -af
	docker volume prune -f
