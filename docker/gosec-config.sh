#!/bin/bash

#######################################################
## Get Gosec-plugin LDAP user and pass and set Sparta vars
#######################################################
export SPARTA_SECURITY_MANAGER_ENABLED=true


##########################################
## Set GOSEC_LOCAL_IP
##########################################
if [ ! -z ${LIBPROCESS_IP+x} ]; then
   INFO "[GOSEC-CONFIG] Setting GOSEC_LOCAL_HOSTNAME"
   export GOSEC_LOCAL_HOSTNAME=$LIBPROCESS_IP
fi

INFO "[GOSEC-CONFIG] Obtaining LDAP user & password from vault"
### Get LDAP user and pass
getPass "userland" "$TENANT_NAME" "ldap"

LDAP_USER=${TENANT_NORM}_LDAP_USER
LDAP_PASS=${TENANT_NORM}_LDAP_PASS

export SPARTA_GOSEC_PLUGIN_LDAP_USER=${!LDAP_USER}
export SPARTA_GOSEC_PLUGIN_LDAP_PASS=${!LDAP_PASS}


if [[ -z ${SPARTA_GOSEC_PLUGIN_LDAP_USER} ]]; then
    ERROR "[GOSEC-CONFIG] LDAP User cannot be empty"
    exit 1
fi

if [[ ${#SPARTA_GOSEC_PLUGIN_LDAP_PASS} -lt 6 ]]; then
    ERROR "[GOSEC-CONFIG] LDAP Password must have at least 6 characters"
    exit 1
fi

INFO "[GOSEC-CONFIG] LDAP user & password from vault: OK"


#Set LDAP config
export SPARTA_PLUGIN_LDAP_PRINCIPAL=${SPARTA_GOSEC_PLUGIN_LDAP_USER}
export SPARTA_PLUGIN_LDAP_CREDENTIALS=${SPARTA_GOSEC_PLUGIN_LDAP_PASS}




