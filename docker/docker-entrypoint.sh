#!/bin/bash -e

function _log_sparta_entrypoint() {
    local message=$1
    echo "[SPARTA-ENTRYPOINT] $message"
}

 _log_sparta_entrypoint "Executing Sparta docker-entrypoint"

 NAME=sparta
 VARIABLES="/etc/default/$NAME-variables"
 SYSTEM_VARIABLES="/etc/profile"
 SPARTA_CONF_FILE=/etc/sds/sparta/reference.conf

 _log_sparta_entrypoint "Loading Sparta common functions"
 
 source /sparta-common.sh

 ## Vault and secrets (configured if enabled)
 ###################################################
 if [ -v VAULT_ENABLE ] && [ ${#VAULT_ENABLE} != 0 ] && [ $VAULT_ENABLE == "true" ] && [ -v VAULT_HOSTS ] && [ ${#VAULT_HOSTS} != 0 ]; then
     _log_sparta_entrypoint "Executing Sparta security script ... "
     source /security-config.sh $1
     _log_sparta_entrypoint "Sparta security script executed correctly"
 fi

 if [[ ! -v SPARTA_APP_TYPE ]]; then
   SPARTA_APP_TYPE="server"
 fi
 case "$SPARTA_APP_TYPE" in
   "marathon") # In this type, Sparta run as spark driver inside the marathon app
     _log_sparta_entrypoint "Executing Sparta as marathon application ... "
     source /sparta-marathon.sh
     ;;
   *) # Default type: Sparta run as server streaming apps launcher
   _log_sparta_entrypoint "Executing Sparta as server application ... "
     source /sparta-server.sh
     ;;
 esac
