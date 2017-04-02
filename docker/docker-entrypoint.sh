#!/bin/bash -e

 NAME=sparta
 VARIABLES="/etc/default/$NAME-variables"
 SYSTEM_VARIABLES="/etc/profile"
 SPARTA_CONF_FILE=/etc/sds/sparta/reference.conf

 source /sparta-common.sh

 ## Vault and secrets (configured if enabled)
 ###################################################
 if [ !  -z ${VAULT_HOST} ]; then
     source /security-config.sh $1
 fi

 if [[ ! -v SPARTA_APP_TYPE ]]; then
   SPARTA_APP_TYPE="server"
 fi
 case "$SPARTA_APP_TYPE" in
   "marathon") # In this type, Sparta run as spark driver inside the marathon app
     source /sparta-marathon.sh
     ;;
   *) # Default type: Sparta run as server streaming apps launcher
     source /sparta-server.sh
     ;;
 esac
