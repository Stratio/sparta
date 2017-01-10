#!/bin/bash -xe
 SPARTA_CONF_FILE=/etc/sds/sparta/application.conf

 # SPARK DOWNLOAD OPTIONS
  if [[ ! -v SPARK_HOME ]]; then
   SPARK_HOME="/opt/sds/spark"
 fi
 if [[ ! -v DOWNLOAD_SPARK ]]; then
   DOWNLOAD_SPARK=false
 fi
 if [[ ! -v SPARK_VERSION ]]; then
   SPARK_VERSION=spark-1.6.2
 fi
 if [[ ! -v HADOOP_SPARK_VERSION ]]; then
   HADOOP_SPARK_VERSION=hadoop2.6
 fi

  # HADOOP DOWNLOAD OPTIONS
  if [[ ! -v HADOOP_HOME ]]; then
   HADOOP_HOME="/opt/sds/hadoop"
 fi
 if [[ ! -v DOWNLOAD_HADOOP ]]; then
   DOWNLOAD_HADOOP=false
 fi
 if [[ ! -v HADOOP_VERSION ]]; then
   HADOOP_VERSION=hadoop-2.7.1
 fi

 # SPARTA API OPTIONS
 if [[ ! -v SPARTA_API_HOST ]]; then
   SPARTA_API_HOST=0.0.0.0
 fi
  if [[ ! -v SPARTA_API_HOST ]]; then
   SPARTA_API_PORT=9090
 fi
   if [[ ! -v SPARTA_API_CERTIFICATE_FILE ]]; then
   SPARTA_API_CERTIFICATE_FILE="/home/user/certifications/stratio.jks"
 fi
   if [[ ! -v SPARTA_API_CERTIFICATE_PASSWORD ]]; then
   SPARTA_API_CERTIFICATE_PASSWORD=stratio
 fi

 # SPARTA SWAGGER OPTIONS
 if [[ ! -v SPARTA_SWAGGER_HOST ]]; then
   SPARTA_API_HOST=0.0.0.0
 fi
  if [[ ! -v SPARTA_SWAGGER_HOST ]]; then
   SPARTA_API_PORT=9091
 fi

 # SPARTA ZOOKEEPER OPTIONS
  if [[ ! -v SPARTA_ZOOKEEPER_CONNECTION_STRING ]]; then
   SPARTA_ZOOKEEPER_CONNECTION_STRING="localhost:2181"
 fi
  if [[ ! -v SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT ]]; then
   SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT=15000
 fi
  if [[ ! -v SPARTA_ZOOKEEPER_SESSION_TIMEOUT ]]; then
   SPARTA_ZOOKEEPER_SESSION_TIMEOUT=60000
 fi
  if [[ ! -v SPARTA_ZOOKEEPER_RETRY_ATEMPTS ]]; then
   SPARTA_ZOOKEEPER_RETRY_ATEMPTS=5
 fi
  if [[ ! -v SPARTA_ZOOKEEPER_RETRY_INTERVAL ]]; then
   SPARTA_ZOOKEEPER_RETRY_INTERVAL=10000
 fi

 # SPARTA AKKA OPTIONS
   if [[ ! -v SPARTA_AKKA_CONTROLLER_INSTANCES ]]; then
   SPARTA_AKKA_CONTROLLER_INSTANCES=5
 fi

 # SPARTA CONFIGURATION OPTIONS
 if [[ ! -v SPARTA_EXECUTION_MODE ]]; then
   SPARTA_EXECUTION_MODE=local
 fi
 if [[ ! -v SPARTA_STOP_GRACEFULLY ]]; then
   SPARTA_STOP_GRACEFULLY=true
 fi
 if [[ ! -v SPARTA_AWAIT_STREAMING_CONTEXT_STOP ]]; then
   SPARTA_AWAIT_STREAMING_CONTEXT_STOP=120s
 fi
  if [[ ! -v SPARTA_AWAIT_SPARK_CONTEXT_STOP ]]; then
   SPARTA_AWAIT_SPARK_CONTEXT_STOP=60s
 fi
  if [[ ! -v SPARTA_AWAIT_POLICY_CHANGE_STATUS ]]; then
   SPARTA_AWAIT_POLICY_CHANGE_STATUS=120s
 fi
 if [[ ! -v SPARTA_REMEMBER_PARTITIONER ]]; then
   SPARTA_REMEMBER_PARTITIONER=true
 fi
  if [[ ! -v SPARTA_CHECKPOINT_PATH ]]; then
   SPARTA_CHECKPOINT_PATH="/tmp/stratio/sparta/checkpoint"
 fi
  if [[ ! -v SPARTA_AUTO_DELETE_CHECKPOINT ]]; then
   SPARTA_AUTO_DELETE_CHECKPOINT=true
 fi

 # HDFS OPTIONS
 if [[ ! -v HDFS_USER_NAME ]]; then
   HDFS_USER_NAME=stratio
 fi
 if [[ ! -v HDFS_MASTER ]]; then
   HDFS_MASTER=localhost
 fi
 if [[ ! -v HDFS_PORT ]]; then
   HDFS_PORT=9000
 fi
  if [[ ! -v HDFS_PLUGINS_FOLDER ]]; then
   HDFS_PLUGINS_FOLDER=plugins
 fi
  if [[ ! -v HDFS_DRIVER_FOLDER ]]; then
   HDFS_DRIVER_FOLDER=jarDriver
 fi
  if [[ ! -v HDFS_CLASSPATH_FOLDER ]]; then
   HDFS_CLASSPATH_FOLDER=classpath
 fi
  if [[ ! -v HDFS_PRINCIPAL_NAME ]]; then
   HDFS_PRINCIPAL_NAME=""
 fi
 if [[ ! -v HDFS_PRINCIPAL_NAME_SUFFIX ]]; then
   HDFS_PRINCIPAL_NAME_SUFFIX=""
 fi
 if [[ ! -v HDFS_PRINCIPAL_NAME_PREFIX ]]; then
   HDFS_PRINCIPAL_NAME_PREFIX=""
 fi
 if [[ ! -v HDFS_KEYTAB ]]; then
   HDFS_KEYTAB=""
 fi
 if [[ ! -v HDFS_KEYTAB_RELOAD ]]; then
   HDFS_KEYTAB_RELOAD=23h
 fi


 # LOCAL EXECUTION OPTIONS
 if [[ ! -v SPARK_MASTER ]]; then
   SPARK_MASTER="local[*]"
 fi

 # MESOS EXECUTION OPTIONS
 if [[ ! -v MESOS_MASTER ]]; then
   MESOS_MASTER=localhost:7077
 fi

 # YARN EXECUTION OPTIONS
  if [[ ! -v MESOS_MASTER ]]; then
   MESOS_MASTER=localhost:7077
 fi

 # STANDALONE EXECUTION OPTIONS
 if [[ ! -v MESOS_MASTER ]]; then
   MESOS_MASTER=localhost:7077
 fi


 # OAUTH2 OPTIONS
 if [[ ! -v OAUTH2_ENABLE ]]; then
   OAUTH2_ENABLE=false
 fi
 if [[ ! -v OAUTH2_COOKIE_NAME ]]; then
   OAUTH2_COOKIE_NAME=user
 fi
 if [[ ! -v OAUTH2_SSL_AUTHORIZE ]]; then
   OAUTH2_URL_AUTHORIZE="https://server.domain:9005/cas/oauth2.0/authorize"
 fi
 if [[ ! -v OAUTH2_URL_ACCESS_TOKEN ]]; then
   OAUTH2_URL_ACCESS_TOKEN="https://server.domain:9005/cas/oauth2.0/accessToken"
 fi
 if [[ ! -v OAUTH2_URL_PROFILE ]]; then
   OAUTH2_URL_PROFILE="https://server.domain:9005/cas/oauth2.0/profile"
 fi
 if [[ ! -v OAUTH2_URL_LOGOUT ]]; then
   OAUTH2_URL_LOGOUT="https://server.domain:9005/cas/logout"
 fi
 if [[ ! -v OAUTH2_URL_CALLBACK ]]; then
   OAUTH2_URL_CALLBACK="http://callback.domain:9090/login"
 fi
 if [[ ! -v OAUTH2_URL_ON_LOGIN_GO_TO ]]; then
   OAUTH2_URL_ON_LOGIN_GO_TO="/"
 fi
 if [[ ! -v OAUTH2_CLIENT_ID ]]; then
   OAUTH2_CLIENT_ID="userid"
 fi
 if [[ ! -v OAUTH2_CLIENT_SECRET ]]; then
   OAUTH2_CLIENT_SECRET="usersecret"
 fi

 # AKKA OPTIONS
  if [[ ! -v AKKA_LOG_DEAD_LETTERS ]]; then
   AKKA_LOG_DEAD_LETTERS=off
 fi

 # SPRAY OPTIONS
  if [[ ! -v SPR_CAN_SERVER_SSL_ENCRYPTION ]]; then
   SPR_CAN_SERVER_SSL_ENCRYPTION=off
 fi

 sed -i "s|executionMode.*|executionMode = \"${EXECUTION_MODE}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|stopGracefully.*|stopGracefully = \"${STOP_GRACEFULLY}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|awaitStopTermination.*|awaitStopTermination = \"${AWAIT_STOP_TERMINATION}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|rememberPartitioner.*|rememberPartitioner = \"${REMEMBER_PARTITIONER}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|autoDeleteCheckpoint.*|autoDeleteCheckpoint = \"${AUTO_DELETE_CHECKPOINT}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|connectionString.*|connectionString = \""${ZOOKEEPER_HOST}"\"|" ${SPARTA_CONF_FILE}
 sed -i "s|hdfsMaster.*|hdfsMaster = \"${HDFS_MASTER}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|hdfsPort.*|hdfsPort = \"${HDFS_PORT}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|hadoopUserName.*|hadoopUserName = \"${HDFS_USER_NAME}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|principalName.*|principalName = \"${HDFS_PRINCIPAL_NAME}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|principalNamePrefix.*|principalNamePrefix = \"${HDFS_PRINCIPAL_NAME_PREFIX}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|principalNameSuffix.*|principalNameSuffix = \"${HDFS_PRINCIPAL_NAME_SUFFIX}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|keytabPath.*|keytabPath = \"${HDFS_KEYTAB}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|reloadKeyTabTime.*|reloadKeyTabTime = \"${HDFS_KEYTAB_RELOAD}\"|" ${SPARTA_CONF_FILE}


 if [[ ! -v EXECUTION_MODE || "${EXECUTION_MODE}" == "local" ]]; then
   sed -i "s|spark.master.*|spark.master = \"${SPARK_MASTER}\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${EXECUTION_MODE}" == "mesos" || "${EXECUTION_MODE}" == "yarn" || "${EXECUTION_MODE}" == "standalone" ]]; then
   SPARK_HADOOP_VERSION_FILE="${SPARK_VERSION}-bin-${HADOOP_SPARK_VERSION}.tgz"
   HADOOP_VERSION_FILE="${HADOOP_VERSION}.tar.gz"
   CORE_SITE=core-site.xml
   HDFS_SITE=hdfs-site.xml
   YARN_SITE=yarn-site.xml.xml
   MAPRED_SITE=mapred-site.xml
   SPARK_HOME="/${SPARK_VERSION}-bin-${HADOOP_SPARK_VERSION}"
   SPARTA_VARIABLES=/etc/default/sparta-variables
   wget "http://apache.rediris.es/spark/${SPARK_VERSION}/${SPARK_HADOOP_VERSION_FILE}"
   tar -xvzf ${SPARK_HADOOP_VERSION_FILE}
   rm ${SPARK_HADOOP_VERSION_FILE}
   wget "http://www.eu.apache.org/dist/hadoop/common/${HADOOP_VERSION}/${HADOOP_VERSION_FILE}"
   tar -xvzf ${HADOOP_VERSION_FILE}
   rm ${HADOOP_VERSION_FILE}
   mkdir "/${HADOOP_VERSION}/conf"
   wget "http://${HDFS_MASTER}:50070/conf"
   cp conf "/${HADOOP_VERSION}/conf/${CORE_SITE}"
   cp conf "/${HADOOP_VERSION}/conf/${HDFS_SITE}"
   cp conf "/${HADOOP_VERSION}/conf/${YARN_SITE}"
   cp conf "/${HADOOP_VERSION}/conf/${MAPRED_SITE}"
   rm conf
   echo "" >> ${SPARTA_VARIABLES}
   echo "export HADOOP_HOME="/${HADOOP_VERSION}"" >> ${SPARTA_VARIABLES}
   echo "export HADOOP_USER_NAME="/${HDFS_USER_NAME}"" >> ${SPARTA_VARIABLES}
   echo "export HADOOP_CONF_DIR="/${HADOOP_VERSION}/conf"" >> ${SPARTA_VARIABLES}
   echo "export SPARK_HOME="${SPARK_HOME}"" >> ${SPARTA_VARIABLES}
   source "${SPARTA_VARIABLES}"
   sed -i "/# checkpointPath/d" ${SPARTA_CONF_FILE}
   sed -i "s|checkpointPath.*|checkpointPath = \"/user/stratio/sparta/checkpoint\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${EXECUTION_MODE}" == "mesos" ]]; then
   sed -i "s|sparkHome.*|sparkHome = \""${SPARK_HOME}"\"|" ${SPARTA_CONF_FILE}
   sed -i "s|master = \"mesos.*|master = \""mesos://${MESOS_MASTER}"\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${EXECUTION_MODE}" == "yarn" ]]; then
   sed -i "s|sparkHome.*|sparkHome = \""${SPARK_HOME}"\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${EXECUTION_MODE}" == "standalone" ]]; then
   sed -i "s|sparkHome.*|sparkHome = \""${SPARK_HOME}"\"|" ${SPARTA_CONF_FILE}
   sed -i "s|master = \"spark.*|master = \""spark://${SPARK_MASTER}"\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${SSH}" == "true" ]]; then
   /usr/sbin/sshd -e
 fi
 /etc/init.d/sparta start
 tail -F /var/log/sds/sparta/sparta.log
