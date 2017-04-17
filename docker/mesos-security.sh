#!/bin/bash

if [ ! -z ${MESOS_SECURITY_ENABLED+x} ]; then

    echo "Obtaining and setting Mesos security configuration"

    ### Get Mesos user and pass
    getPass "userland" "$TENANT_NAME" "mesos"

    MESOS_USER=${TENANT_NORM}_MESOS_USER
    MESOS_PASS=${TENANT_NORM}_MESOS_PASS

    export SPARK_MESOS_PRINCIPAL=${!MESOS_USER}
    export SPARK_MESOS_SECRET=${!MESOS_PASS}

    echo "Mesos security configuration: OK"
fi
