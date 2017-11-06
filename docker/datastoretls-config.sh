#!/bin/bash

#1--- Enable datastore ssl
export CROSSDATA_SERVER_SPARK_DATASTORE_SSL_ENABLE="true"
export CROSSDATA_SERVER_SPARK_EXECUTOR_APP_NAME=$TENANT_NAME
export VAULT_PROTOCOL="https"

#2--- GET certificates
export SPARK_SSL_CERT_PATH="/tmp"
SERVICE_ID=$TENANT_NAME
INSTANCE=$TENANT_NAME

getCert "userland" "$INSTANCE" "$SERVICE_ID" "PEM" $SPARK_SSL_CERT_PATH || exit $?

#GET CA-BUNDLE for given CA and store in caroot.crt
VAULT_URI="$VAULT_PROTOCOL://$VAULT_HOSTS:$VAULT_PORT"
JSON_KEY="${CROSSDATA_SERVER_SPARK_EXECUTOR_CA_NAME}_crt"
CA_BUNDLE=$(curl -k -XGET -H "X-Vault-Token:$VAULT_TOKEN" "$VAULT_URI/v1/ca-trust/certificates/$CROSSDATA_SERVER_SPARK_EXECUTOR_CA_NAME" -s |  jq -cMSr --arg fqdn "" ".data[\"$JSON_KEY\"]")

echo "$CA_BUNDLE" > ${SPARK_SSL_CERT_PATH}/caroot.crt
sed -i 's/-----BEGIN CERTIFICATE-----/-----BEGIN CERTIFICATE-----\n/g' ${SPARK_SSL_CERT_PATH}/caroot.crt
sed -i 's/-----END CERTIFICATE-----/\n-----END CERTIFICATE-----\n/g' ${SPARK_SSL_CERT_PATH}/caroot.crt
sed -i 's/-----END CERTIFICATE----------BEGIN CERTIFICATE-----/-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----/g'  ${SPARK_SSL_CERT_PATH}/caroot.crt


#3--- Format certs as postgres expect
fold -w64 "${SPARK_SSL_CERT_PATH}/${SERVICE_ID}.key" >> "${SPARK_SSL_CERT_PATH}/aux.key"
mv "${SPARK_SSL_CERT_PATH}/aux.key" "${SPARK_SSL_CERT_PATH}/${SERVICE_ID}.key"
openssl pkcs8 -topk8 -inform pem -in "${SPARK_SSL_CERT_PATH}/${SERVICE_ID}.key" -outform der -nocrypt -out "${SPARK_SSL_CERT_PATH}/key.pkcs8"
mv $SPARK_SSL_CERT_PATH/${SERVICE_ID}.pem $SPARK_SSL_CERT_PATH/cert.crt


#Datastore TLS  settings
export SPARK_SECURITY_DATASTORE_ENABLE="true"
export SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH="/v1/ca-trust/certificates/ca"
export SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH="/v1/ca-trust/passwords/default/keystore"
export SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH="/v1/userland/certificates/$SERVICE_ID"
export SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH="/v1/userland/passwords/$SERVICE_ID/keystore"
export SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH=$SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH