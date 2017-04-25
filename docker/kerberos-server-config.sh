#!/bin/bash

### Get keytab
_log_sparta_sec "Downloading keytab from vault"
#Downloads principal and keytab for sparta
#This will be saved in /etc/sds/sparta/security/sparta.keytab

#The principal is saved to SPARTA_PRINCIPAL_NAME
getKrb userland "$TENANT_NAME" "$TENANT_NAME" "/etc/sds/sparta/security" SPARTA_PRINCIPAL_NAME
_log_sparta_sec "Download ok , now exporting variables"

echo "Setting configuration options needed for securized Zookeeper"


##In sparta keytab is expected in SPARTA_KEYTAB_PATH
export SPARTA_KEYTAB_PATH=/etc/sds/sparta/security/sparta.keytab

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


sed -i "s#<__PRINCIPAL__>#$SPARTA_PRINCIPAL_NAME#" $SPARTA_JAAS_FILE\
   && echo "[JAAS_CONF] ZK principal configured as $SPARTA_PRINCIPAL_NAME" \
   || echo "[JAAS_CONF-ERROR] ZK principal was NOT configured"
sed -i "s#<__KEYTAB__>#$SPARTA_KEYTAB_PATH#" $SPARTA_JAAS_FILE\
   && echo "[JAAS_CONF] ZK keytab configured as $SPARTA_KEYTAB_PATH" \
   || echo "[JAAS_CONF-ERROR] ZK keytab was NOT configured"

echo "Kerberos to use against securized Zookeeper: OK"
