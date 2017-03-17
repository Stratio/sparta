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


export SPARTA_TLS_JKS_NAME="/etc/sds/sparta/security/$TENANT_NAME.jks"
export SPARTA_TRUST_JKS_NAME="/etc/sds/sparta/security/truststore.jks"
export SPARTA_KEYTAB_NAME="/etc/sds/sparta/security/$TENANT_NAME.keytab"
export GOSEC_PLUGIN_JKS_NAME="$SPARTA_TLS_JKS_NAME"


####################################################
## Get TLS Server Info and set SPARTA_KEYSTORE_PASS
####################################################
_log_sparta_sec "Configuring tls ..."
source /tls-config.sh
_log_sparta_sec "Configuring tls Ok"

#######################################################
## Create XD Truststore and set DEFAULT_KEYSTORE_PASS
#######################################################
_log_sparta_sec "Configuring truststore ..."
source /truststore-config.sh
_log_sparta_sec "Configuring truststore OK"

####################################################
## Kerberos config set SPARTA_PRINCIPAL_NAME and SPARTA_KEYTAB_PATH
####################################################
_log_sparta_sec "Configuring kerberos ..."
source /kerberos-server-config.sh
_log_sparta_sec "Configuring kerberos Ok"

#######################################################
## Gosec-plugin config
#######################################################
source gosec-config.sh

#######################################################
## Oauth config set OAUTH2_ENABLE OAUTH2_CLIENT_ID OAUTH2_CLIENT_SECRET
#######################################################
_log_sparta_sec "Configuring Oauth ..."
source /oauth2.sh
_log_sparta_sec "Configuring Oauth Ok"

#######################################################
## MesosSecurity config set MESOS_USER and MESOS_PASS
#######################################################
_log_sparta_sec "Configuring Mesos Security ..."
source mesos-security.sh
_log_sparta_sec "Configuring Mesos Security Ok"
