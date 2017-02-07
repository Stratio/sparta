#!/bin/bash
_log_sparta_sec "Getting app cert from vault"
#Downloads jks for the app
#This wil be saved in /etc/sds/sparta/security/sparta.jks
#The password for the jks is saved to $SPARTA_KEYSTORE_PASS
getCert "userland" "$TENANT_NAME" "sparta" "JKS" "/etc/sds/sparta/security"

_log_sparta_sec "Exporting sparta tls variables"
### Get keystore password
JKS_PASSWORD=${TENANT_NORM}_KEYSTORE_PASS
export XD_TLS_PASSWORD=${!JKS_PASSWORD}
export XD_TLS_JKS_NAME="/etc/sds/sparta/security/$TENANT_NAME.jks"

##Export variables for the app to configure tls
export SPRAY_CAN_SERVER_SSL_ENCRYPTION=on
export SPARTA_API_CERTIFICATE_FILE=$XD_TLS_JKS_NAME
export SPARTA_API_CERTIFICATE_PASSWORD=$XD_TLS_PASSWORD

