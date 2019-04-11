#!/bin/bash

### Get keytab
INFO "[KERBEROS] Downloading keytab from vault"
#Downloads principal and keytab for sparta
#This will be saved in /etc/sds/sparta/security/sparta.keytab

#The principal is saved to SPARTA_PRINCIPAL_NAME
getKrb userland "$TENANT_NAME" "$TENANT_NAME" "/etc/sds/sparta/security" SPARTA_PRINCIPAL_NAME
getKrb userland "$WORKFLOW_IDENTITY" "$WORKFLOW_IDENTITY" "/etc/sds/sparta/security/workflow" SPARTA_PRINCIPAL_NAME_WORKFLOW
INFO "[KERBEROS] Download ok , now exporting variables"

export SPARTA_PRINCIPAL_NAME=${SPARTA_PRINCIPAL_NAME}
echo "export SPARTA_PRINCIPAL_NAME=${SPARTA_PRINCIPAL_NAME}" >> ${VARIABLES}
echo "export SPARTA_PRINCIPAL_NAME_WORKFLOW=${SPARTA_PRINCIPAL_NAME_WORKFLOW}" >> ${VARIABLES}

INFO "[KERBEROS] Setting configuration options needed for securized Zookeeper"

##In sparta keytab is expected in SPARTA_KEYTAB_PATH
export SPARTA_KEYTAB_PATH=/etc/sds/sparta/security/$TENANT_NAME.keytab
export SPARTA_KEYTAB_PATH_WORKFLOW=/etc/sds/sparta/security/$WORKFLOW_IDENTITY.keytab
echo "export SPARTA_KEYTAB_PATH=${SPARTA_KEYTAB_PATH}" >> ${VARIABLES}
echo "export SPARTA_KEYTAB_PATH_WORKFLOW=${SPARTA_KEYTAB_PATH_WORKFLOW}" >> ${VARIABLES}

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
echo "export SPARTA_JAAS_FILE=${SPARTA_JAAS_FILE}" >> ${VARIABLES}

sed -i "s#<__PRINCIPAL__>#$SPARTA_PRINCIPAL_NAME#" $SPARTA_JAAS_FILE\
   && INFO "[KERBEROS] ZK principal configured as $SPARTA_PRINCIPAL_NAME" \
   || ERROR "[KERBEROS] ZK principal was NOT configured"
sed -i "s#<__KEYTAB__>#$SPARTA_KEYTAB_PATH#" $SPARTA_JAAS_FILE\
   && INFO "[KERBEROS] ZK keytab configured as $SPARTA_KEYTAB_PATH" \
   || ERROR "[KERBEROS] ZK keytab was NOT configured"

## Testing if the keytab can generate valid tickets
kinit -c /tmp/kb-cache -kt $SPARTA_KEYTAB_PATH $SPARTA_PRINCIPAL_NAME
if [ $? -eq 0 ]; then
    INFO "[KERBEROS] Keytab for $SPARTA_PRINCIPAL_NAME: validated"
    INFO "[KERBEROS] Configuring kerberos: OK"
else
    ERROR "[KERBEROS] The retrieved keytab could not be validated: it is likely that Sparta will run into some errors with kerberized services"
    ERROR "[KERBEROS] Configuring kerberos: WARNING!"
fi