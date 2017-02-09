#!/bin/bash -e

 NAME=sparta
 VARIABLES="/etc/default/$NAME-variables"
 SYSTEM_VARIABLES="/etc/profile"
 SPARTA_CONF_FILE=/etc/sds/sparta/reference.conf

 ## Vault and secrets (configured if enabled)
 ###################################################
 if [ !  -z ${VAULT_HOST} ]; then
     source security-config.sh $1
 fi

 # SPARTA JAVA OPTIONS
 if [[ ! -v SPARTA_HEAP_SIZE ]]; then
   SPARTA_HEAP_SIZE=-Xmx2048m
 fi
 sed -i "s|export SPARTA_HEAP_SIZE.*|export SPARTA_HEAP_SIZE=${SPARTA_HEAP_SIZE}|" ${VARIABLES}

 if [[ ! -v SPARTA_HEAP_MINIMUM_SIZE ]]; then
   SPARTA_HEAP_MINIMUM_SIZE=-Xms1024m
 fi
 sed -i "s|export SPARTA_HEAP_MINIMUM_SIZE.*|export SPARTA_HEAP_MINIMUM_SIZE=${SPARTA_HEAP_MINIMUM_SIZE}|" ${VARIABLES}

 if [[ ! -v SPARTA_MAX_PERM_SIZE ]]; then
   SPARTA_MAX_PERM_SIZE=-XX:MaxPermSize=512m
 fi
 sed -i "s|export SPARTA_MAX_PERM_SIZE.*|export SPARTA_MAX_PERM_SIZE=${SPARTA_MAX_PERM_SIZE}|" ${VARIABLES}


 # SPARTA GENERIC OPTIONS
 if [[ ! -v MAX_OPEN_FILES ]]; then
   MAX_OPEN_FILES=65535
 fi
 sed -i "s|export MAX_OPEN_FILES.*|export MAX_OPEN_FILES=${MAX_OPEN_FILES}|" ${VARIABLES}


 # SPARK DOWNLOAD OPTIONS
 if [[ ! -v SPARK_HOME ]]; then
   SPARK_HOME="/opt/sds/spark"
 fi
 echo "" >> ${VARIABLES}
 echo "export SPARK_HOME=${SPARK_HOME}" >> ${VARIABLES}
  echo "" >> ${SYSTEM_VARIABLES}
 echo "export SPARK_HOME=${SPARK_HOME}" >> ${SYSTEM_VARIABLES}


 # HDFS OPTIONS
 if [[ -v HDFS_USER_NAME ]]; then
   echo "" >> ${VARIABLES}
   echo "export HADOOP_USER_NAME=${HDFS_USER_NAME}" >> ${VARIABLES}
   echo "" >> ${SYSTEM_VARIABLES}
   echo "export HADOOP_USER_NAME=${HDFS_USER_NAME}" >> ${SYSTEM_VARIABLES}
   sed -i "s|.*sparta.hdfs.hadoopUserName.*|sparta.hdfs.hadoopUserName = \""${HDFS_USER_NAME}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v HDFS_MASTER ]&& [ HDFS_MASTER != "" ]; then
   sed -i "s|sparta.hdfs.hdfsMaster.*|sparta.hdfs.hdfsMaster = \""${HDFS_MASTER}"\"|" ${SPARTA_CONF_FILE}
   HADOOP_CONF_DIR=/opt/sds/hadoop/conf
   mkdir -p "${HADOOP_CONF_DIR}"
   CORE_SITE="${HADOOP_CONF_DIR}/core-site.xml"
   HDFS_SITE="${HADOOP_CONF_DIR}/hdfs-site.xml"
   wget "http://${HDFS_MASTER}:50070/conf"
   cp conf "${CORE_SITE}"
   cp conf "${HDFS_SITE}"
   rm -f conf
   sed -i "s|0.0.0.0|${HDFS_MASTER}|" ${CORE_SITE}
   sed -i "s|0.0.0.0|${HDFS_MASTER}|" ${HDFS_SITE}
   echo "" >> ${VARIABLES}
   echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${VARIABLES}
   echo "" >> ${SYSTEM_VARIABLES}
   echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
 fi

 if [[ ! -v HDFS_PORT ]]; then
   HDFS_PORT=9000
 fi
 sed -i "s|sparta.hdfs.hdfsPort.*|sparta.hdfs.hdfsPort = ${HDFS_PORT}|" ${SPARTA_CONF_FILE}

 if [ -v HDFS_PRINCIPAL_NAME ] && [ HDFS_PRINCIPAL_NAME != "" ]; then
   sed -i "s|.*sparta.hdfs.principalName.*|sparta.hdfs.principalName = \""${HDFS_PRINCIPAL_NAME}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v HDFS_PRINCIPAL_NAME_SUFFIX ] && [ HDFS_PRINCIPAL_NAME_SUFFIX != "" ]; then
   sed -i "s|.*sparta.hdfs.principalNameSuffix.*|sparta.hdfs.principalNameSuffix = \""${HDFS_PRINCIPAL_NAME_SUFFIX}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v HDFS_PRINCIPAL_NAME_PREFIX ] && [ HDFS_PRINCIPAL_NAME_PREFIX != "" ]; then
   sed -i "s|.*sparta.hdfs.principalNamePrefix.*|sparta.hdfs.principalNamePrefix = \""${HDFS_PRINCIPAL_NAME_PREFIX}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v HDFS_KEYTAB ] && [ HDFS_KEYTAB != "" ]; then
   sed -i "s|.*sparta.hdfs.keytabPath.*|sparta.hdfs.keytabPath = \""${HDFS_KEYTAB}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v HDFS_KEYTAB_RELOAD ] && [ HDFS_KEYTAB_RELOAD != "" ]; then
   sed -i "s|.*sparta.hdfs.reloadKeyTab.*|sparta.hdfs.reloadKeyTab = ${HDFS_KEYTAB_RELOAD}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v HDFS_KEYTAB_RELOAD_TIME ] && [ HDFS_KEYTAB_RELOAD_TIME != "" ]; then
   sed -i "s|.*sparta.hdfs.reloadKeyTabTime.*|sparta.hdfs.reloadKeyTabTime = ${HDFS_KEYTAB_RELOAD_TIME}|" ${SPARTA_CONF_FILE}
 fi


 # Load SPARTA VARIABLES file with JAVA, LOG, SPARK, HDFS and GENERIC options
 source "${VARIABLES}"
 source "${SYSTEM_VARIABLES}"


 # SPARTA LOG LEVEL OPTIONS
 if [[ ! -v SERVICE_LOG_LEVEL ]]; then
   SERVICE_LOG_LEVEL="ERROR"
 fi
 sed -i "s|<root level.*|<root level = \""${SERVICE_LOG_LEVEL}"\">|" ${LOG_CONFIG_FILE}

 if [[ ! -v SPARTA_LOG_LEVEL ]]; then
   SPARTA_LOG_LEVEL="INFO"
 fi
 sed -i "s|com.stratio.sparta.*|com.stratio.sparta\" level= \""${SPARTA_LOG_LEVEL}"\"/>|" ${LOG_CONFIG_FILE}

 if [[ ! -v SPARK_LOG_LEVEL ]]; then
   SPARK_LOG_LEVEL="ERROR"
 fi
 sed -i "s|org.apache.spark.*|org.apache.spark\" level= \""${SPARK_LOG_LEVEL}"\"/>|" ${LOG_CONFIG_FILE}

 if [[ ! -v HADOOP_LOG_LEVEL ]]; then
   HADOOP_LOG_LEVEL="ERROR"
 fi
 sed -i "s|org.apache.hadoop.*|org.apache.hadoop\" level= \""${HADOOP_LOG_LEVEL}"\"/>|" ${LOG_CONFIG_FILE}

 if [[ ! -v AVRO_NETTY_LOG_LEVEL ]]; then
   AVRO_NETTY_LOG_LEVEL="ERROR"
 fi
 sed -i "s|org.apache.avro.ipc.NettyTransceiver.*|org.apache.avro.ipc.NettyTransceiver\" level= \""${AVRO_NETTY_LOG_LEVEL}"\"/>|" ${LOG_CONFIG_FILE}

 if [[ ! -v MORTBAY_LOG_LEVEL ]]; then
   MORTBAY_LOG_LEVEL="ERROR"
 fi
 sed -i "s|org.mortbay.*|org.mortbay\" level= \""${MORTBAY_LOG_LEVEL}"\"/>|" ${LOG_CONFIG_FILE}

 if [[ ! -v JBOSS_LOG_LEVEL ]]; then
   JBOSS_LOG_LEVEL="ERROR"
 fi
 sed -i "s|org.jboss.*|org.jboss\" level= \""${JBOSS_LOG_LEVEL}"\"/>|" ${LOG_CONFIG_FILE}


 # SPARTA API OPTIONS
 if [[ ! -v SPARTA_API_HOST ]]; then
   SPARTA_API_HOST=0.0.0.0
 fi
 sed -i "s|sparta.api.host.*|sparta.api.host = \"${SPARTA_API_HOST}\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_API_PORT ]]; then
   SPARTA_API_PORT=9090
 fi
 sed -i "s|sparta.api.port.*|sparta.api.port = ${SPARTA_API_PORT}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_API_CERTIFICATE_FILE ]]; then
   SPARTA_API_CERTIFICATE_FILE="/home/user/certifications/stratio.jks"
 fi
 sed -i "s|sparta.api.certificate-file.*|sparta.api.certificate-file = \""${SPARTA_API_CERTIFICATE_FILE}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_API_CERTIFICATE_PASSWORD ]]; then
   SPARTA_API_CERTIFICATE_PASSWORD=stratio
 fi
 sed -i "s|sparta.api.certificate-password.*|sparta.api.certificate-password = \""${SPARTA_API_CERTIFICATE_PASSWORD}"\"|" ${SPARTA_CONF_FILE}
 # SPARTA ZOOKEEPER OPTIONS
 if [[ ! -v SPARTA_ZOOKEEPER_CONNECTION_STRING ]]; then
   SPARTA_ZOOKEEPER_CONNECTION_STRING="localhost:2181"
 fi
 sed -i "s|sparta.zookeeper.connectionString.*|sparta.zookeeper.connectionString = \""${SPARTA_ZOOKEEPER_CONNECTION_STRING}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT ]]; then
   SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT=15000
 fi
 sed -i "s|sparta.zookeeper.connectionTimeout.*|sparta.zookeeper.connectionTimeout = ${SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ZOOKEEPER_SESSION_TIMEOUT ]]; then
   SPARTA_ZOOKEEPER_SESSION_TIMEOUT=60000
 fi
 sed -i "s|sparta.zookeeper.sessionTimeout.*|sparta.zookeeper.sessionTimeout = ${SPARTA_ZOOKEEPER_SESSION_TIMEOUT}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ZOOKEEPER_RETRY_ATEMPTS ]]; then
   SPARTA_ZOOKEEPER_RETRY_ATEMPTS=5
 fi
 sed -i "s|sparta.zookeeper.retryAttempts.*|sparta.zookeeper.retryAttempts = ${SPARTA_ZOOKEEPER_RETRY_ATEMPTS}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ZOOKEEPER_RETRY_INTERVAL ]]; then
   SPARTA_ZOOKEEPER_RETRY_INTERVAL=10000
 fi
 sed -i "s|sparta.zookeeper.retryInterval.*|sparta.zookeeper.retryInterval = ${SPARTA_ZOOKEEPER_RETRY_INTERVAL}|" ${SPARTA_CONF_FILE}


 # SPARTA AKKA OPTIONS
   if [[ ! -v SPARTA_AKKA_CONTROLLER_INSTANCES ]]; then
   SPARTA_AKKA_CONTROLLER_INSTANCES=5
 fi
 sed -i "s|sparta.akka.controllerActorInstances.*|sparta.akka.controllerActorInstances = ${SPARTA_AKKA_CONTROLLER_INSTANCES}|" ${SPARTA_CONF_FILE}


 # SPARTA CONFIGURATION OPTIONS
 if [[ ! -v SPARTA_EXECUTION_MODE ]]; then
   SPARTA_EXECUTION_MODE=local
 fi
 sed -i "s|sparta.config.executionMode.*|sparta.config.executionMode = ${SPARTA_EXECUTION_MODE}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_DRIVER_PACKAGE_LOCATION ]]; then
   SPARTA_DRIVER_PACKAGE_LOCATION="/opt/sds/sparta/driver/"
 fi
 sed -i "s|sparta.config.driverPackageLocation.*|sparta.config.driverPackageLocation = \""${SPARTA_DRIVER_PACKAGE_LOCATION}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_DRIVER_URI ]]; then
   SPARTA_DRIVER_URI="http://sparta:9090/drivers/driver-plugin.jar"
 fi
 sed -i "s|sparta.config.driverURI.*|sparta.config.driverURI = \""${SPARTA_DRIVER_URI}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_PLUGIN_PACKAGE_LOCATION ]]; then
   SPARTA_PLUGIN_PACKAGE_LOCATION="/opt/sds/plugins/"
 fi
 sed -i "s|sparta.config.pluginPackageLocation.*|sparta.config.pluginPackageLocation = \""${SPARTA_PLUGIN_PACKAGE_LOCATION}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_STOP_GRACEFULLY ]]; then
   SPARTA_STOP_GRACEFULLY=true
 fi
 sed -i "s|sparta.config.stopGracefully.*|sparta.config.stopGracefully = ${SPARTA_STOP_GRACEFULLY}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_AWAIT_POLICY_CHANGE_STATUS ]]; then
   SPARTA_AWAIT_POLICY_CHANGE_STATUS=120s
 fi
 sed -i "s|sparta.config.awaitPolicyChangeStatus.*|sparta.config.awaitPolicyChangeStatus = ${SPARTA_AWAIT_POLICY_CHANGE_STATUS}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_REMEMBER_PARTITIONER ]]; then
   SPARTA_REMEMBER_PARTITIONER=true
 fi
 sed -i "s|sparta.config.rememberPartitioner.*|sparta.config.rememberPartitioner = ${SPARTA_REMEMBER_PARTITIONER}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_CHECKPOINT_PATH ]]; then
   SPARTA_CHECKPOINT_PATH="/tmp/stratio/sparta/checkpoint"
 fi
 sed -i "s|sparta.config.checkpointPath.*|sparta.config.checkpointPath = \""${SPARTA_CHECKPOINT_PATH}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_AUTO_DELETE_CHECKPOINT ]]; then
   SPARTA_AUTO_DELETE_CHECKPOINT=false
 fi
 sed -i "s|sparta.config.autoDeleteCheckpoint.*|sparta.config.autoDeleteCheckpoint = ${SPARTA_AUTO_DELETE_CHECKPOINT}|" ${SPARTA_CONF_FILE}


 # LOCAL EXECUTION OPTIONS
 if [[ ! -v SPARK_LOCAL_MASTER ]]; then
   SPARK_LOCAL_MASTER="local[*]"
 fi
 sed -i "s|sparta.local.spark.master.*|sparta.local.spark.master = \""${SPARK_LOCAL_MASTER}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_LOCAL_APP_NAME ]]; then
   SPARK_LOCAL_APP_NAME=SPARTA
 fi
 sed -i "s|sparta.local.spark.app.name.*|sparta.local.spark.app.name = ${SPARK_LOCAL_APP_NAME}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_LOCAL_DRIVER_MEMORY ]]; then
   SPARK_LOCAL_DRIVER_MEMORY=1G
 fi
 sed -i "s|sparta.local.spark.driver.memory.*|sparta.local.spark.driver.memory = ${SPARK_LOCAL_DRIVER_MEMORY}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_LOCAL_DRIVER_CORES ]]; then
   SPARK_LOCAL_DRIVER_CORES=1
 fi
 sed -i "s|sparta.local.spark.driver.cores.*|sparta.local.spark.driver.cores = ${SPARK_LOCAL_DRIVER_CORES}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_LOCAL_EXECUTOR_MEMORY ]]; then
   SPARK_LOCAL_EXECUTOR_MEMORY=1G
 fi
 sed -i "s|sparta.local.spark.executor.memory.*|sparta.local.spark.executor.memory = ${SPARK_LOCAL_EXECUTOR_MEMORY}|" ${SPARTA_CONF_FILE}

 if [ -v SPARK_LOCAL_CONCURRENT_JOBS ] && [ SPARK_LOCAL_CONCURRENT_JOBS != "" ]; then
   sed -i "s|.*sparta.local.spark.streaming.concurrentJobs.*|sparta.local.spark.streaming.concurrentJobs = ${SPARK_LOCAL_CONCURRENT_JOBS}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_LOCAL_GRACEFUL_STOP ] && [ SPARK_LOCAL_GRACEFUL_STOP != "" ]; then
   sed -i "s|.*sparta.local.spark.streaming.gracefulStopTimeout.*|sparta.local.spark.streaming.gracefulStopTimeout = ${SPARK_LOCAL_GRACEFUL_STOP}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_LOCAL_METRICS_CONF ] && [ SPARK_LOCAL_METRICS_CONF != "" ]; then
   touch ${SPARK_LOCAL_METRICS_CONF}
   sed -i "s|.*sparta.local.spark.metrics.conf.*|sparta.local.spark.metrics.conf = \""${SPARK_LOCAL_METRICS_CONF}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_LOCAL_SERIALIZER ] && [ SPARK_LOCAL_SERIALIZER != "" ]; then
   sed -i "s|.*sparta.local.spark.serializer.*|sparta.local.spark.serializer = ${SPARK_LOCAL_SERIALIZER}|" ${SPARTA_CONF_FILE}
 fi

 if [[ ! -v SPARK_LOCAL_PARQUET_BINARY_AS_STRING ]]; then
   SPARK_LOCAL_PARQUET_BINARY_AS_STRING=true
 fi
 sed -i "s|sparta.local.spark.sql.parquet.binaryAsString.*|sparta.local.spark.sql.parquet.binaryAsString = ${SPARK_LOCAL_PARQUET_BINARY_AS_STRING}|" ${SPARTA_CONF_FILE}


 # MESOS EXECUTION OPTIONS
 if [[ ! -v SPARK_MESOS_HOME ]]; then
   SPARK_MESOS_HOME=${SPARK_HOME}
 fi
 sed -i "s|sparta.mesos.sparkHome.*|sparta.mesos.sparkHome = \""${SPARK_MESOS_HOME}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_COARSE ]]; then
   SPARK_MESOS_COARSE=true
 fi
 sed -i "s|sparta.mesos.spark.mesos.coarse.*|sparta.mesos.spark.mesos.coarse = ${SPARK_MESOS_COARSE}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_DEPLOY ]]; then
   SPARK_MESOS_DEPLOY=cluster
 fi
 sed -i "s|sparta.mesos.deployMode.*|sparta.mesos.deployMode = ${SPARK_MESOS_DEPLOY}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_MASTER ]]; then
   SPARK_MESOS_MASTER="mesos://mesosDispatcherURL"
 fi
 sed -i "s|sparta.mesos.master.*|sparta.mesos.master = \""${SPARK_MESOS_MASTER}"\"|" ${SPARTA_CONF_FILE}

  if [[ ! -v SPARK_MESOS_KILL_URL ]]; then
   SPARK_MESOS_KILL_URL="http://mesosDispatcherURL/v1/submissions/kill"
 fi
 sed -i "s|sparta.mesos.killUrl.*|sparta.mesos.killUrl = \""${SPARK_MESOS_KILL_URL}"\"|" ${SPARTA_CONF_FILE}

 if [ -v SPARK_MESOS_JARS ] && [ SPARK_MESOS_JARS != "" ]; then
   sed -i "s|.*sparta.mesos.jars.*|sparta.mesos.jars = \""${SPARK_MESOS_JARS}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_PACKAGES ] && [ SPARK_MESOS_PACKAGES != "" ]; then
   sed -i "s|.*sparta.mesos.packages.*|sparta.mesos.packages = \""${SPARK_MESOS_PACKAGES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXCLUDE_PACKAGES ] && [ SPARK_MESOS_EXCLUDE_PACKAGES != "" ]; then
   sed -i "s|.*sparta.mesos.exclude-packages.*|sparta.mesos.exclude-packages = \""${SPARK_MESOS_EXCLUDE_PACKAGES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_REPOSITORIES ] && [ SPARK_MESOS_REPOSITORIES != "" ]; then
   sed -i "s|.*sparta.mesos.repositories.*|sparta.mesos.repositories = \""${SPARK_MESOS_REPOSITORIES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_PROXY_USER ] && [ SPARK_MESOS_PROXY_USER != "" ]; then
   sed -i "s|.*sparta.mesos.proxy-user.*|sparta.mesos.proxy-user = \""${SPARK_MESOS_PROXY_USER}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_DRIVER_JAVA_OPTIONS ] && [ SPARK_MESOS_DRIVER_JAVA_OPTIONS != "" ]; then
   sed -i "s|.*sparta.mesos.driver-java-options.*|sparta.mesos.driver-java-options = \""${SPARK_MESOS_DRIVER_JAVA_OPTIONS}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_DRIVER_LIBRARY_PATH ] && [ SPARK_MESOS_DRIVER_LIBRARY_PATH != "" ]; then
   sed -i "s|.*sparta.mesos.driver-library-path.*|sparta.mesos.driver-library-path = \""${SPARK_MESOS_DRIVER_LIBRARY_PATH}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_DRIVER_CLASSPATH ] && [ SPARK_MESOS_DRIVER_CLASSPATH != "" ]; then
   sed -i "s|.*sparta.mesos.driver-class-path.*|sparta.mesos.driver-class-path = \""${SPARK_MESOS_DRIVER_CLASSPATH}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [[ ! -v SPARK_MESOS_EXECUTOR_MEMORY ]]; then
   SPARK_MESOS_EXECUTOR_MEMORY=1G
 fi
 sed -i "s|sparta.mesos.spark.executor.memory.*|sparta.mesos.spark.executor.memory = ${SPARK_MESOS_EXECUTOR_MEMORY}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_TOTAL_EXECUTOR_CORES ]]; then
   SPARK_MESOS_TOTAL_EXECUTOR_CORES=2
 fi
 sed -i "s|sparta.mesos.totalExecutorCores.*|sparta.mesos.totalExecutorCores = ${SPARK_MESOS_TOTAL_EXECUTOR_CORES}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_EXECUTOR_CORES ]]; then
   SPARK_MESOS_EXECUTOR_CORES=1
 fi
 sed -i "s|.*sparta.mesos.spark.executor.cores.*|sparta.mesos.spark.executor.cores = ${SPARK_MESOS_EXECUTOR_CORES}|" ${SPARTA_CONF_FILE}


 if [ -v SPARK_MESOS_EXTRA_CORES ] && [ SPARK_MESOS_EXTRA_CORES != "" ]; then
   sed -i "s|.*sparta.mesos.spark.mesos.extra.cores.*|sparta.mesos.spark.mesos.extra.cores = ${SPARK_MESOS_EXTRA_CORES}|" ${SPARTA_CONF_FILE}
 fi

 if [[ ! -v SPARK_MESOS_DRIVER_CORES ]]; then
   SPARK_MESOS_DRIVER_CORES=1
 fi
 sed -i "s|sparta.mesos.spark.driver.cores.*|sparta.mesos.spark.driver.cores = ${SPARK_MESOS_DRIVER_CORES}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_DRIVER_MEMORY ]]; then
   SPARK_MESOS_DRIVER_MEMORY=1G
 fi
 sed -i "s|sparta.mesos.spark.driver.memory.*|sparta.mesos.spark.driver.memory = ${SPARK_MESOS_DRIVER_MEMORY}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_SUPERVISE ]]; then
   SPARK_MESOS_SUPERVISE=false
 fi
 sed -i "s|sparta.mesos.supervise.*|sparta.mesos.supervise = ${SPARK_MESOS_SUPERVISE}|" ${SPARTA_CONF_FILE}

 if [ -v SPARK_MESOS_CONCURRENT_JOBS ] && [ SPARK_MESOS_CONCURRENT_JOBS != "" ]; then
   sed -i "s|.*sparta.mesos.spark.streaming.concurrentJobs.*|sparta.mesos.spark.streaming.concurrentJobs = ${SPARK_MESOS_CONCURRENT_JOBS}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_GRACEFUL_STOP ] && [ SPARK_MESOS_GRACEFUL_STOP != "" ]; then
   sed -i "s|.*sparta.mesos.spark.streaming.gracefulStopTimeout.*|sparta.mesos.spark.streaming.gracefulStopTimeout = ${SPARK_MESOS_GRACEFUL_STOP}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SERIALIZER ] && [ SPARK_MESOS_SERIALIZER != "" ]; then
   sed -i "s|.*sparta.mesos.spark.serializer.*|sparta.mesos.spark.serializer = ${SPARK_MESOS_SERIALIZER}|" ${SPARTA_CONF_FILE}
 fi

 if [[ ! -v SPARK_MESOS_PARQUET_BINARY_AS_STRING ]]; then
   SPARK_MESOS_PARQUET_BINARY_AS_STRING=true
 fi
 sed -i "s|sparta.mesos.spark.sql.parquet.binaryAsString.*|sparta.mesos.spark.sql.parquet.binaryAsString = ${SPARK_MESOS_PARQUET_BINARY_AS_STRING}|" ${SPARTA_CONF_FILE}

 if [ -v SPARK_MESOS_PROPERTIES_FILE ] && [ SPARK_MESOS_PROPERTIES_FILE != "" ]; then
   sed -i "s|.*sparta.mesos.propertiesFile.*|sparta.mesos.propertiesFile = \""${SPARK_MESOS_PROPERTIES_FILE}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_DOCKER_IMAGE ] && [ SPARK_MESOS_EXECUTOR_DOCKER_IMAGE != "" ]; then
   sed -i "s|.*sparta.mesos.spark.mesos.executor.docker.image.*|sparta.mesos.spark.mesos.executor.docker.image = \""${SPARK_MESOS_EXECUTOR_DOCKER_IMAGE}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_FORCE_PULL_IMAGE ] && [ SPARK_MESOS_EXECUTOR_FORCE_PULL_IMAGE != "" ]; then
   sed -i "s|.*spark.mesos.executor.docker.forcePullImage.*|spark.mesos.executor.docker.forcePullImage = ${SPARK_MESOS_EXECUTOR_FORCE_PULL_IMAGE}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_HOME ] && [ SPARK_MESOS_EXECUTOR_HOME != "" ]; then
   sed -i "s|.*sparta.mesos.spark.executor.home.*|sparta.mesos.spark.executor.home = \""${SPARK_MESOS_EXECUTOR_HOME}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_URI ] && [ SPARK_MESOS_EXECUTOR_URI != "" ]; then
   sed -i "s|.*sparta.mesos.spark.executor.uri.*|sparta.mesos.spark.executor.uri = \""${SPARK_MESOS_EXECUTOR_URI}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SPARK_FILES ] && [ SPARK_MESOS_SPARK_FILES != "" ]; then
   sed -i "s|.*sparta.mesos.spark.files.*|sparta.mesos.spark.files = \""${SPARK_MESOS_SPARK_FILES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SPARK_JARS_IVY ] && [ SPARK_MESOS_SPARK_JARS_IVY != "" ]; then
   sed -i "s|.*sparta.mesos.spark.jars.ivy.*|sparta.mesos.spark.jars.ivy = \""${SPARK_MESOS_SPARK_JARS_IVY}"\"|" ${SPARTA_CONF_FILE}
 fi


 # YARN EXECUTION OPTIONS
 # TODO add options available in SPARTA configuration file


 # STANDALONE EXECUTION OPTIONS
 # TODO add options available in SPARTA configuration file


 # OAUTH2 OPTIONS
 if [[ ! -v OAUTH2_ENABLE ]]; then
   OAUTH2_ENABLE=false
 fi
 sed -i "s|oauth2.enable.*|oauth2.enable = \""${OAUTH2_ENABLE}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_COOKIE_NAME ]]; then
   OAUTH2_COOKIE_NAME=user
 fi
 sed -i "s|oauth2.cookieName.*|oauth2.cookieName = \""${OAUTH2_COOKIE_NAME}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_SSL_AUTHORIZE ]]; then
   OAUTH2_URL_AUTHORIZE="https://server.domain:9005/cas/oauth2.0/authorize"
 fi
 sed -i "s|oauth2.url.authorize.*|oauth2.url.authorize = \""${OAUTH2_SSL_AUTHORIZE}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_URL_ACCESS_TOKEN ]]; then
   OAUTH2_URL_ACCESS_TOKEN="https://server.domain:9005/cas/oauth2.0/accessToken"
 fi
 sed -i "s|oauth2.url.accessToken.*|oauth2.url.accessToken = \""${OAUTH2_URL_ACCESS_TOKEN}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_URL_PROFILE ]]; then
   OAUTH2_URL_PROFILE="https://server.domain:9005/cas/oauth2.0/profile"
 fi
 sed -i "s|oauth2.url.profile.*|oauth2.url.profile = \""${OAUTH2_URL_PROFILE}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_URL_LOGOUT ]]; then
   OAUTH2_URL_LOGOUT="https://server.domain:9005/cas/logout"
 fi
 sed -i "s|oauth2.url.logout.*|oauth2.url.logout = \""${OAUTH2_URL_LOGOUT}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_URL_CALLBACK ]]; then
   OAUTH2_URL_CALLBACK="http://callback.domain:9090/login"
 fi
 sed -i "s|oauth2.url.callBack.*|oauth2.url.callBack = \""${OAUTH2_URL_CALLBACK}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_URL_ON_LOGIN_GO_TO ]]; then
   OAUTH2_URL_ON_LOGIN_GO_TO="/"
 fi
 sed -i "s|oauth2.url.onLoginGoTo.*|oauth2.url.onLoginGoTo = \""${OAUTH2_URL_ON_LOGIN_GO_TO}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_CLIENT_ID ]]; then
   OAUTH2_CLIENT_ID="userid"
 fi
 sed -i "s|oauth2.client.id.*|oauth2.client.id = \""${OAUTH2_CLIENT_ID}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v OAUTH2_CLIENT_SECRET ]]; then
   OAUTH2_CLIENT_SECRET="usersecret"
 fi
 sed -i "s|oauth2.client.secret.*|oauth2.client.secret = \""${OAUTH2_CLIENT_SECRET}"\"|" ${SPARTA_CONF_FILE}


 # AKKA OPTIONS
  if [[ ! -v AKKA_LOG_DEAD_LETTERS ]]; then
   AKKA_LOG_DEAD_LETTERS=off
 fi
 sed -i "s|akka.log-dead-letters.*|akka.log-dead-letters = ${AKKA_LOG_DEAD_LETTERS}|" ${SPARTA_CONF_FILE}


 # SPRAY OPTIONS
  if [[ ! -v SPRAY_CAN_SERVER_SSL_ENCRYPTION ]]; then
   SPRAY_CAN_SERVER_SSL_ENCRYPTION=off
 fi
 sed -i "s|spray.can.server.ssl-encryption.*|spray.can.server.ssl-encryption = ${SPRAY_CAN_SERVER_SSL_ENCRYPTION}|" ${SPARTA_CONF_FILE}


 if [[ "${SSH}" == "true" ]]; then
   /usr/sbin/sshd -e
 fi

 if [[ ! -v RUN_MODE ]]; then
   RUN_MODE="production"
 fi
 case "$RUN_MODE" in
   "debug") # In this mode, Sparta will be launched as a service within the docker container.
     SERVICE_LOG_APPENDER="FILE"
     sed -i "s|<appender-ref ref.*|<appender-ref ref= \""${SERVICE_LOG_APPENDER}"\" />|" ${LOG_CONFIG_FILE}
     service sparta start
     tail -F /var/log/sds/sparta/sparta.log
     ;;
   *) # Default mode: Sparta run as a docker application
     SERVICE_LOG_APPENDER="STDOUT"
     export SPARTA_OPTS="$SPARTA_OPTS -Dconfig.file=$SPARTA_CONF_FILE"
     sed -i "s|<appender-ref ref.*|<appender-ref ref= \""${SERVICE_LOG_APPENDER}"\" />|" ${LOG_CONFIG_FILE}
     /opt/sds/sparta/bin/run
     ;;
 esac
