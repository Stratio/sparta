#!/bin/bash

INFO "[MARATHON-SSO] Obtaining and setting Mesos security configuration"

### Get Mesos user and pass
getPass "userland" "$TENANT_NAME" "sso"

SSO_USER=${TENANT_NORM}_SSO_USER
SSO_PASS=${TENANT_NORM}_SSO_PASS

export MARATHON_SSO_USERNAME=${!SSO_USER}
echo "" >> ${VARIABLES}
echo "export MARATHON_SSO_USERNAME=${MARATHON_SSO_USERNAME}" >> ${VARIABLES}
export MARATHON_SSO_PASSWORD=${!SSO_PASS}
echo "export MARATHON_SSO_PASSWORD=${MARATHON_SSO_PASSWORD}" >> ${VARIABLES}

