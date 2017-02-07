#!/bin/bash

### Get keytab
_log_sparta_sec "Downloading keytab from vault"
#Downloads principal and keytab for spartas
#This wil be saved in /etc/sds/sparta/security/sparta.keytab
#The principal is saved to $XD_PRINCIPAL
getKrb userland "$TENANT_NAME" "$TENANT_NAME" "/etc/sds/sparta/security" XD_PRINCIPAL
_log_sparta_sec "Download ok , now exporting variables"
##In sparta principal name is expected in HADOOP_PRINCIPAL_NAME
export HADOOP_PRINCIPAL_NAME=$XD_PRINCIPAL
##In sparta keytab is expected in HADOOP_KEYTAB_PATH
export HADOOP_KEYTAB_PATH=/etc/sds/sparta/security/sparta.keytab

