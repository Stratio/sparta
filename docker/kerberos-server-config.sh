#!/bin/bash

### Get keytab
_log_sparta_sec "Downloading keytab from vault"
#Downloads principal and keytab for sparta
#This will be saved in /etc/sds/sparta/security/sparta.keytab

#The principal is saved to SPARTA_PRINCIPAL_NAME
getKrb userland "$TENANT_NAME" "$TENANT_NAME" "/etc/sds/sparta/security" SPARTA_PRINCIPAL_NAME
_log_sparta_sec "Download ok , now exporting variables"

export SPARTA_PRINCIPAL_NAME=${SPARTA_PRINCIPAL_NAME}
echo "" >> ${VARIABLES}
echo "export SPARTA_PRINCIPAL_NAME=${SPARTA_PRINCIPAL_NAME}" >> ${VARIABLES}

echo "Setting configuration options needed for securized Zookeeper"

##In sparta keytab is expected in SPARTA_KEYTAB_PATH
export SPARTA_KEYTAB_PATH=/etc/sds/sparta/security/$TENANT_NAME.keytab
echo "" >> ${VARIABLES}
echo "export SPARTA_KEYTAB_PATH=${SPARTA_KEYTAB_PATH}" >> ${VARIABLES}

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
echo "" >> ${VARIABLES}
echo "export SPARTA_JAAS_FILE=${SPARTA_JAAS_FILE}" >> ${VARIABLES}

sed -i "s#<__PRINCIPAL__>#$SPARTA_PRINCIPAL_NAME#" $SPARTA_JAAS_FILE\
   && echo "[JAAS_CONF] ZK principal configured as $SPARTA_PRINCIPAL_NAME" \
   || echo "[JAAS_CONF-ERROR] ZK principal was NOT configured"
sed -i "s#<__KEYTAB__>#$SPARTA_KEYTAB_PATH#" $SPARTA_JAAS_FILE\
   && echo "[JAAS_CONF] ZK keytab configured as $SPARTA_KEYTAB_PATH" \
   || echo "[JAAS_CONF-ERROR] ZK keytab was NOT configured"

## Testing if the keytab can generate valid tickets
kinit -c /tmp/kb-cache -kt $SPARTA_KEYTAB_PATH $SPARTA_PRINCIPAL_NAME
if [ $? -eq 0 ]; then
    _log_sparta_sec "Keytab for $SPARTA_PRINCIPAL_NAME: validated"
    _log_sparta_sec "Configuring kerberos: OK"
else
    _log_sparta_sec "The retrieved keytab could not be validated: it is likely that Sparta will run into some errors with kerberized services"
    _log_sparta_sec "Configuring kerberos: WARNING!"
fi