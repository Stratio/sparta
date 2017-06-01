#!/bin/bash

function initJavaOptions() {

 if [[ ! -v SPARTA_HEAP_SIZE ]]; then
   SPARTA_HEAP_SIZE=-Xmx2048m
 fi
 sed -i "s|export SPARTA_HEAP_SIZE.*|export SPARTA_HEAP_SIZE=${SPARTA_HEAP_SIZE}|" ${VARIABLES}

 if [[ ! -v SPARTA_HEAP_MINIMUM_SIZE ]]; then
   SPARTA_HEAP_MINIMUM_SIZE=-Xms1024m
 fi
 sed -i "s|export SPARTA_HEAP_MINIMUM_SIZE.*|export SPARTA_HEAP_MINIMUM_SIZE=${SPARTA_HEAP_MINIMUM_SIZE}|" ${VARIABLES}

 if [[ ! -v MAX_OPEN_FILES ]]; then
   MAX_OPEN_FILES=65535
 fi
 sed -i "s|export MAX_OPEN_FILES.*|export MAX_OPEN_FILES=${MAX_OPEN_FILES}|" ${VARIABLES}

 if [ -v SPARTA_JAAS_FILE ] && [ ${#SPARTA_JAAS_FILE} != 0 ]; then
   sed -i "s|.*export SPARTA_CONFIG_JAAS_FILE.*|export SPARTA_CONFIG_JAAS_FILE=\"-Djava.security.auth.login.config=${SPARTA_JAAS_FILE}\"|" ${VARIABLES}
 fi

}

function hdfsOptions() {

 if [[ ! -v HADOOP_PORT ]]; then
   HADOOP_PORT=9000
 fi
 sed -i "s|.*sparta.hdfs.hdfsPort.*|sparta.hdfs.hdfsPort = ${HADOOP_PORT}|" ${SPARTA_CONF_FILE}

 if [[ ! -v HDFS_SECURITY_ENABLED ]]; then
   HDFS_SECURITY_ENABLED=false
 fi
 if [ $HDFS_SECURITY_ENABLED == "true" ] && [ -v SPARTA_PRINCIPAL_NAME ] && [ ${#SPARTA_PRINCIPAL_NAME} != 0 ]; then
   sed -i "s|.*sparta.hdfs.principalName .*|sparta.hdfs.principalName = \""${SPARTA_PRINCIPAL_NAME}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ $HDFS_SECURITY_ENABLED == "true" ] && [ -v SPARTA_KEYTAB_PATH ] && [ ${#SPARTA_KEYTAB_PATH} != 0 ]; then
   sed -i "s|.*sparta.hdfs.keytabPath.*|sparta.hdfs.keytabPath = \""${SPARTA_KEYTAB_PATH}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ $HDFS_SECURITY_ENABLED == "true" ] && [ -v HDFS_KEYTAB_RELOAD ] && [ ${#HDFS_KEYTAB_RELOAD} != 0 ]; then
   sed -i "s|.*sparta.hdfs.reloadKeyTab.*|sparta.hdfs.reloadKeyTab = ${HDFS_KEYTAB_RELOAD}|" ${SPARTA_CONF_FILE}
 fi

 if [ $HDFS_SECURITY_ENABLED == "true" ] && [ -v HDFS_KEYTAB_RELOAD_TIME ] && [ ${#HDFS_KEYTAB_RELOAD_TIME} != 0 ]; then
   sed -i "s|.*sparta.hdfs.reloadKeyTabTime.*|sparta.hdfs.reloadKeyTabTime = ${HDFS_KEYTAB_RELOAD_TIME}|" ${SPARTA_CONF_FILE}
 fi
}

function apiOptions() {

 if [[ ! -v SPARTA_API_HOST ]]; then
   SPARTA_API_HOST=0.0.0.0
 fi
 sed -i "s|.*sparta.api.host.*|sparta.api.host = \"${SPARTA_API_HOST}\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v PORT_SPARTAAPI ]]; then
   PORT_SPARTAAPI=9090
 fi
 sed -i "s|.*sparta.api.port.*|sparta.api.port = ${PORT_SPARTAAPI}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_API_CERTIFICATE_FILE ]]; then
   SPARTA_API_CERTIFICATE_FILE="/home/user/certifications/stratio.jks"
 fi
 sed -i "s|.*sparta.api.certificate-file.*|sparta.api.certificate-file = \""${SPARTA_API_CERTIFICATE_FILE}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_API_CERTIFICATE_PASSWORD ]]; then
   SPARTA_API_CERTIFICATE_PASSWORD=stratio
 fi
 sed -i "s|.*sparta.api.certificate-password.*|sparta.api.certificate-password = \""${SPARTA_API_CERTIFICATE_PASSWORD}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPRAY_CAN_SERVER_SSL_ENCRYPTION ]]; then
   SPRAY_CAN_SERVER_SSL_ENCRYPTION_SWITCH=off
   elif [[ $SPRAY_CAN_SERVER_SSL_ENCRYPTION == "true" ]]; then
    SPRAY_CAN_SERVER_SSL_ENCRYPTION_SWITCH=on
   else
    SPRAY_CAN_SERVER_SSL_ENCRYPTION_SWITCH=off
 fi

 sed -i "s|.*spray.can.server.ssl-encryption.*|spray.can.server.ssl-encryption = ${SPRAY_CAN_SERVER_SSL_ENCRYPTION_SWITCH}|" ${SPARTA_CONF_FILE}
}

function oauthOptions() {

 if [[ ! -v OAUTH2_ENABLE ]]; then
   OAUTH2_ENABLE=false
 fi
 sed -i "s|.*oauth2.enable.*|oauth2.enable = \""${OAUTH2_ENABLE}"\"|" ${SPARTA_CONF_FILE}

 if [ -v OAUTH2_COOKIE_NAME ] && [ ${#OAUTH2_COOKIE_NAME} != 0 ]; then
   sed -i "s|.*oauth2.cookieName.*|oauth2.cookieName = \""${OAUTH2_COOKIE_NAME}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v OAUTH2_SSL_AUTHORIZE ] && [ ${#OAUTH2_SSL_AUTHORIZE} != 0 ]; then
   sed -i "s|.*oauth2.url.authorize.*|oauth2.url.authorize = \""${OAUTH2_SSL_AUTHORIZE}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v OAUTH2_URL_ACCESS_TOKEN ] && [ ${#OAUTH2_URL_ACCESS_TOKEN} != 0 ]; then
   sed -i "s|.*oauth2.url.accessToken.*|oauth2.url.accessToken = \""${OAUTH2_URL_ACCESS_TOKEN}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v OAUTH2_URL_PROFILE ] && [ ${#OAUTH2_URL_PROFILE} != 0 ]; then
   sed -i "s|.*oauth2.url.profile.*|oauth2.url.profile = \""${OAUTH2_URL_PROFILE}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v OAUTH2_URL_LOGOUT ] && [ ${#OAUTH2_URL_PROFILE} != 0 ]; then
   sed -i "s|.*oauth2.url.logout.*|oauth2.url.logout = \""${OAUTH2_URL_LOGOUT}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v OAUTH2_URL_CALLBACK ] && [ ${#OAUTH2_URL_CALLBACK} != 0 ]; then
   sed -i "s|.*oauth2.url.callBack.*|oauth2.url.callBack = \""${OAUTH2_URL_CALLBACK}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v OAUTH2_URL_ON_LOGIN_GO_TO ] && [ ${#OAUTH2_URL_ON_LOGIN_GO_TO} != 0 ]; then
   sed -i "s|.*oauth2.url.onLoginGoTo.*|oauth2.url.onLoginGoTo = \""${OAUTH2_URL_ON_LOGIN_GO_TO}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v OAUTH2_CLIENT_ID ] && [ ${#OAUTH2_CLIENT_ID} != 0 ]; then
   sed -i "s|.*oauth2.client.id.*|oauth2.client.id = \""${OAUTH2_CLIENT_ID}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v OAUTH2_CLIENT_SECRET ] && [ ${#OAUTH2_CLIENT_SECRET} != 0 ]; then
   sed -i "s|.*oauth2.client.secret.*|oauth2.client.secret = \""${OAUTH2_CLIENT_SECRET}"\"|" ${SPARTA_CONF_FILE}
 fi
}

function zookeeperOptions() {

 if [[ ! -v SPARTA_ZOOKEEPER_CONNECTION_STRING ]]; then
   SPARTA_ZOOKEEPER_CONNECTION_STRING="localhost:2181"
 fi
 sed -i "s|.*sparta.zookeeper.connectionString.*|sparta.zookeeper.connectionString = \""${SPARTA_ZOOKEEPER_CONNECTION_STRING}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT ]]; then
   SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT=15000
 fi
 sed -i "s|.*sparta.zookeeper.connectionTimeout.*|sparta.zookeeper.connectionTimeout = ${SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ZOOKEEPER_SESSION_TIMEOUT ]]; then
   SPARTA_ZOOKEEPER_SESSION_TIMEOUT=60000
 fi
 sed -i "s|.*sparta.zookeeper.sessionTimeout.*|sparta.zookeeper.sessionTimeout = ${SPARTA_ZOOKEEPER_SESSION_TIMEOUT}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ZOOKEEPER_RETRY_ATEMPTS ]]; then
   SPARTA_ZOOKEEPER_RETRY_ATEMPTS=5
 fi
 sed -i "s|.*sparta.zookeeper.retryAttempts.*|sparta.zookeeper.retryAttempts = ${SPARTA_ZOOKEEPER_RETRY_ATEMPTS}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ZOOKEEPER_RETRY_INTERVAL ]]; then
   SPARTA_ZOOKEEPER_RETRY_INTERVAL=10000
 fi
 sed -i "s|.*sparta.zookeeper.retryInterval.*|sparta.zookeeper.retryInterval = ${SPARTA_ZOOKEEPER_RETRY_INTERVAL}|" ${SPARTA_CONF_FILE}
}

function configOptions() {

 if [[ ! -v SPARTA_EXECUTION_MODE ]]; then
   SPARTA_EXECUTION_MODE=local
 fi
 sed -i "s|.*sparta.config.executionMode.*|sparta.config.executionMode = ${SPARTA_EXECUTION_MODE}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_DRIVER_PACKAGE_LOCATION ]]; then
   SPARTA_DRIVER_PACKAGE_LOCATION="/opt/sds/sparta/driver/"
 fi
 sed -i "s|.*sparta.config.driverPackageLocation.*|sparta.config.driverPackageLocation = \""${SPARTA_DRIVER_PACKAGE_LOCATION}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_DRIVER_URI ]]; then
   SPARTA_DRIVER_URI="http://sparta:9090/driver/sparta-driver.jar"
 fi
 sed -i "s|.*sparta.config.driverURI.*|sparta.config.driverURI = \""${SPARTA_DRIVER_URI}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_PLUGIN_PACKAGE_LOCATION ]]; then
   SPARTA_PLUGIN_PACKAGE_LOCATION="/opt/sds/sparta/plugins/"
 fi
 sed -i "s|.*sparta.config.pluginPackageLocation.*|sparta.config.pluginPackageLocation = \""${SPARTA_PLUGIN_PACKAGE_LOCATION}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_AWAIT_POLICY_CHANGE_STATUS ]]; then
   SPARTA_AWAIT_POLICY_CHANGE_STATUS=180s
 fi
 sed -i "s|.*sparta.config.awaitPolicyChangeStatus.*|sparta.config.awaitPolicyChangeStatus = ${SPARTA_AWAIT_POLICY_CHANGE_STATUS}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_REMEMBER_PARTITIONER ]]; then
   SPARTA_REMEMBER_PARTITIONER=true
 fi
 sed -i "s|.*sparta.config.rememberPartitioner.*|sparta.config.rememberPartitioner = ${SPARTA_REMEMBER_PARTITIONER}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_CHECKPOINT_PATH ]]; then
   SPARTA_CHECKPOINT_PATH="/tmp/stratio/sparta/checkpoint"
 fi
 sed -i "s|.*sparta.config.checkpointPath.*|sparta.config.checkpointPath = \""${SPARTA_CHECKPOINT_PATH}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_AUTO_DELETE_CHECKPOINT ]]; then
   SPARTA_AUTO_DELETE_CHECKPOINT=true
 fi
 sed -i "s|.*sparta.config.autoDeleteCheckpoint.*|sparta.config.autoDeleteCheckpoint = ${SPARTA_AUTO_DELETE_CHECKPOINT}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_ADD_TIME_TO_CHECKPOINT ]]; then
   SPARTA_ADD_TIME_TO_CHECKPOINT=false
 fi
 sed -i "s|.*sparta.config.addTimeToCheckpointPath.*|sparta.config.addTimeToCheckpointPath = ${SPARTA_ADD_TIME_TO_CHECKPOINT}|" ${SPARTA_CONF_FILE}

 if [[ -v FRONTEND_TIMEOUT ]] && [ ${#FRONTEND_TIMEOUT} != 0 ]; then
 sed -i "s|.*sparta.config.frontend.timeout.*|sparta.config.frontend.timeout = ${FRONTEND_TIMEOUT}|" ${SPARTA_CONF_FILE}
 fi

}

function localSparkOptions() {

 if [[ ! -v SPARK_LOCAL_MASTER ]]; then
   SPARK_LOCAL_MASTER="local[*]"
 fi
 sed -i "s|.*sparta.local.spark.master.*|sparta.local.spark.master = \""${SPARK_LOCAL_MASTER}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_LOCAL_APP_NAME ]]; then
   SPARK_LOCAL_APP_NAME=Sparta
 fi
 sed -i "s|.*sparta.local.spark.app.name.*|sparta.local.spark.app.name = ${SPARK_LOCAL_APP_NAME}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_LOCAL_DRIVER_MEMORY ]]; then
   SPARK_LOCAL_DRIVER_MEMORY=1G
 fi
 sed -i "s|.*sparta.local.spark.driver.memory.*|sparta.local.spark.driver.memory = ${SPARK_LOCAL_DRIVER_MEMORY}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_LOCAL_EXECUTOR_MEMORY ]]; then
   SPARK_LOCAL_EXECUTOR_MEMORY=1G
 fi
 sed -i "s|.*sparta.local.spark.executor.memory.*|sparta.local.spark.executor.memory = ${SPARK_LOCAL_EXECUTOR_MEMORY}|" ${SPARTA_CONF_FILE}

 if [ -v SPARK_LOCAL_METRICS_CONF ] && [ ${#SPARK_LOCAL_METRICS_CONF} != 0 ]; then
   touch ${SPARK_LOCAL_METRICS_CONF}
   sed -i "s|.*sparta.local.spark.metrics.conf.*|sparta.local.spark.metrics.conf = \""${SPARK_LOCAL_METRICS_CONF}"\"|" ${SPARTA_CONF_FILE}
 fi
}

function mesosSparkOptions() {

 if [[ ! -v SPARK_MESOS_HOME ]]; then
   SPARK_MESOS_HOME=${SPARK_HOME}
 fi
 sed -i "s|.*sparta.mesos.sparkHome.*|sparta.mesos.sparkHome = \""${SPARK_MESOS_HOME}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_COARSE ]]; then
   SPARK_MESOS_COARSE=true
 fi
 sed -i "s|.*sparta.mesos.spark.mesos.coarse.*|sparta.mesos.spark.mesos.coarse = ${SPARK_MESOS_COARSE}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_DEPLOY ]]; then
   SPARK_MESOS_DEPLOY=cluster
 fi
 sed -i "s|.*sparta.mesos.deployMode.*|sparta.mesos.deployMode = ${SPARK_MESOS_DEPLOY}|" ${SPARTA_CONF_FILE}

 if [ -v SPARK_MESOS_MASTER ] && [ ${#SPARK_MESOS_MASTER} != 0 ]; then
   sed -i "s|.*sparta.mesos.master.*|sparta.mesos.master = \""${SPARK_MESOS_MASTER}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_KILL_URL ] && [ ${#SPARK_MESOS_KILL_URL} != 0 ]; then
   sed -i "s|.*sparta.mesos.killUrl.*|sparta.mesos.killUrl = \""${SPARK_MESOS_KILL_URL}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_PRINCIPAL ] && [ ${#SPARK_MESOS_PRINCIPAL} != 0 ] && [ -v SPARK_MESOS_SECRET ] && [ ${#SPARK_MESOS_SECRET} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.mesos.principal.*|sparta.mesos.spark.mesos.principal = \""${SPARK_MESOS_PRINCIPAL}"\"|" ${SPARTA_CONF_FILE}
   sed -i "s|.*sparta.mesos.spark.mesos.secret.*|sparta.mesos.spark.mesos.secret = \""${SPARK_MESOS_SECRET}"\"|" ${SPARTA_CONF_FILE}
   if [[ ! -v SPARK_MESOS_ROLE ]]; then
      SPARK_MESOS_ROLE=sparta
   fi
   sed -i "s|.*sparta.mesos.spark.mesos.role.*|sparta.mesos.spark.mesos.role = \""${SPARK_MESOS_ROLE}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_JARS ] && [ ${#SPARK_MESOS_JARS} != 0 ]; then
   sed -i "s|.*sparta.mesos.jars.*|sparta.mesos.jars = \""${SPARK_MESOS_JARS}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_PACKAGES ] && [ ${#SPARK_MESOS_PACKAGES} != 0 ]; then
   sed -i "s|.*sparta.mesos.packages.*|sparta.mesos.packages = \""${SPARK_MESOS_PACKAGES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXCLUDE_PACKAGES ] && [ ${#SPARK_MESOS_EXCLUDE_PACKAGES} != 0 ]; then
   sed -i "s|.*sparta.mesos.exclude-packages.*|sparta.mesos.exclude-packages = \""${SPARK_MESOS_EXCLUDE_PACKAGES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_REPOSITORIES ] && [ ${#SPARK_MESOS_REPOSITORIES} != 0 ]; then
   sed -i "s|.*sparta.mesos.repositories.*|sparta.mesos.repositories = \""${SPARK_MESOS_REPOSITORIES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_PROXY_USER ] && [ ${#SPARK_MESOS_PROXY_USER} != 0 ]; then
   sed -i "s|.*sparta.mesos.proxy-user.*|sparta.mesos.proxy-user = \""${SPARK_MESOS_PROXY_USER}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_DRIVER_JAVA_OPTIONS ] && [ ${#SPARK_MESOS_DRIVER_JAVA_OPTIONS} != 0 ]; then
   sed -i "s|.*sparta.mesos.driver-java-options.*|sparta.mesos.driver-java-options = \""${SPARK_MESOS_DRIVER_JAVA_OPTIONS}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_DRIVER_LIBRARY_PATH ] && [ ${#SPARK_MESOS_DRIVER_LIBRARY_PATH} != 0 ]; then
   sed -i "s|.*sparta.mesos.driver-library-path.*|sparta.mesos.driver-library-path = \""${SPARK_MESOS_DRIVER_LIBRARY_PATH}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_DRIVER_CLASSPATH ] && [ ${#SPARK_MESOS_DRIVER_CLASSPATH} != 0 ]; then
   sed -i "s|.*sparta.mesos.driver-class-path.*|sparta.mesos.driver-class-path = \""${SPARK_MESOS_DRIVER_CLASSPATH}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [[ ! -v SPARK_MESOS_EXECUTOR_MEMORY ]]; then
   SPARK_MESOS_EXECUTOR_MEMORY=1G
 fi
 sed -i "s|.*sparta.mesos.spark.executor.memory.*|sparta.mesos.spark.executor.memory = ${SPARK_MESOS_EXECUTOR_MEMORY}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_TOTAL_EXECUTOR_CORES ]]; then
   SPARK_MESOS_TOTAL_EXECUTOR_CORES=2
 fi
 sed -i "s|.*sparta.mesos.totalExecutorCores.*|sparta.mesos.totalExecutorCores = ${SPARK_MESOS_TOTAL_EXECUTOR_CORES}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_EXECUTOR_CORES ]]; then
   SPARK_MESOS_EXECUTOR_CORES=1
 fi
 sed -i "s|.*sparta.mesos.spark.executor.cores.*|sparta.mesos.spark.executor.cores = ${SPARK_MESOS_EXECUTOR_CORES}|" ${SPARTA_CONF_FILE}

 if [ -v SPARK_MESOS_EXTRA_CORES ] && [ ${#SPARK_MESOS_EXTRA_CORES} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.mesos.extra.cores.*|sparta.mesos.spark.mesos.extra.cores = ${SPARK_MESOS_EXTRA_CORES}|" ${SPARTA_CONF_FILE}
 fi

 if [[ ! -v SPARK_MESOS_DRIVER_CORES ]]; then
   SPARK_MESOS_DRIVER_CORES=1
 fi
 sed -i "s|.*sparta.mesos.spark.driver.cores.*|sparta.mesos.spark.driver.cores = ${SPARK_MESOS_DRIVER_CORES}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARK_MESOS_DRIVER_MEMORY ]]; then
   SPARK_MESOS_DRIVER_MEMORY=1G
 fi
 sed -i "s|.*sparta.mesos.spark.driver.memory.*|sparta.mesos.spark.driver.memory = ${SPARK_MESOS_DRIVER_MEMORY}|" ${SPARTA_CONF_FILE}

 if [ -v SPARK_MESOS_SUPERVISE ] && [ ${#SPARK_MESOS_SUPERVISE} != 0 ]; then
    sed -i "s|.*sparta.mesos.supervise.*|sparta.mesos.supervise = ${SPARK_MESOS_SUPERVISE}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_CONCURRENT_JOBS ] && [ ${#SPARK_MESOS_CONCURRENT_JOBS} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.streaming.concurrentJobs.*|sparta.mesos.spark.streaming.concurrentJobs = ${SPARK_MESOS_CONCURRENT_JOBS}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_GRACEFUL_STOP ] && [ ${#SPARK_MESOS_GRACEFUL_STOP} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.streaming.stopGracefullyOnShutdown.*|sparta.mesos.spark.streaming.stopGracefullyOnShutdown = \""${SPARK_MESOS_GRACEFUL_STOP}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_GRACEFUL_STOP_TIMEOUT ] && [ ${#SPARK_MESOS_GRACEFUL_STOP_TIMEOUT} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.streaming.gracefulStopTimeout.*|sparta.mesos.spark.streaming.gracefulStopTimeout = ${SPARK_MESOS_GRACEFUL_STOP_TIMEOUT}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SERIALIZER ] && [ ${#SPARK_MESOS_SERIALIZER} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.serializer.*|sparta.mesos.spark.serializer = ${SPARK_MESOS_SERIALIZER}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_PARQUET_BINARY_AS_STRING ] && [ ${#SPARK_MESOS_PARQUET_BINARY_AS_STRING} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.sql.parquet.binaryAsString.*|sparta.mesos.spark.sql.parquet.binaryAsString = ${SPARK_MESOS_PARQUET_BINARY_AS_STRING}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_PROPERTIES_FILE ] && [ ${#SPARK_MESOS_PROPERTIES_FILE} != 0 ]; then
   sed -i "s|.*sparta.mesos.propertiesFile.*|sparta.mesos.propertiesFile = \""${SPARK_MESOS_PROPERTIES_FILE}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_DOCKER_IMAGE ] && [ ${#SPARK_MESOS_EXECUTOR_DOCKER_IMAGE} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.mesos.executor.docker.image.*|sparta.mesos.spark.mesos.executor.docker.image = \""${SPARK_MESOS_EXECUTOR_DOCKER_IMAGE}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_DOCKER_VOLUMES ] && [ ${#SPARK_MESOS_EXECUTOR_DOCKER_VOLUMES} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.mesos.executor.docker.volumes.*|sparta.mesos.spark.mesos.executor.docker.volumes = \""${SPARK_MESOS_EXECUTOR_DOCKER_VOLUMES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_DOCKER_ENV_MESOS ] && [ ${#SPARK_MESOS_EXECUTOR_DOCKER_ENV_MESOS} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.executorEnv.MESOS_NATIVE_JAVA_LIBRARY.*|sparta.mesos.spark.executorEnv.MESOS_NATIVE_JAVA_LIBRARY = \""${SPARK_MESOS_EXECUTOR_DOCKER_ENV_MESOS}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_FORCE_PULL_IMAGE ] && [ ${#SPARK_MESOS_EXECUTOR_FORCE_PULL_IMAGE} != 0 ]; then
   sed -i "s|.*spark.mesos.executor.docker.forcePullImage.*|spark.mesos.executor.docker.forcePullImage = ${SPARK_MESOS_EXECUTOR_FORCE_PULL_IMAGE}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_HOME ] && [ ${#SPARK_MESOS_EXECUTOR_HOME} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.executor.home.*|sparta.mesos.spark.executor.home = \""${SPARK_MESOS_EXECUTOR_HOME}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_EXECUTOR_URI ] && [ ${#SPARK_MESOS_EXECUTOR_URI} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.executor.uri.*|sparta.mesos.spark.executor.uri = \""${SPARK_MESOS_EXECUTOR_URI}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_URIS ] && [ ${#SPARK_MESOS_URIS} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.mesos.uris.*|sparta.mesos.spark.mesos.uris = \""${SPARK_MESOS_URIS}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SPARK_FILES ] && [ ${#SPARK_MESOS_SPARK_FILES} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.files.*|sparta.mesos.spark.files = \""${SPARK_MESOS_SPARK_FILES}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SPARK_JARS_IVY ] && [ ${#SPARK_MESOS_SPARK_JARS_IVY} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.jars.ivy.*|sparta.mesos.spark.jars.ivy = \""${SPARK_MESOS_SPARK_JARS_IVY}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v HDFS_CONF_URI ] && [ ${#HDFS_CONF_URI} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.mesos.driverEnv.HDFS_CONF_URI.*|sparta.mesos.spark.mesos.driverEnv.HDFS_CONF_URI = \""${HDFS_CONF_URI}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SPARK_LOCALITY_WAIT ] && [ ${#SPARK_MESOS_SPARK_LOCALITY_WAIT} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.locality.wait.*|sparta.mesos.spark.locality.wait = \""${SPARK_MESOS_SPARK_LOCALITY_WAIT}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SPARK_TASK_MAXFILURES ] && [ ${#SPARK_MESOS_SPARK_TASK_MAXFILURES} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.task.maxFailures.*|sparta.mesos.spark.task.maxFailures = ${SPARK_MESOS_SPARK_TASK_MAXFILURES}|" ${SPARTA_CONF_FILE}
 fi

 if [ -v SPARK_MESOS_SPARK_STREAMING_BLOCK_INTERVAL ] && [ ${#SPARK_MESOS_SPARK_STREAMING_BLOCK_INTERVAL} != 0 ]; then
   sed -i "s|.*sparta.mesos.spark.streaming.blockInterval.*|sparta.mesos.spark.streaming.blockInterval = \""${SPARK_MESOS_SPARK_STREAMING_BLOCK_INTERVAL}"\"|" ${SPARTA_CONF_FILE}
 fi
}

function marathonOptions() {

 if [[ ! -v SPARTA_MARATHON_JAR ]]; then
   SPARTA_MARATHON_JAR="/opt/sds/sparta/driver/sparta-driver.jar"
 fi
 sed -i "s|.*sparta.marathon.jar.*|sparta.marathon.jar = \""${SPARTA_MARATHON_JAR}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_MARATHON_TEMPLATE_FILE ]]; then
   SPARTA_MARATHON_TEMPLATE_FILE="/etc/sds/sparta/marathon-app-template.json"
 fi
 sed -i "s|.*sparta.marathon.template.file.*|sparta.marathon.template.file = \""${SPARTA_MARATHON_TEMPLATE_FILE}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_MARATHON_MESOSPHERE_PACKAGES ]]; then
   SPARTA_MARATHON_MESOSPHERE_PACKAGES="/opt/mesosphere/packages"
 fi
 sed -i "s|.*sparta.marathon.mesosphere.packages.*|sparta.marathon.mesosphere.packages = \""${SPARTA_MARATHON_MESOSPHERE_PACKAGES}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_DOCKER_IMAGE ]]; then
   SPARTA_DOCKER_IMAGE="qa.stratio.com/stratio/sparta:1.4.0-SNAPSHOT"
 fi
 sed -i "s|.*sparta.marathon.docker.image.*|sparta.marathon.docker.image = \""${SPARTA_DOCKER_IMAGE}"\"|" ${SPARTA_CONF_FILE}


 if [[ ! -v SPARTA_MARATHON_MESOSPHERE_LIB ]]; then
   SPARTA_MARATHON_MESOSPHERE_LIB="/opt/mesosphere/lib"
 fi
 sed -i "s|.*sparta.marathon.mesosphere.lib.*|sparta.marathon.mesosphere.lib = \""${SPARTA_MARATHON_MESOSPHERE_LIB}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_MARATHON_FORCE_PULL_IMAGE ]]; then
   SPARTA_MARATHON_FORCE_PULL_IMAGE=false
 fi
 sed -i "s|.*sparta.marathon.docker.forcePullImage.*|sparta.marathon.docker.forcePullImage = ${SPARTA_MARATHON_FORCE_PULL_IMAGE}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_MARATHON_PRIVILEGED ]]; then
   SPARTA_MARATHON_PRIVILEGED=false
 fi
 sed -i "s|.*sparta.marathon.docker.privileged.*|sparta.marathon.docker.privileged = ${SPARTA_MARATHON_PRIVILEGED}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_MARATHON_GRACEPERIODS_SECONDS ]]; then
   SPARTA_MARATHON_GRACEPERIODS_SECONDS=180
 fi
 sed -i "s|.*sparta.marathon.gracePeriodSeconds.*|sparta.marathon.gracePeriodSeconds = ${SPARTA_MARATHON_GRACEPERIODS_SECONDS}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_MARATHON_INTERVAL_SECONDS ]]; then
   SPARTA_MARATHON_INTERVAL_SECONDS=60
 fi
 sed -i "s|.*sparta.marathon.intervalSeconds.*|sparta.marathon.intervalSeconds = ${SPARTA_MARATHON_INTERVAL_SECONDS}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_MARATHON_TIMEOUT_SECONDS ]]; then
   SPARTA_MARATHON_TIMEOUT_SECONDS=20
 fi
 sed -i "s|.*sparta.marathon.timeoutSeconds.*|sparta.marathon.timeoutSeconds = ${SPARTA_MARATHON_TIMEOUT_SECONDS}|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_MARATHON_MAX_FAILURES ]]; then
   SPARTA_MARATHON_MAX_FAILURES=3
 fi
 sed -i "s|.*sparta.marathon.maxConsecutiveFailures.*|sparta.marathon.maxConsecutiveFailures = ${SPARTA_MARATHON_MAX_FAILURES}|" ${SPARTA_CONF_FILE}

 if [ -v MARATHON_SSO_URI ] && [ ${#MARATHON_SSO_URI} != 0 ]; then
   sed -i "s|.*sparta.marathon.sso.uri.*|sparta.marathon.sso.uri = \""${MARATHON_SSO_URI}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v MARATHON_SSO_USERNAME ] && [ ${#MARATHON_SSO_USERNAME} != 0 ]; then
   sed -i "s|.*sparta.marathon.sso.username.*|sparta.marathon.sso.username = \""${MARATHON_SSO_USERNAME}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v MARATHON_SSO_PASSWORD ] && [ ${#MARATHON_SSO_PASSWORD} != 0 ]; then
   sed -i "s|.*sparta.marathon.sso.username.*|sparta.marathon.sso.username = \""${MARATHON_SSO_PASSWORD}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v MARATHON_SSO_CLIENT_ID ] && [ ${#MARATHON_SSO_CLIENT_ID} != 0 ]; then
   sed -i "s|.*sparta.marathon.sso.clientId.*|sparta.marathon.sso.clientId = \""${MARATHON_SSO_CLIENT_ID}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v MARATHON_SSO_REDIRECT_URI ] && [ ${#MARATHON_SSO_REDIRECT_URI} != 0 ]; then
   sed -i "s|.*sparta.marathon.sso.redirectUri.*|sparta.marathon.sso.redirectUri = \""${MARATHON_SSO_REDIRECT_URI}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v MARATHON_TIKI_TAKKA_MARATHON_URI ] && [ ${#MARATHON_TIKI_TAKKA_MARATHON_URI} != 0 ]; then
   sed -i "s|.*sparta.marathon.tikitakka.marathon.uri.*|sparta.marathon.tikitakka.marathon.uri = \""${MARATHON_TIKI_TAKKA_MARATHON_URI}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ -v MARATHON_TIKI_TAKKA_MARATHON_API_VERSION ] && [ ${#MARATHON_TIKI_TAKKA_MARATHON_API_VERSION} != 0 ]; then
   sed -i "s|.*sparta.marathon.tikitakka.marathon.api.version.*|sparta.marathon.tikitakka.marathon.api.version = \""${MARATHON_TIKI_TAKKA_MARATHON_API_VERSION}"\"|" ${SPARTA_CONF_FILE}
 fi
}

function goSecOptions(){

if [ -v SPARTA_SECURITY_MANAGER_ENABLED ] && [ $SPARTA_SECURITY_MANAGER_ENABLED=="true" ]; then
   sed -i "s|.*sparta.security.manager.enabled.*|sparta.security.manager.enabled = \""${SPARTA_SECURITY_MANAGER_ENABLED}"\"|" ${SPARTA_CONF_FILE}
   sed -i "s|.*sparta.security.manager.class.*|sparta.security.manager.class = \""${SPARTA_SECURITY_MANAGER_CLASS}"\"|" ${SPARTA_CONF_FILE}
fi
}
