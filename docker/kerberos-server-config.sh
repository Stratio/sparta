#!/bin/bash

### Get keytab
_log_sparta_sec "Downloading keytab from vault"
#Downloads principal and keytab for sparta
#This will be saved in /etc/sds/sparta/security/sparta.keytab
#The principal is saved to HADOOP_PRINCIPAL_NAME
getKrb userland "$TENANT_NAME" "$TENANT_NAME" "/etc/sds/sparta/security" HADOOP_PRINCIPAL_NAME
_log_sparta_sec "Download ok , now exporting variables"

echo "Setting configuration options needed for securized Zookeeper"

##In sparta keytab is expected in HADOOP_KEYTAB_PATH
export HADOOP_KEYTAB_PATH=/etc/sds/sparta/security/sparta.keytab

## Creating a jaas.conf that must be used to connect to Zookeeper if Zookeeper is securized

cat > /etc/sds/sparta/security/jaas.conf<<EOF
Client {
         com.sun.security.auth.module.Krb5LoginModule required
         useKeyTab=true
         storeKey=true
         useTicketCache=false
         keyTab="<__KEYTAB__>"
         principal="<__PRINCIPAL__>";
        };
EOF

export SPARTA_JAAS_FILE=/etc/sds/sparta/security/jaas.conf
#export SPARTA_PLUGIN_CLIENT_JAAS_PATH=$SPARTA_JAAS_FILE

sed -i "s#<__PRINCIPAL__>#$HADOOP_PRINCIPAL_NAME#" $SPARTA_JAAS_FILE\
   && echo "[JAAS_CONF] ZK principal configured as $HADOOP_PRINCIPAL_NAME" \
   || echo "[JAAS_CONF-ERROR] ZK principal was NOT configured"
sed -i "s#<__KEYTAB__>#$HADOOP_KEYTAB_PATH#" $SPARTA_JAAS_FILE\
   && echo "[JAAS_CONF] ZK keytab configured as $HADOOP_KEYTAB_PATH" \
   || echo "[JAAS_CONF-ERROR] ZK keytab was NOT configured"

echo "Kerberos to use against securized Zookeeper: OK"
