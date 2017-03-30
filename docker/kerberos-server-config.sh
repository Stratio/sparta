#!/bin/bash

### Get keytab
_log_sparta_sec "Downloading keytab from vault"
#Downloads principal and keytab for sparta
#This wil be saved in /etc/sds/sparta/security/sparta.keytab
#The principal is saved to HADOOP_PRINCIPAL_NAME
getKrb userland "$TENANT_NAME" "$TENANT_NAME" "/etc/sds/sparta/security" HADOOP_PRINCIPAL_NAME
_log_sparta_sec "Download ok , now exporting variables"
##In sparta keytab is expected in HADOOP_KEYTAB_PATH
export HADOOP_KEYTAB_PATH=/etc/sds/sparta/security/sparta.keytab
