#!/usr/bin/env bash
# Chaos: restart Kafka container and verify platform recovers
set -euo pipefail

echo "Restarting Kafka..."
docker restart unp-kafka
sleep 15
docker ps --filter name=unp-kafka --format '{{.Status}}' | grep -qi up
echo "Kafka up. Run ./scripts/platform-e2e-cross-service.sh to verify recovery."
