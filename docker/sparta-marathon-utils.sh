#!/bin/bash

function initJavaOptions() {

 if [[ ! -v MARATHON_APP_HEAP_SIZE ]]; then
   export MARATHON_APP_HEAP_SIZE=-Xmx2048m
 fi

 if [[ ! -v MARATHON_APP_HEAP_MINIMUM_SIZE ]]; then
   export MARATHON_APP_HEAP_MINIMUM_SIZE=-Xms1024m
 fi
}

function initSparkEnvOptions() {

 echo "No environment variables override yet"
 # if [ -v PORT_SPARKUI ] && [ ${#PORT_SPARKUI} != 0 ]; then
 #  sed -i "s|.*SPARK_MASTER_WEBUI_PORT.*|SPARK_MASTER_WEBUI_PORT=${PORT_SPARKUI}|" ${SPARK_ENV_FILE}
 # fi

}

function initSparkDefaultsOptions() {

  if [ -v PORT_SPARKUI ] && [ ${#PORT_SPARKUI} != 0 ]; then
    echo "spark.ui.port=${PORT_SPARKUI}" >> ${SPARK_CONF_DEFAULTS_FILE}
  fi

}