#!/bin/bash


INFO "[JENKINS-CONFIG] Obtaining user & API Token from vault"
### Get Jenkins user and token
getPass "userland" "$SERVICE_ID_WITH_PATH" "jenkins"

JENKINS_USER=${SERVICE_ID_WITH_PATH_NORM}_JENKINS_USER
JENKINS_PASS=${SERVICE_ID_WITH_PATH_NORM}_JENKINS_PASS

export SPARTA_CICD_JENKINS_USER=${!JENKINS_USER}
export SPARTA_CICD_JENKINS_APITOKEN=${!JENKINS_PASS}


if [[ -z ${SPARTA_CICD_JENKINS_USER} ]]; then
    ERROR "[JENKINS-CONFIG] JENKINS User cannot be empty"
    exit 1
fi

if [[ ${#SPARTA_CICD_JENKINS_APITOKEN} -lt 6 ]]; then
    ERROR "[JENKINS-CONFIG] JENKINS Password must have at least 6 characters"
    exit 1
fi

INFO "[JENKINS-CONFIG] JENKINS user & password from vault: OK"