#!/bin/bash

set -e

export MESOS_WORK_DIR="/tmp"
export MESOS_QUORUM="1"
MESOS_MODE=${MODE:=master}
MESOS_VARIABLES=/etc/sds/mesos/mesos-variables-env.sh

mkdir -p /var/log/sds/mesos

nohup /opt/sds/mesos/sbin/mesos-${MESOS_MODE} > /var/log/sds/mesos/mesos.log &

if [[ ${MESOS_MODE} == "master" ]]; then
  echo "CONF DISPATCHER"
  sleep 5
  ./${SPARK_HOME}/sbin/start-mesos-dispatcher.sh --master mesos://`hostname`:5050
fi

tail -F /var/log/sds/mesos/mesos.log
