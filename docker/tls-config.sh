#!/bin/bash
_log_sparta_sec "Getting app cert from vault"
#Downloads jks for the app
#This will be saved in /etc/sds/sparta/security/sparta.jks
#The password for the jks is saved to $SPARTA_KEYSTORE_PASS
getCert "userland" "$TENANT_NAME" "$TENANT_NAME" "JKS" "/etc/sds/sparta/security"

_log_sparta_sec "Exporting sparta tls variables"
##Export variables for the app to configure tls

export SPARTA_API_CERTIFICATE_FILE="/etc/sds/sparta/security/$TENANT_NAME.jks"
export SPARTA_API_CERTIFICATE_PASSWORD=$SPARTA_KEYSTORE_PASS

