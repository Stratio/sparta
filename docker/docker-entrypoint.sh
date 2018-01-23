#!/bin/bash -e

 NAME=sparta
 VARIABLES="/etc/default/$NAME-variables"
 SYSTEM_VARIABLES="/etc/profile"
 SPARTA_CLASSPATH_DIR=/etc/sds/sparta
 SPARTA_CONF_FILE=/etc/sds/sparta/reference.conf

 source /b-log.sh

 export DOCKER_LOG_LEVEL=${DOCKER_LOG_LEVEL:-${BASH_LOG_LEVEL:-INFO}}
 eval LOG_LEVEL_${DOCKER_LOG_LEVEL}
 B_LOG --stdout true

 INFO "[ENTRYPOINT] Loading Sparta common functions ... "
 source /sparta-common.sh
 INFO "[ENTRYPOINT] Loaded Sparta common functions"

 ## Vault and secrets (configured if enabled)
 ###################################################
 if [ -v VAULT_ENABLE ] && [ ${#VAULT_ENABLE} != 0 ] && [ $VAULT_ENABLE == "true" ] && [ -v VAULT_HOSTS ] && [ ${#VAULT_HOSTS} != 0 ]; then
     INFO "[ENTRYPOINT] Executing Sparta security script ... "
     source /security-config.sh $1
     INFO "[ENTRYPOINT] Sparta security script executed correctly"
 fi

 if [[ ! -v SPARTA_APP_TYPE ]]; then
   SPARTA_APP_TYPE="server"
 fi
 case "$SPARTA_APP_TYPE" in
   "marathon") # In this type, Sparta run as spark driver inside the marathon app
     INFO "[ENTRYPOINT] Executing Sparta as marathon application ... "
     source /sparta-marathon.sh
     ;;
   *) # Default type: Sparta run as server streaming apps launcher
     INFO "[ENTRYPOINT] Executing Sparta as server application ... "
     source /sparta-server.sh
     ;;
 esac
