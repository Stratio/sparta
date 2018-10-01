#!/bin/bash

function loadVariables() {

 source "${VARIABLES}"
 source "${SYSTEM_VARIABLES}"

  INFO "[SPARTA-AKKA] SPARTA_AKKA_HOST = ${SPARTA_AKKA_HOST} "
  INFO "[SPARTA-AKKA] SPARTA_AKKA_BIND_HOST = ${SPARTA_AKKA_BIND_HOST} "
}

function initAkkaPaths() {

  HOST_IN_USE=$HOST
  if [ -v CALICO_ENABLED ] && [ $CALICO_ENABLED == "true" ] && [ -v CALICO_NETWORK ] && [ ${#CALICO_NETWORK} != 0 ]; then
       DOCKER_HOST="hostname -f"
       if [[ "$(hostname -f)" =~ \. ]]; then
          DOCKER_HOST="$(hostname -f)"
       else
          DOCKER_HOST="$(hostname -i)"
       fi
      HOST_IN_USE=$DOCKER_HOST
  fi

  echo "export SPARTA_AKKA_HOST=${HOST_IN_USE}" >> ${VARIABLES}
  echo "export SPARTA_AKKA_BIND_HOST=${HOST_IN_USE}" >> ${VARIABLES}

  echo "export SPARTA_AKKA_HOST=${HOST_IN_USE}" >> ${SYSTEM_VARIABLES}
  echo "export SPARTA_AKKA_BIND_HOST=${HOST_IN_USE}" >> ${SYSTEM_VARIABLES}

}

function initPersistencePaths() {

  mkdir -p /var/sds/sparta/dg-agent
  mkdir -p /var/sds/sparta/dg-agent/persistence
  mkdir -p /var/sds/sparta/dg-agent/persistence/journal
  mkdir -p /var/sds/sparta/dg-agent/persistence/snapshots

  mkdir -p /var/sds/sparta/spark-driver
  mkdir -p /var/sds/sparta/spark-driver/persistence
  mkdir -p /var/sds/sparta/spark-driver/persistence/journal
  mkdir -p /var/sds/sparta/spark-driver/persistence/snapshots
}

function initSpark() {

  if [[ ! -v SPARK_HOME ]]; then
    SPARK_HOME="/opt/spark/dist"
  fi
  if [[ ! -v SPARK_ENV_FILE ]]; then
    SPARK_ENV_FILE="${SPARK_HOME}/conf/spark-env.sh"
  fi
  if [[ ! -v SPARK_CONF_DEFAULTS_FILE ]]; then
    SPARK_CONF_DEFAULTS_FILE="${SPARK_HOME}/conf/spark-defaults.conf"
  fi

  echo "" >> ${VARIABLES}
  echo "export SPARK_HOME=${SPARK_HOME}" >> ${VARIABLES}
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export SPARK_HOME=${SPARK_HOME}" >> ${SYSTEM_VARIABLES}
  echo "" >> ${VARIABLES}
  echo "export SPARK_ENV_FILE=${SPARK_ENV_FILE}" >> ${VARIABLES}
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export SPARK_ENV_FILE=${SPARK_ENV_FILE}" >> ${SYSTEM_VARIABLES}
  echo "export SPARK_CONF_DEFAULTS_FILE=${SPARK_CONF_DEFAULTS_FILE}" >> ${VARIABLES}
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export SPARK_CONF_DEFAULTS_FILE=${SPARK_CONF_DEFAULTS_FILE}" >> ${SYSTEM_VARIABLES}

  rm "${SPARK_HOME}/jars/curator-client-2.6.0.jar"
  rm "${SPARK_HOME}/jars/curator-recipes-2.6.0.jar"
  rm "${SPARK_HOME}/jars/curator-framework-2.6.0.jar"
  rm "${SPARK_HOME}/jars/zookeeper-3.4.6.jar"
  #rm "${SPARK_HOME}/jars/log4j-1.2.17.jar"
  #rm "${SPARK_HOME}/jars/slf4j-api-1.7.16.jar"
  #rm "${SPARK_HOME}/jars/slf4j-log4j12-1.7.16.jar"

}

function initSparkEnvOptions() {

  if [ -v CALICO_ENABLED ] && [ $CALICO_ENABLED == "true" ] && [ -v CALICO_NETWORK ] && [ ${#CALICO_NETWORK} != 0 ]; then
    HOST="$(hostname --all-ip-addresses|xargs)"
    INFO "[COMMON] Virutal network detected changed LIBPROCESS_IP $LIBPROCESS_IP to $HOST"
    export LIBPROCESS_IP=$HOST
  fi

}

function initHdfs() {

  if [[ -v HADOOP_USER_NAME ]]; then
    echo "" >> ${VARIABLES}
    echo "export HADOOP_USER_NAME=${HADOOP_USER_NAME}" >> ${VARIABLES}
    echo "" >> ${SYSTEM_VARIABLES}
    echo "export HADOOP_USER_NAME=${HADOOP_USER_NAME}" >> ${SYSTEM_VARIABLES}
  fi

  if [[ ! -v HADOOP_CONF_FROM_URI ]]; then
   HADOOP_CONF_FROM_URI="false"
  fi
  if [ $HADOOP_CONF_FROM_URI == "true" ] && [ -v HADOOP_CONF_URI ] && [ ${#HADOOP_CONF_URI} != 0 ]; then
    if [ ! -v HADOOP_CONF_DIR ] && [ ${#HADOOP_CONF_DIR} != 0 ]; then
      HADOOP_CONF_DIR=/opt/sds/hadoop/conf
    fi
    source hdfs_utils.sh
    generate_hdfs-conf-from-uri
  fi

  if [[ ! -v CORE_SITE_FROM_URI ]]; then
   CORE_SITE_FROM_URI="false"
  fi
  if [ $CORE_SITE_FROM_URI == "true" ] && [ -v HADOOP_CONF_URI ] && [ ${#HADOOP_CONF_URI} != 0 ]; then
    if [ ! -v HADOOP_CONF_DIR ] && [ ${#HADOOP_CONF_DIR} != 0 ]; then
      HADOOP_CONF_DIR=/opt/sds/hadoop/conf
    fi
    source hdfs_utils.sh
    generate_core-site-from-uri
  fi

  if [[ ! -v HADOOP_CONF_FROM_DFS ]]; then
   HADOOP_CONF_FROM_DFS="false"
  fi
  if [ $HADOOP_CONF_FROM_DFS == "true" ] && [ -v HADOOP_FS_DEFAULT_NAME ] && [ ${#HADOOP_FS_DEFAULT_NAME} != 0 ]; then
    if [ ! -v HADOOP_CONF_DIR ] && [ ${#HADOOP_CONF_DIR} != 0 ]; then
      HADOOP_CONF_DIR=/opt/sds/hadoop/conf
    fi
    if [[ ! -v HADOOP_DFS_ENCRYPT_DATA_TRANSFER ]]; then
       HADOOP_DFS_ENCRYPT_DATA_TRANSFER="true"
    fi
    if [[ ! -v HADOOP_SECURITY_TOKEN_USE_IP ]]; then
       HADOOP_SECURITY_TOKEN_USE_IP="false"
    fi
    if [[ ! -v HADOOP_MAP_REDUCE_FRAMEWORK_NAME ]]; then
       HADOOP_MAP_REDUCE_FRAMEWORK_NAME="mesos"
    fi
    if [[ ! -v HADOOP_SECURITY_AUTH ]]; then
       HADOOP_SECURITY_AUTH="kerberos"
    fi
    if [[ ! -v HADOOP_RPC_PROTECTION ]]; then
       HADOOP_RPC_PROTECTION="authentication"
    fi
    source hdfs_utils.sh
    generate_hdfs-conf-from-fs
  fi

  if [[ ! -v HADOOP_CONF_FROM_DFS_NOT_SECURED ]]; then
   HADOOP_CONF_FROM_DFS_NOT_SECURED="false"
  fi
  if [ $HADOOP_CONF_FROM_DFS_NOT_SECURED == "true" ] && [ -v HADOOP_FS_DEFAULT_NAME ] && [ ${#HADOOP_FS_DEFAULT_NAME} != 0 ]; then
    if [ ! -v HADOOP_CONF_DIR ] && [ ${#HADOOP_CONF_DIR} != 0 ]; then
      HADOOP_CONF_DIR=/opt/sds/hadoop/conf
    fi
    source hdfs_utils.sh
    generate_hdfs-conf-from-fs-not-secured
  fi

}

function logLevelOptions() {

  if [[ ! -v SERVICE_LOG_LEVEL ]]; then
    SERVICE_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.rootLogger.*|log4j.rootLogger= ${SERVICE_LOG_LEVEL}, stdout|" ${SPARK_LOG_CONFIG_FILE}
  echo "export SERVICE_LOG_LEVEL=${SERVICE_LOG_LEVEL}" >> ${VARIABLES}
  echo "export SERVICE_LOG_LEVEL=${SERVICE_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v SPARTA_LOG_LEVEL ]]; then
    SPARTA_LOG_LEVEL="INFO"
  fi
  sed -i "s|log4j.logger.com.stratio.sparta.*|log4j.logger.com.stratio.sparta= ${SPARTA_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export SPARTA_LOG_LEVEL=${SPARTA_LOG_LEVEL}" >> ${VARIABLES}
  echo "export SPARTA_LOG_LEVEL=${SPARTA_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v SPARTA_REDIRECTOR_LOG_LEVEL ]]; then
    SPARTA_REDIRECTOR_LOG_LEVEL="INFO"
  fi
  sed -i "s|log4j.logger.org.apache.spark.launcher.SpartaOutputRedirector.*|log4j.logger.org.apache.spark.launcher.SpartaOutputRedirector= ${SPARTA_REDIRECTOR_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export SPARTA_REDIRECTOR_LOG_LEVEL=${SPARTA_REDIRECTOR_LOG_LEVEL}" >> ${VARIABLES}
  echo "export SPARTA_REDIRECTOR_LOG_LEVEL=${SPARTA_REDIRECTOR_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v CROSSDATA_LOG_LEVEL ]]; then
      CROSSDATA_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.com.stratio.crossdata.*|log4j.logger.com.stratio.crossdata= ${CROSSDATA_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export CROSSDATA_LOG_LEVEL=${CROSSDATA_LOG_LEVEL}" >> ${VARIABLES}
  echo "export CROSSDATA_LOG_LEVEL=${CROSSDATA_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v SPARK_LOG_LEVEL ]]; then
    SPARK_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.spark=.*|log4j.logger.org.apache.spark= ${SPARK_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  sed -i "s|log4j.logger.org.spark-project=.*|log4j.logger.org.spark-project= ${SPARK_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export SPARK_LOG_LEVEL=${SPARK_LOG_LEVEL}" >> ${VARIABLES}
  echo "export SPARK_LOG_LEVEL=${SPARK_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v HADOOP_LOG_LEVEL ]]; then
    HADOOP_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.hadoop=.*|log4j.logger.org.apache.hadoop= ${HADOOP_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export HADOOP_LOG_LEVEL=${HADOOP_LOG_LEVEL}" >> ${VARIABLES}
  echo "export HADOOP_LOG_LEVEL=${HADOOP_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v ZOOKEEPER_LOG_LEVEL ]]; then
    ZOOKEEPER_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.zookeeper.*|log4j.logger.org.apache.zookeeper= ${ZOOKEEPER_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  sed -i "s|log4j.logger.org.I0Itec.zkclient.*|log4j.logger.org.I0Itec.zkclient= ${ZOOKEEPER_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export ZOOKEEPER_LOG_LEVEL=${ZOOKEEPER_LOG_LEVEL}" >> ${VARIABLES}
  echo "export ZOOKEEPER_LOG_LEVEL=${ZOOKEEPER_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v PARQUET_LOG_LEVEL ]]; then
    PARQUET_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.parquet.*|log4j.logger.org.apache.parquet= ${PARQUET_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export PARQUET_LOG_LEVEL=${PARQUET_LOG_LEVEL}" >> ${VARIABLES}
  echo "export PARQUET_LOG_LEVEL=${PARQUET_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v AVRO_LOG_LEVEL ]]; then
    AVRO_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.avro.*|log4j.logger.org.apache.avro= ${AVRO_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export AVRO_LOG_LEVEL=${AVRO_LOG_LEVEL}" >> ${VARIABLES}
  echo "export AVRO_LOG_LEVEL=${AVRO_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v HTTP_LOG_LEVEL ]]; then
    HTTP_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.http.*|log4j.logger.org.apache.http= ${HTTP_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export HTTP_LOG_LEVEL=${HTTP_LOG_LEVEL}" >> ${VARIABLES}
  echo "export HTTP_LOG_LEVEL=${HTTP_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}

  if [[ ! -v SLICK_LOG_LEVEL ]]; then
    SLICK_LOG_LEVEL="ERROR"
  fi
  sed -i "s|com.typesafe.slick.*|com.typesafe.slick= ${SLICK_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  echo "export SLICK_LOG_LEVEL=${SLICK_LOG_LEVEL}" >> ${VARIABLES}
  echo "export SLICK_LOG_LEVEL=${SLICK_LOG_LEVEL}" >> ${SYSTEM_VARIABLES}
}

function logLevelAppender() {

  if [ -v LOG_APPENDER ] && [ $LOG_APPENDER == "file" ]; then
    cp "${SERVER_PROPERTIES}/log4j2-file.xml" "${LOG_CONFIG_FILE}"
  else
    cp "${SERVER_PROPERTIES}/log4j2-console.xml" "${LOG_CONFIG_FILE}"
  fi
  cp "${LOG_CONFIG_FILE}" "${SPARK_HOME}/conf/log4j2.xml"
  cp "${SPARK_LOG_CONFIG_FILE}" "${SPARK_HOME}/conf/log4j.properties"
  cp "${SPARK_LOG_CONFIG_FILE}" "${SPARK_HOME}/conf/log4j.properties.template"

}
