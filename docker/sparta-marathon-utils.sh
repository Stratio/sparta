#!/bin/bash

function initClusterSparkIp() {

  if [ -v LIBPROCESS_IP ] && [ ${#LIBPROCESS_IP} != 0 ]; then
    echo "" >> ${VARIABLES}
    echo "export SPARK_LOCAL_IP=${LIBPROCESS_IP}" >> ${VARIABLES}
    echo "" >> ${SYSTEM_VARIABLES}
    echo "export SPARK_LOCAL_IP=${LIBPROCESS_IP}" >> ${SYSTEM_VARIABLES}
  fi

}

function initJavaOptions() {

 if [[ ! -v MARATHON_APP_HEAP_SIZE ]]; then
   export MARATHON_APP_HEAP_SIZE=-Xmx2048m
 fi

 if [[ ! -v MARATHON_APP_HEAP_MINIMUM_SIZE ]]; then
   export MARATHON_APP_HEAP_MINIMUM_SIZE=-Xms1024m
 fi

 if [ -v SPARTA_JAAS_FILE ] && [ ${#SPARTA_JAAS_FILE} != 0 ]; then
   export SPARTA_CONFIG_JAAS_FILE="-Djava.security.auth.login.config=${SPARTA_JAAS_FILE}"
 fi
}

function initSparkEnvOptions() {

  if [ -v CALICO_NETWORK ] && [ ${#CALICO_NETWORK} != 0 ]; then
    HOST="$(hostname --all-ip-addresses|xargs)"
    echo "Virutal network detected changed LIBPROCESS_IP $LIBPROCESS_IP to $HOST"
    export LIBPROCESS_IP=$HOST
  fi

 # if [ -v PORT_SPARKUI ] && [ ${#PORT_SPARKUI} != 0 ]; then
 #  sed -i "s|.*SPARK_MASTER_WEBUI_PORT.*|SPARK_MASTER_WEBUI_PORT=${PORT_SPARKUI}|" ${SPARK_ENV_FILE}
 # fi

}

function initSparkDefaultsOptions() {

  if [ -v PORT_SPARKUI ] && [ ${#PORT_SPARKUI} != 0 ]; then
    echo "spark.ui.port=${PORT_SPARKUI}" >> ${SPARK_CONF_DEFAULTS_FILE}
  fi

}