#!/bin/bash

function loadVariables() {
 source "${VARIABLES}"
 source "${SYSTEM_VARIABLES}"
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

 # if [ -v PORT_SPARKUI ] && [ ${#PORT_SPARKUI} != 0 ]; then
 #  sed -i "s|.*SPARK_MASTER_WEBUI_PORT.*|SPARK_MASTER_WEBUI_PORT=${PORT_SPARKUI}|" ${SPARK_ENV_FILE}
 # fi

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
    sed -i "s|.*sparta.hdfs.hdfsMaster.*|#sparta.hdfs.hdfsMaster = \""${HADOOP_CONF_URI}"\"|" ${SPARTA_CONF_FILE}
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
    sed -i "s|.*sparta.hdfs.hdfsMaster.*|#sparta.hdfs.hdfsMaster = \""${HADOOP_CONF_URI}"\"|" ${SPARTA_CONF_FILE}
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
    sed -i "s|.*sparta.hdfs.hdfsMaster.*|#sparta.hdfs.hdfsMaster = \""${HADOOP_FS_DEFAULT_NAME}"\"|" ${SPARTA_CONF_FILE}
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
    sed -i "s|.*sparta.hdfs.hdfsMaster.*|#sparta.hdfs.hdfsMaster = \""${HADOOP_FS_DEFAULT_NAME}"\"|" ${SPARTA_CONF_FILE}
    source hdfs_utils.sh
    generate_hdfs-conf-from-fs-not-secured
  fi
}

function logLevelOptions() {

  if [[ ! -v SERVICE_LOG_LEVEL ]]; then
    SERVICE_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.rootLogger.*|log4j.rootLogger= ${SERVICE_LOG_LEVEL}, stdout|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v SPARTA_LOG_LEVEL ]]; then
    SPARTA_LOG_LEVEL="INFO"
  fi
  sed -i "s|log4j.logger.com.stratio.sparta.*|log4j.logger.com.stratio.sparta= ${SPARTA_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v SPARTA_REDIRECTOR ]]; then
    SPARTA_REDIRECTOR="INFO"
  fi
  sed -i "s|log4j.logger.org.apache.spark.launcher.SpartaOutputRedirector.*|log4j.logger.org.apache.spark.launcher.SpartaOutputRedirector= ${SPARTA_REDIRECTOR}|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v CROSSDATA_LOG_LEVEL ]]; then
      CROSSDATA_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.com.stratio.crossdata.*|log4j.logger.com.stratio.crossdata= ${CROSSDATA_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v SPARK_LOG_LEVEL ]]; then
    SPARK_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.spark=.*|log4j.logger.org.apache.spark= ${SPARK_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  sed -i "s|log4j.logger.org.spark-project=.*|log4j.logger.org.spark-project= ${SPARK_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v HADOOP_LOG_LEVEL ]]; then
    HADOOP_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.hadoop=.*|log4j.logger.org.apache.hadoop= ${HADOOP_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v ZOOKEEPER_LOG_LEVEL ]]; then
    ZOOKEEPER_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.zookeeper.*|log4j.logger.org.apache.zookeeper= ${ZOOKEEPER_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
  sed -i "s|log4j.logger.org.I0Itec.zkclient.*|log4j.logger.org.I0Itec.zkclient= ${ZOOKEEPER_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v PARQUET_LOG_LEVEL ]]; then
    PARQUET_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.parquet.*|log4j.logger.org.apache.parquet= ${PARQUET_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v AVRO_LOG_LEVEL ]]; then
    AVRO_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.avro.*|log4j.logger.org.apache.avro= ${AVRO_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}

  if [[ ! -v HTTP_LOG_LEVEL ]]; then
    HTTP_LOG_LEVEL="ERROR"
  fi
  sed -i "s|log4j.logger.org.apache.http.*|log4j.logger.org.apache.http= ${HTTP_LOG_LEVEL}|" ${SPARK_LOG_CONFIG_FILE}
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
