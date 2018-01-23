#!/bin/bash
INFO "[TLS-CONFIG] Getting app cert from vault"
#Downloads jks for the app
#This will be saved in /etc/sds/sparta/security/$TENANT_NAME.jks
#The password for the jks is saved to $SPARTA_KEYSTORE_PASS
getCert "userland" "$TENANT_NAME" "$TENANT_NAME" "JKS" "/etc/sds/sparta/security"

INFO "[TLS-CONFIG] Exporting sparta tls variables"
#1--- Export variables for the app to configure tls
export SPARTA_API_CERTIFICATE_FILE="/etc/sds/sparta/security/$TENANT_NAME.jks"
CERTIFICATE_KEYSTORE_PASSWORD_VARIABLE=${TENANT_NORM}_KEYSTORE_PASS
export SPARTA_API_CERTIFICATE_PASSWORD=${!CERTIFICATE_KEYSTORE_PASSWORD_VARIABLE}

#2--- GET certificates 
export SPARK_SSL_CERT_PATH="/tmp"

getCert "userland" "$TENANT_NAME" "$TENANT_NAME" "PEM" $SPARK_SSL_CERT_PATH || exit $?

#GET CA-BUNDLE for given CA and store in ca.crt
VAULT_URI="$VAULT_PROTOCOL://$VAULT_HOSTS:$VAULT_PORT"
JSON_KEY="${TRUSTSTORE_CA_NAME}_crt"
CA_BUNDLE=$(curl -k -XGET -H "X-Vault-Token:$VAULT_TOKEN" "$VAULT_URI/v1/ca-trust/certificates/$TRUSTSTORE_CA_NAME" -s |  jq -cMSr --arg fqdn "" ".data[\"$JSON_KEY\"]")

echo "$CA_BUNDLE" > ${SPARK_SSL_CERT_PATH}/ca.crt
sed -i 's/-----BEGIN CERTIFICATE-----/-----BEGIN CERTIFICATE-----\n/g' ${SPARK_SSL_CERT_PATH}/ca.crt
sed -i 's/-----END CERTIFICATE-----/\n-----END CERTIFICATE-----\n/g' ${SPARK_SSL_CERT_PATH}/ca.crt
sed -i 's/-----END CERTIFICATE----------BEGIN CERTIFICATE-----/-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----/g'  ${SPARK_SSL_CERT_PATH}/ca.crt

#3--- Format certs as postgres expect
fold -w64 "${SPARK_SSL_CERT_PATH}/${TENANT_NAME}.key" >> "${SPARK_SSL_CERT_PATH}/aux.key"
mv "${SPARK_SSL_CERT_PATH}/aux.key" "${SPARK_SSL_CERT_PATH}/${TENANT_NAME}.key"
openssl pkcs8 -topk8 -inform pem -in "${SPARK_SSL_CERT_PATH}/${TENANT_NAME}.key" -outform der -nocrypt -out "${SPARK_SSL_CERT_PATH}/key.pkcs8"
mv $SPARK_SSL_CERT_PATH/${TENANT_NAME}.pem $SPARK_SSL_CERT_PATH/cert.crt
