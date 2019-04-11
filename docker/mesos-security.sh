#!/bin/bash

INFO "[MESOS-SECURITY] Obtaining and setting Mesos security configuration"

### Get Mesos user and pass
getPass "userland" "$TENANT_NAME" "mesos"

MESOS_USER=${TENANT_NORM}_MESOS_USER
MESOS_PASS=${TENANT_NORM}_MESOS_PASS

export SPARK_MESOS_PRINCIPAL=${!MESOS_USER}

echo "export SPARK_SECURITY_MESOS_ENABLE=true" >> ${VARIABLES}
echo "export SPARK_SECURITY_MESOS_VAULT_PATH=\"v1/userland/passwords/"${TENANT_NAME}"/mesos\"" >> ${VARIABLES}
echo "export SPARK_MESOS_PRINCIPAL=${SPARK_MESOS_PRINCIPAL}" >> ${VARIABLES}
export SPARK_MESOS_SECRET=${!MESOS_PASS}
echo "export SPARK_MESOS_SECRET=${SPARK_MESOS_SECRET}" >> ${VARIABLES}

if [[ ! -v SPARK_MESOS_ROLE ]]; then
   export SPARK_MESOS_ROLE=$TENANT_NAME
fi
echo "export SPARK_MESOS_ROLE=${SPARK_MESOS_ROLE}" >> ${VARIABLES}

