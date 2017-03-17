#!/bin/bash

echo "Starting Dyplon Gosec configuration"


#######################################################
## Get Gosec-plugin LDAP user and pass and set Sparta vars
#######################################################
if [[ ! -v ENABLE_GOSEC_AUTH ]]; then
    echo "GoSec security module not activated"
    export ENABLE_GOSEC_AUTH=false
fi

if [ $ENABLE_GOSEC_AUTH=="true" ]; then
    echo "Enabling Dyplon GoSec authorization"
    export SPARTA_SECURITY_MANAGER_CLASS=com.stratio.gosec.dyplon.plugins.sparta.GoSecSpartaSecurityManager
    export SPARTA_SECURITY_MANAGER_ENABLED=true
fi


##########################################
## Set GOSEC_LOCAL_IP
##########################################
if [ ! -z ${LIBPROCESS_IP+x} ]; then
   echo "Setting GOSEC_LOCAL_HOSTNAME"
   export GOSEC_LOCAL_HOSTNAME=$LIBPROCESS_IP
fi

echo "Obtaining LDAP user & password from vault"
### Get LDAP user and pass
getPass "userland" "$TENANT_NAME" "ldap"

LDAP_USER=${TENANT_NORM}_LDAP_USER
LDAP_PASS=${TENANT_NORM}_LDAP_PASS

export SPARTA_GOSEC_PLUGIN_LDAP_USER=${!LDAP_USER}
export SPARTA_GOSEC_PLUGIN_LDAP_PASS=${!LDAP_PASS}

echo "LDAP user & password from vault: OK"

#############################################
## Set Sparta_GoSec_PLUGIN_JKS_PASSWORD
#############################################

### Get keystore password
JKS_PASSWORD=${TENANT_NORM}_KEYSTORE_PASS
export SPARTA_GOSEC_PLUGIN_JKS_PASSWORD=${!JKS_PASSWORD}

#Set LDAP config
export SPARTA_PLUGIN_LDAP_PRINCIPAL=$SPARTA_GOSEC_PLUGIN_LDAP_USER
export SPARTA_PLUGIN_LDAP_CREDENTIALS=$SPARTA_GOSEC_PLUGIN_LDAP_PASS

#Set Kafka config
#TODO THIS SHOULD BE HERE?????? TRUSTSTORE WHEN IS CREATED????
export SPARTA_PLUGIN_KAFKA_TRUSTSTORE_PASSWORD=$SPARTA_TRUSTSTORE_PASSWORD
export SPARTA_PLUGIN_KAFKA_TRUSTSTORE=$SPARTA_TRUST_JKS_NAME
export SPARTA_PLUGIN_KAFKA_KEYSTORE=$GOSEC_PLUGIN_JKS_NAME
export SPARTA_PLUGIN_KAFKA_KEYSTORE_PASSWORD=$SPARTA_GOSEC_PLUGIN_JKS_PASSWORD
export SPARTA_PLUGIN_KAFKA_KEY_PASSWORD=$SPARTA_GOSEC_PLUGIN_JKS_PASSWORD

echo "Finished Dyplon Gosec configuration: OK"