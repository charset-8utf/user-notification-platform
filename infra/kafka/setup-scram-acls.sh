#!/usr/bin/env bash
# Топики + ACL после старта брокера. SCRAM users — в kafka_server_jaas.conf.
set -euo pipefail

BOOTSTRAP="${KAFKA_BOOTSTRAP_SERVERS:-kafka:29093}"
ADMIN_CFG="${KAFKA_ADMIN_CONFIG:-/etc/kafka/secrets/client-admin.properties}"
TOPIC="${KAFKA_TOPIC:-user-notifications}"
DLT_TOPIC="${TOPIC}.DLT"
PARTITIONS="${KAFKA_TOPIC_PARTITIONS:-3}"

echo "Waiting for Kafka at ${BOOTSTRAP}..."
for i in $(seq 1 40); do
  if kafka-topics --bootstrap-server "${BOOTSTRAP}" --command-config "${ADMIN_CFG}" --list >/dev/null 2>&1; then
    break
  fi
  sleep 3
  if [[ "${i}" -eq 40 ]]; then
    echo "Kafka not ready"
    exit 1
  fi
done

echo "Creating topics..."
kafka-topics --bootstrap-server "${BOOTSTRAP}" --command-config "${ADMIN_CFG}" \
  --create --if-not-exists --topic "${TOPIC}" --partitions "${PARTITIONS}" --replication-factor 1
kafka-topics --bootstrap-server "${BOOTSTRAP}" --command-config "${ADMIN_CFG}" \
  --create --if-not-exists --topic "${DLT_TOPIC}" --partitions "${PARTITIONS}" --replication-factor 1

echo "Applying ACLs..."
kafka-acls --bootstrap-server "${BOOTSTRAP}" --command-config "${ADMIN_CFG}" \
  --add --if-not-exists --allow-principal User:user-service \
  --operation Write --operation Describe --topic "${TOPIC}"

kafka-acls --bootstrap-server "${BOOTSTRAP}" --command-config "${ADMIN_CFG}" \
  --add --if-not-exists --allow-principal User:user-service \
  --operation IdempotentWrite --cluster

kafka-acls --bootstrap-server "${BOOTSTRAP}" --command-config "${ADMIN_CFG}" \
  --add --if-not-exists --allow-principal User:notification-service \
  --operation Read --operation Describe --topic "${TOPIC}"

kafka-acls --bootstrap-server "${BOOTSTRAP}" --command-config "${ADMIN_CFG}" \
  --add --if-not-exists --allow-principal User:notification-service \
  --operation Read --operation Describe --operation Write --topic "${DLT_TOPIC}"

kafka-acls --bootstrap-server "${BOOTSTRAP}" --command-config "${ADMIN_CFG}" \
  --add --if-not-exists --allow-principal User:notification-service \
  --operation Read --group notification-service

echo "Kafka ACL setup complete."
