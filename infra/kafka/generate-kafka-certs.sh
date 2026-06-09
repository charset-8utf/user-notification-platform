#!/usr/bin/env bash
# TLS для Kafka broker (SASL_SSL) и kafka-truststore.p12 для Spring-клиентов.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TLS_WORK="${ROOT}/infra/tls/.work"
SECRETS="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/secrets"
PASSWORD="${KEYSTORE_PASSWORD:-changeit}"
DAYS_SERVER=825

if [[ ! -f "${TLS_WORK}/ca.crt" || ! -f "${TLS_WORK}/ca.key" ]]; then
  echo "CA not found. Run: ${ROOT}/infra/tls/generate-dev-certs.sh"
  exit 1
fi

mkdir -p "${SECRETS}"
WORKDIR="${SECRETS}/.work"
rm -rf "${WORKDIR}"
mkdir -p "${WORKDIR}"

echo "==> Kafka broker key + cert (CN=kafka)"
openssl genrsa -out "${WORKDIR}/kafka.key" 2048
openssl req -new -key "${WORKDIR}/kafka.key" -out "${WORKDIR}/kafka.csr" \
  -subj "/CN=kafka/O=UNP Dev/C=RU"
openssl x509 -req -in "${WORKDIR}/kafka.csr" \
  -CA "${TLS_WORK}/ca.crt" -CAkey "${TLS_WORK}/ca.key" -CAcreateserial \
  -out "${WORKDIR}/kafka.crt" -days "${DAYS_SERVER}" -sha256 \
  -extfile <(printf '%s\n' "subjectAltName=DNS:kafka,DNS:localhost,IP:127.0.0.1")

openssl pkcs12 -export \
  -inkey "${WORKDIR}/kafka.key" \
  -in "${WORKDIR}/kafka.crt" \
  -certfile "${TLS_WORK}/ca.crt" \
  -out "${WORKDIR}/kafka.p12" \
  -name kafka \
  -password "pass:${PASSWORD}"

keytool -importkeystore -noprompt \
  -srckeystore "${WORKDIR}/kafka.p12" -srcstoretype PKCS12 -srcstorepass "${PASSWORD}" \
  -destkeystore "${SECRETS}/kafka.server.keystore.jks" -deststoretype JKS -deststorepass "${PASSWORD}"

keytool -importcert -noprompt \
  -alias platform-dev-ca \
  -file "${TLS_WORK}/ca.crt" \
  -keystore "${SECRETS}/kafka.server.truststore.jks" \
  -storepass "${PASSWORD}" \
  -deststoretype JKS

keytool -importcert -noprompt \
  -alias platform-dev-ca \
  -file "${TLS_WORK}/ca.crt" \
  -keystore "${SECRETS}/kafka.client.truststore.jks" \
  -storepass "${PASSWORD}" \
  -deststoretype JKS

cp "${SECRETS}/kafka.client.truststore.jks" "${WORKDIR}/client-trust.jks"
keytool -importkeystore -noprompt \
  -srckeystore "${WORKDIR}/client-trust.jks" -srcstoretype JKS -srcstorepass "${PASSWORD}" \
  -destkeystore "${SECRETS}/kafka-truststore.p12" -deststoretype PKCS12 -deststorepass "${PASSWORD}"

cp "${SECRETS}/kafka-truststore.p12" "${ROOT}/user-service/src/main/resources/kafka-truststore.p12"
cp "${SECRETS}/kafka-truststore.p12" "${ROOT}/notification-service/src/main/resources/kafka-truststore.p12"

printf '%s\n' "${PASSWORD}" > "${SECRETS}/keystore_creds"
printf '%s\n' "${PASSWORD}" > "${SECRETS}/key_creds"
printf '%s\n' "${PASSWORD}" > "${SECRETS}/truststore_creds"

rm -rf "${WORKDIR}"
echo "Kafka TLS artifacts in ${SECRETS}"
