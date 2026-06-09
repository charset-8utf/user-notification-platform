#!/usr/bin/env bash
# Dev PKI для east-west TLS: CA, серверные cert с SAN для compose, truststore для user-service RestClient.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
WORKDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/.work"
PASSWORD="${KEYSTORE_PASSWORD:-changeit}"
DAYS_CA=3650
DAYS_SERVER=825

NOTIFICATION_KEYSTORE="${ROOT}/notification-service/src/main/resources/keystore.p12"
USER_KEYSTORE="${ROOT}/user-service/src/main/resources/keystore.p12"
TRUSTSTORE="${ROOT}/user-service/src/main/resources/notification-truststore.p12"

rm -rf "${WORKDIR}"
mkdir -p "${WORKDIR}"

echo "==> Dev CA"
openssl genrsa -out "${WORKDIR}/ca.key" 4096
openssl req -x509 -new -nodes -key "${WORKDIR}/ca.key" -sha256 -days "${DAYS_CA}" \
  -out "${WORKDIR}/ca.crt" \
  -subj "/CN=User Notification Platform Dev CA/O=UNP Dev/C=RU"

write_server_cert() {
  local name="$1"
  local cn="$2"
  local san="$3"
  local keystore_out="$4"
  local alias="$5"

  echo "==> Server cert: ${name}"
  openssl genrsa -out "${WORKDIR}/${name}.key" 2048
  openssl req -new -key "${WORKDIR}/${name}.key" -out "${WORKDIR}/${name}.csr" \
    -subj "/CN=${cn}/O=UNP Dev/C=RU"
  openssl x509 -req -in "${WORKDIR}/${name}.csr" \
    -CA "${WORKDIR}/ca.crt" -CAkey "${WORKDIR}/ca.key" -CAcreateserial \
    -out "${WORKDIR}/${name}.crt" -days "${DAYS_SERVER}" -sha256 \
    -extfile <(printf '%s\n' "subjectAltName=${san}")

  openssl pkcs12 -export \
    -inkey "${WORKDIR}/${name}.key" \
    -in "${WORKDIR}/${name}.crt" \
    -certfile "${WORKDIR}/ca.crt" \
    -out "${WORKDIR}/${name}.p12" \
    -name "${alias}" \
    -password "pass:${PASSWORD}"

  cp "${WORKDIR}/${name}.p12" "${keystore_out}"
}

write_server_cert "notification-service" "notification-service" \
  "DNS:notification-service,DNS:localhost,IP:127.0.0.1" \
  "${NOTIFICATION_KEYSTORE}" "notification-service"

write_server_cert "user-service" "user-service" \
  "DNS:user-service,DNS:localhost,IP:127.0.0.1" \
  "${USER_KEYSTORE}" "user-service"

echo "==> Truststore (CA only) for RestClient"
rm -f "${TRUSTSTORE}"
keytool -importcert -noprompt \
  -alias platform-dev-ca \
  -file "${WORKDIR}/ca.crt" \
  -keystore "${TRUSTSTORE}" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}"

echo "Done."
echo "  notification keystore: ${NOTIFICATION_KEYSTORE}"
echo "  user-service keystore: ${USER_KEYSTORE}"
echo "  RestClient truststore: ${TRUSTSTORE}"
echo "Password: ${PASSWORD}"
