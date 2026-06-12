.PHONY: help up up-full down smoke smoke-cloud smoke-oidc e2e-cross e2e-compensation ci-fast ci-e2e ci-e2e-cloud ci-e2e-cloud-suite ci-full ci-down k8s-up k8s-build k8s-install k8s-install-build k8s-smoke k8s-e2e-cross k8s-e2e-compensation k8s-delete k8s-kyverno gitops-dev docker-prune

K8S_CONTEXT ?= user-service-platform

help:
	@echo "Targets:"
	@echo "  up           docker compose up -d --build"
	@echo "  up-full      compose with cloud + observability profiles"
	@echo "  down         compose down -v"
	@echo "  smoke        platform-smoke.sh (stack must be running)"
	@echo "  smoke-cloud  platform-smoke-cloud.sh"
	@echo "  smoke-oidc   Keycloak OIDC token via gateway (profiles cloud+auth)"
	@echo "  e2e-compensation  inbox fail → compensation → saga rollback (404)"
	@echo "  ci-fast      ./gradlew check"
	@echo "  ci-e2e       build, compose up, legacy smoke"
	@echo "  ci-e2e-cloud compose cloud profile + gateway smoke"
	@echo "  ci-e2e-cloud-suite  smoke + cross-service + compensation"
	@echo "  ci-full      fast + legacy e2e + cloud suite + OIDC + security"
	@echo "  ci-down      stop all compose profiles"
	@echo "  k8s-up       ensure Docker Desktop Kubernetes is running"
	@echo "  k8s-build         import local images into K8s (--if-needed)"
	@echo "  k8s-install       helm install only (images must already be in cluster)"
	@echo "  k8s-install-build build/import images + helm install"
	@echo "  k8s-smoke    smoke test (nginx :80 → gateway + /bff)"
	@echo "  k8s-e2e-cross / k8s-e2e-compensation  E2E via ingress"
	@echo "  k8s-delete   helm uninstall platform release"
	@echo "  k8s-kyverno  apply Kyverno policies (requires Kyverno installed)"
	@echo "  gitops-dev   register Argo CD Application (platform-dev)"

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
	@for i in $$(seq 1 60); do \
	  docker desktop kubernetes status 2>/dev/null | awk '/^State:/ {print $$2}' | grep -qx running \
	    && kubectl cluster-info >/dev/null 2>&1 && break; \
	  if [ $$i -eq 1 ]; then \
	    echo "Waiting for Docker Desktop Kubernetes (enable in Settings if disabled)..." >&2; \
	  fi; \
	  sleep 5; \
	done
	@chmod +x scripts/k8s/setup-context.sh
	@K8S_CONTEXT=$(K8S_CONTEXT) ./scripts/k8s/setup-context.sh >/dev/null

k8s-build: k8s-up
	chmod +x scripts/k8s/load-images.sh
	./scripts/k8s/load-images.sh --if-needed

k8s-install: k8s-up
	chmod +x scripts/k8s/install.sh scripts/k8s-smoke.sh
	./scripts/k8s/install.sh

k8s-install-build: k8s-up
	chmod +x scripts/k8s/install.sh scripts/k8s-smoke.sh scripts/k8s/load-images.sh
	./scripts/k8s/install.sh --build-images

k8s-smoke:
	./scripts/k8s-smoke.sh

k8s-e2e-cross:
	chmod +x scripts/platform-e2e-cross-service.sh
	GATEWAY_HTTP=http://localhost ./scripts/platform-e2e-cross-service.sh

k8s-e2e-compensation:
	chmod +x scripts/platform-e2e-compensation.sh
	GATEWAY_HTTP=http://localhost ./scripts/platform-e2e-compensation.sh

k8s-delete:
	helm uninstall platform -n platform 2>/dev/null || true

k8s-kyverno:
	kubectl apply -f deploy/kyverno/policies.yaml

gitops-dev:
	kubectl apply -f deploy/gitops/argocd/platform-dev.yaml

docker-prune:
	docker container prune -f
	docker image prune -af --filter "until=168h"
	docker builder prune -af
	docker volume prune -f
