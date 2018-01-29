#!/bin/bash

function initDatastoreTls() {
    if [ -v SECURITY_TLS_ENABLE ] && [ ${#SECURITY_TLS_ENABLE} != 0 ] && [ $SECURITY_TLS_ENABLE == "true" ] ; then
        echo "" >> ${VARIABLES}
        echo "export SPARK_SECURITY_DATASTORE_ENABLE=true" >> ${VARIABLES}
        echo "" >> ${SYSTEM_VARIABLES}
        echo "export SPARK_SECURITY_DATASTORE_ENABLE=true" >> ${SYSTEM_VARIABLES}
        echo "" >> ${VARIABLES}
        echo "export SPARK_SECURITY_DATASTORE_ENABLE=true" >> ${VARIABLES}
        echo "" >> ${SYSTEM_VARIABLES}
        echo "export SPARK_SECURITY_DATASTORE_ENABLE=true" >> ${SYSTEM_VARIABLES}
    fi
}

function initLocalSparkIp() {
    if ([ -v CROSSDATA_SERVER_CONFIG_SPARK_MASTER ] && [ $CROSSDATA_SERVER_CONFIG_SPARK_MASTER == "local[*]" ]) || [ ! -v LIBPROCESS_IP ] ; then
        echo "" >> ${VARIABLES}
        echo "export SPARK_LOCAL_IP=127.0.0.1" >> ${VARIABLES}
        echo "" >> ${SYSTEM_VARIABLES}
        echo "export SPARK_LOCAL_IP=127.0.0.1" >> ${SYSTEM_VARIABLES}
    elif [ -v LIBPROCESS_IP ] && [ ${#LIBPROCESS_IP} != 0 ]; then
        echo "" >> ${VARIABLES}
        echo "export SPARK_LOCAL_IP=$LIBPROCESS_IP" >> ${VARIABLES}
        echo "" >> ${SYSTEM_VARIABLES}
        echo "export SPARK_LOCAL_IP=$LIBPROCESS_IP" >> ${SYSTEM_VARIABLES}
    fi
}

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

 if [ -v HADOOP_PORT ] && [ ${#HADOOP_PORT} != 0 ]; then
   sed -i "s|.*sparta.hdfs.hdfsPort.*|sparta.hdfs.hdfsPort = ${HADOOP_PORT}|" ${SPARTA_CONF_FILE}
 fi

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

  if [[ ! -v SPARTA_TIMEOUT_API_CALLS ]]; then
   SPARTA_TIMEOUT_API_CALLS=20
 fi
 sed -i "s|.*spray.can.server.request-timeout.*|spray.can.server.request-timeout = ${SPARTA_TIMEOUT_API_CALLS}s|" ${SPARTA_CONF_FILE}

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

 if [ -v SPARTA_ZOOKEEPER_PATH ] && [ ${#SPARTA_ZOOKEEPER_PATH} != 0 ]; then
  sed -i "s|.*sparta.zookeeper.storagePath.*|sparta.zookeeper.storagePath = \""${SPARTA_ZOOKEEPER_PATH}"\"|" ${SPARTA_CONF_FILE}
 fi

 if [ ! -v SPARTA_ZOOKEEPER_PATH ] && [ -v MARATHON_APP_LABEL_DCOS_SERVICE_NAME ] && [ ${#MARATHON_APP_LABEL_DCOS_SERVICE_NAME} != 0 ]; then
   sed -i "s|.*sparta.zookeeper.storagePath.*|sparta.zookeeper.storagePath = \"/stratio/sparta/"${MARATHON_APP_LABEL_DCOS_SERVICE_NAME}"\"|" ${SPARTA_CONF_FILE}
 fi

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

 if [[ ! -v SPARTA_DRIVER_PACKAGE_LOCATION ]]; then
   SPARTA_DRIVER_PACKAGE_LOCATION="/opt/sds/sparta/driver"
 fi
 sed -i "s|.*sparta.config.driverPackageLocation.*|sparta.config.driverPackageLocation = \""${SPARTA_DRIVER_PACKAGE_LOCATION}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_PLUGIN_PACKAGE_LOCATION ]]; then
   SPARTA_PLUGIN_PACKAGE_LOCATION="plugins"
 fi
 sed -i "s|.*sparta.config.pluginsLocation.*|sparta.config.pluginsLocation = \""${SPARTA_PLUGIN_PACKAGE_LOCATION}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_BACKUP_LOCATION ]]; then
   SPARTA_BACKUP_LOCATION="/opt/sds/sparta/backups"
 fi
 sed -i "s|.*sparta.config.backupsLocation.*|sparta.config.backupsLocation = \""${SPARTA_BACKUP_LOCATION}"\"|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPARTA_AWAIT_POLICY_CHANGE_STATUS ]]; then
   SPARTA_AWAIT_POLICY_CHANGE_STATUS=320s
 fi
 sed -i "s|.*sparta.config.awaitWorkflowChangeStatus.*|sparta.config.awaitWorkflowChangeStatus = ${SPARTA_AWAIT_POLICY_CHANGE_STATUS}|" ${SPARTA_CONF_FILE}

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
   SPARTA_DOCKER_IMAGE="qa.stratio.com/stratio/sparta:2.0.0-SNAPSHOT"
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
   SPARTA_MARATHON_GRACEPERIODS_SECONDS=240
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
   sed -i "s|.*sparta.marathon.sso.password.*|sparta.marathon.sso.password = \""${MARATHON_SSO_PASSWORD}"\"|" ${SPARTA_CONF_FILE}
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

function prepareNginx(){
   #Make cert.crt usable for Nginx by limiting each of its base64 line lengths to 65
   if [ -v SECURITY_TLS_ENABLE ] && [ ${#SECURITY_TLS_ENABLE} != 0 ] && [ $SECURITY_TLS_ENABLE == "true" ]; then
    fold -w65 /tmp/cert.crt > /tmp/nginx_cert.crt
   fi
}
