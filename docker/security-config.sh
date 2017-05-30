#!/bin/bash

function _log_sparta_sec() {
    local message=$1
    echo "[SPARTA-SECURITY] $message"
}

_log_sparta_sec "Setup vault hosts"
export VAULT_HOSTS=($VAULT_HOST)

_log_sparta_sec "Loading kms-utils ... "
source /kms_utils.sh
_log_sparta_sec "Loaded kms-utils"

#Ensure security folder is created
mkdir -p /etc/sds/sparta/security

# Main execution

## Init
export TENANT_NAME='sparta'   # MARATHON_APP_ID without slash
#Setup tenant_normalized for access kms_utils
export TENANT_UNDERSCORE=${TENANT_NAME//-/_}
export TENANT_NORM="${TENANT_UNDERSCORE^^}"



####################################################
## Get TLS Server Info and set SPARTA_KEYSTORE_PASS
####################################################
 if [ -v SECURITY_TLS_ENABLE ] && [ ${#SECURITY_TLS_ENABLE} != 0 ] && [ $SECURITY_TLS_ENABLE == "true" ]; then
  _log_sparta_sec "Configuring tls ..."
  source /tls-config.sh
  _log_sparta_sec "Configuring tls Ok"
 fi

#######################################################
## Create Sparta Truststore and set DEFAULT_KEYSTORE_PASS
#######################################################
if [ -v SECURITY_TRUSTSTORE_ENABLE ] && [ ${#SECURITY_TRUSTSTORE_ENABLE} != 0 ] && [ $SECURITY_TRUSTSTORE_ENABLE == "true" ]; then
  _log_sparta_sec "Configuring truststore ..."
  source /truststore-config.sh
  _log_sparta_sec "Configuring truststore OK"
fi

####################################################
## Kerberos config set SPARTA_PRINCIPAL_NAME and SPARTA_KEYTAB_PATH
####################################################
if [ -v SECURITY_KRB_ENABLE ] && [ ${#SECURITY_KRB_ENABLE} != 0 ] && [ $SECURITY_KRB_ENABLE == "true" ]; then
  _log_sparta_sec "Configuring kerberos ..."
  source /kerberos-server-config.sh
  _log_sparta_sec "Configuring kerberos Ok"
fi

#######################################################
## Oauth config set OAUTH2_ENABLE OAUTH2_CLIENT_ID OAUTH2_CLIENT_SECRET
#######################################################
if [ -v SECURITY_OAUTH2_ENABLE ] && [ ${#SECURITY_OAUTH2_ENABLE} != 0 ] && [ $SECURITY_OAUTH2_ENABLE == "true" ]; then
  _log_sparta_sec "Configuring Oauth ..."
  source /oauth2.sh
  _log_sparta_sec "Configuring Oauth Ok"
fi

#######################################################
## MesosSecurity config set MESOS_USER and MESOS_PASS
#######################################################
if [ -v SECURITY_MESOS_ENABLE ] && [ ${#SECURITY_MESOS_ENABLE} != 0 ] && [ $SECURITY_MESOS_ENABLE == "true" ]; then
 _log_sparta_sec "Configuring Mesos Security ..."
 source mesos-security.sh
 _log_sparta_sec "Configuring Mesos Security Ok"
fi