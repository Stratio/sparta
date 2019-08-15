#!/bin/bash
INFO "[TLS-CONFIG] Getting app cert from vault"
#Downloads jks for the app
#This will be saved in /etc/sds/sparta/security/$TENANT_NAME.jks
#The password for the jks is saved to $SPARTA_KEYSTORE_PASS
getCert "userland" "$SERVICE_ID_WITH_PATH" "$TENANT_NAME" "JKS" $SPARTA_SECRET_FOLDER

INFO "[TLS-CONFIG] Exporting sparta tls variables"
CERTIFICATE_KEYSTORE_PASSWORD_VARIABLE=${SERVICE_ID_WITH_PATH_NORM}_KEYSTORE_PASS
export SPARTA_TLS_KEYSTORE_PASSWORD=${!CERTIFICATE_KEYSTORE_PASSWORD_VARIABLE}

getCert "userland" "$SERVICE_ID_WITH_PATH" "$TENANT_NAME" "PEM" $SPARTA_SECRET_FOLDER || exit $?

#GET CA-BUNDLE for given CA and store in ca.crt
getCAbundle ${SPARTA_SECRET_FOLDER} PEM "ca.crt"

#3--- Format certs as postgres expect
fold -w64 "${SPARTA_SECRET_FOLDER}/${TENANT_NAME}.key" >> "${SPARTA_SECRET_FOLDER}/aux.key"
mv "${SPARTA_SECRET_FOLDER}/aux.key" "${SPARTA_SECRET_FOLDER}/${TENANT_NAME}.key"
openssl pkcs8 -topk8 -inform pem -in "${SPARTA_SECRET_FOLDER}/${TENANT_NAME}.key" -outform der -nocrypt -out "${SPARTA_SECRET_FOLDER}/key.pkcs8"
cp $SPARTA_SECRET_FOLDER/${TENANT_NAME}.pem $SPARTA_SECRET_FOLDER/cert.crt
export SPARTA_TLS_KEY_PKCS8="${SPARTA_SECRET_FOLDER}/key.pkcs8"
export SPARTA_TLS_ROOTCERT="${SPARTA_SECRET_FOLDER}/ca.crt"
export SPARTA_TLS_CERT="$SPARTA_SECRET_FOLDER/cert.crt"
export SPARTA_PEM_LOCATION="${SPARTA_SECRET_FOLDER}/${TENANT_NAME}.pem"
export SPARTA_PEM_KEY_LOCATION="${SPARTA_SECRET_FOLDER}/${TENANT_NAME}.key"
fold -w65 ${SPARTA_SECRET_FOLDER}/cert.crt > ${SPARTA_SECRET_FOLDER}/nginx_cert.crt


mkdir /usr/local/share/ca-certificates/sparta &&\
cp ${SPARTA_SECRET_FOLDER}/cert.crt /usr/local/share/ca-certificates/sparta/sparta_cert.crt &&\
update-ca-certificates
res=$?
if [[ $res == 0 ]]; then
INFO "[TLS-CONFIG] Successfully updated OS certificates"
else
INFO "[TLS-CONFIG] The OS certificates were not updated"
fi
