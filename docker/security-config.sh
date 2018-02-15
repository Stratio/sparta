#!/bin/bash

INFO "[SECURITY-CONFIG] Loading kms-utils ... "
source /kms_utils.sh
INFO "[SECURITY-CONFIG] Loaded kms-utils"

if [ "$USE_DYNAMIC_AUTHENTICATION" = "true" ]; then
    INFO "[SECURITY-CONFIG] Dynamic authentication enabled. Obtaining token from Vault"
    login
    if [ $? != 0 ]; then
        INFO "[SECURITY-CONFIG] Login using dynamic authentication failed!"
        exit 1
    fi
fi

if [ -v VAULT_ENABLE ] && [ ${#VAULT_ENABLE} != 0 ] && [ $VAULT_ENABLE == "true" ] && [ -v VAULT_TOKEN ] && [ ${#VAULT_TOKEN} != 0 ]; then
  echo "" >> ${VARIABLES}
  echo "export VAULT_TOKEN=$VAULT_TOKEN" >> ${VARIABLES}
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export VAULT_TOKEN=$VAULT_TOKEN" >> ${SYSTEM_VARIABLES}
  echo "" >> ${VARIABLES}
  echo "export VAULT_PROTOCOL=https" >> ${VARIABLES}
  echo "" >> ${VARIABLES}
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export VAULT_PROTOCOL=https" >> ${SYSTEM_VARIABLES}
  echo "" >> ${VARIABLES}
  echo "export VAULT_HOST=$VAULT_HOSTS" >> ${VARIABLES}
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export VAULT_HOST=$VAULT_HOSTS" >> ${SYSTEM_VARIABLES}
fi

#Ensure security folder is created
mkdir -p /etc/sds/sparta/security

# Main execution

## Init
if [ -v MARATHON_APP_LABEL_DCOS_SERVICE_NAME ] && [ ${#MARATHON_APP_LABEL_DCOS_SERVICE_NAME} != 0 ]; then
export TENANT_NAME=${MARATHON_APP_LABEL_DCOS_SERVICE_NAME}
else
export TENANT_NAME='sparta'   # MARATHON_APP_ID without slash
fi

if [ -v DATASTORE_TRUSTSTORE_CA_NAME ] && [ ${#DATASTORE_TRUSTSTORE_CA_NAME} != 0 ]; then
export TRUSTSTORE_CA_NAME=${DATASTORE_TRUSTSTORE_CA_NAME}
else
export TRUSTSTORE_CA_NAME='ca'
fi

if [[ ! -v SPARTA_SECRET_FOLDER ]]; then
export SPARTA_SECRET_FOLDER="/etc/sds/sparta/security"
fi

if [[ ! -v SPARK_DRIVER_SECRET_FOLDER ]]; then
export SPARK_DRIVER_SECRET_FOLDER="/tmp/secrets"
fi

#Setup tenant_normalized for access kms_utils
export TENANT_UNDERSCORE=${TENANT_NAME//-/_}
export TENANT_NORM="${TENANT_UNDERSCORE^^}"

export SPARTA_TLS_JKS_NAME="/etc/sds/sparta/security/$TENANT_NAME.jks"
export SPARTA_TRUST_JKS_NAME="/etc/sds/sparta/security/truststore.jks"
export SPARTA_KEYTAB_NAME="/etc/sds/sparta/security/$TENANT_NAME.keytab"
export GOSEC_PLUGIN_JKS_NAME=${SPARTA_TLS_JKS_NAME}

export SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PATH="/v1/ca-trust/certificates/$TRUSTSTORE_CA_NAME"
export SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH="/v1/ca-trust/passwords/default/keystore"
export SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH="/v1/userland/certificates/$TENANT_NAME"
export SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH="/v1/userland/passwords/$TENANT_NAME/keystore"
export SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH=$SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH
export VAULT_PROTOCOL="https"


####################################################
## Get TLS Server Info and set SPARTA_KEYSTORE_PASS
####################################################
 if [ -v SECURITY_TLS_ENABLE ] && [ ${#SECURITY_TLS_ENABLE} != 0 ] && [ $SECURITY_TLS_ENABLE == "true" ]; then
  INFO "[SECURITY-CONFIG] Configuring tls ..."
  source /tls-config.sh
  INFO "[SECURITY-CONFIG] Configuring tls Ok"
 fi

#######################################################
## Create Sparta Truststore and set DEFAULT_KEYSTORE_PASS
#######################################################
if [ -v SECURITY_TRUSTSTORE_ENABLE ] && [ ${#SECURITY_TRUSTSTORE_ENABLE} != 0 ] && [ $SECURITY_TRUSTSTORE_ENABLE == "true" ]; then
  INFO "[SECURITY-CONFIG] Configuring truststore ..."
  source /truststore-config.sh
  INFO "[SECURITY-CONFIG] Configuring truststore OK"
fi

####################################################
## Kerberos config set SPARTA_PRINCIPAL_NAME and SPARTA_KEYTAB_PATH
####################################################
if [ -v SECURITY_KRB_ENABLE ] && [ ${#SECURITY_KRB_ENABLE} != 0 ] && [ $SECURITY_KRB_ENABLE == "true" ]; then
  INFO "[SECURITY-CONFIG] Configuring kerberos ..."
  source /kerberos-server-config.sh
fi

#######################################################
## Gosec-plugin config
#######################################################
if [ -v ENABLE_GOSEC_AUTH ] && [ ${#ENABLE_GOSEC_AUTH} != 0 ] && [ $ENABLE_GOSEC_AUTH == "true" ]; then
  INFO "[SECURITY-CONFIG] Configuring GoSec Dyplon plugin ..."
    source /gosec-config.sh
  INFO "[SECURITY-CONFIG] Configuring GoSec Dyplon plugin Ok"
fi

#######################################################
## Oauth config set OAUTH2_ENABLE OAUTH2_CLIENT_ID OAUTH2_CLIENT_SECRET
#######################################################
if [ -v SECURITY_OAUTH2_ENABLE ] && [ ${#SECURITY_OAUTH2_ENABLE} != 0 ] && [ $SECURITY_OAUTH2_ENABLE == "true" ]; then
  INFO "[SECURITY-CONFIG] Configuring Oauth ..."
  source /oauth2.sh
  INFO "[SECURITY-CONFIG] Configuring Oauth Ok"
fi

#######################################################
## MesosSecurity config set MESOS_USER and MESOS_PASS
#######################################################
if [ -v SECURITY_MESOS_ENABLE ] && [ ${#SECURITY_MESOS_ENABLE} != 0 ] && [ $SECURITY_MESOS_ENABLE == "true" ]; then
 INFO "[SECURITY-CONFIG] Configuring Mesos Security ..."
 source mesos-security.sh
 INFO "[SECURITY-CONFIG] Configuring Mesos Security Ok"
fi

#######################################################
## MarathonSecurity config set MARATHON_SSO_USERNAME and MARATHON_SSO_PASSWORD
#######################################################
if [ -v SECURITY_MARATHON_ENABLED ] && [ ${#SECURITY_MARATHON_ENABLED} != 0 ] && [ $SECURITY_MARATHON_ENABLED == "true" ]; then
 INFO "[SECURITY-CONFIG] Configuring Marathon Security ..."
 source marathon-sso-security.sh
 INFO "[SECURITY-CONFIG] Configuring Marathon Security Ok"
fi