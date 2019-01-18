#!/bin/bash

function initDatastoreTls() {
    if [ -v SECURITY_TLS_ENABLE ] && [ ${#SECURITY_TLS_ENABLE} != 0 ] && [ $SECURITY_TLS_ENABLE == "true" ] ; then
        echo "" >> ${VARIABLES}
        echo "export SPARK_SECURITY_DATASTORE_ENABLE=true" >> ${VARIABLES}
        echo "" >> ${SYSTEM_VARIABLES}
        echo "export SPARK_SECURITY_DATASTORE_ENABLE=true" >> ${SYSTEM_VARIABLES}
        echo "" >> ${VARIABLES}
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

function initSparkUICrossdata() {
    if [ -v MARATHON_APP_LABEL_HAPROXY_1_VHOST ] ; then
        echo "" >> ${VARIABLES}
        echo "export APPLICATION_WEB_PROXY_BASE=/workflows-${MARATHON_APP_LABEL_DCOS_SERVICE_NAME}/crossdata-sparkUI" >> ${VARIABLES}
        echo "" >> ${SYSTEM_VARIABLES}
        echo "export APPLICATION_WEB_PROXY_BASE=/workflows-${MARATHON_APP_LABEL_DCOS_SERVICE_NAME}/crossdata-sparkUI" >> ${SYSTEM_VARIABLES}
        echo "" >> ${VARIABLES}
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

function initPluginCrossdata() {

 if [ -v CROSSDATA_SECURITY_MANAGER_ENABLED ] && [ $CROSSDATA_SECURITY_MANAGER_ENABLED == "true" ]; then
    INFO "[GOSEC-CROSSDATA-CONFIG] Choose version: OK"
    INFO "[GOSEC-CROSSDATA-CONFIG] dyplon-crossdata-${GOSEC_CROSSDATA_VERSION//./\\.}.*\.jar"
    PLUGIN_FILES=(`ls -d -1  /opt/sds/sparta/crossdata/{*,.*} | grep -e "dyplon-crossdata-${GOSEC_CROSSDATA_VERSION//./\\.}.*\.jar"`)
    INFO "[GOSEC-CROSSDATA-CONFIG] Version choosed: ${PLUGIN_FILES} OK"

    case "${#PLUGIN_FILES[*]}" in
            0)
                ERROR "[GOSEC-CROSSDATA-CONFIG]GoSec Version (${GOSEC_CROSSDATA_VERSION}) is not compatible with the current version of Crossdata"
                exit 1
                ;;
            1)
                DYPLON_PLUGIN="${PLUGIN_FILES[0]}"
                if [ -f  ${DYPLON_PLUGIN} ]; then

                    INFO "[GOSEC-CROSSDATA-CONFIG] Copying Dyplon GoSec plugin in classpath"
                    cp --preserve ${DYPLON_PLUGIN} /opt/sds/sparta/repo/
                    INFO "[GOSEC-CROSSDATA-CONFIG] Copied Dyplon GoSec plugin in classpath"

                else
                    ERROR "[GOSEC-CROSSDATA-CONFIG]GoSec Version (${GOSEC_VERSION}) is not compatible with the current version of Crossdata"
                    exit 1
                fi
                ;;
            *)
                ERROR "[GOSEC-CROSSDATA-CONFIG]More than 1 available plugin for provided GoSec Version (${GOSEC_VERSION})"
                for each_plugin in "${PLUGIN_FILES[@]}"
                do
                    ERROR "[GOSEC-CROSSDATA-CONFIG]Available Dyplon GoSec plugin version: [$each_plugin]";
                done
                exit 1
                ;;
        esac
 fi
}

function apiOptions() {
 if [[ ! -v SPARTA_TIMEOUT_API_CALLS ]]; then
   SPARTA_TIMEOUT_API_CALLS=20
 fi
 sed -i "s|.*spray.can.server.request-timeout.*|spray.can.server.request-timeout = ${SPARTA_TIMEOUT_API_CALLS}s|" ${SPARTA_CONF_FILE}

 if [[ ! -v SPRAY_CAN_SERVER_SSL_ENCRYPTION ]]; then
   SPRAY_CAN_SERVER_SSL_ENCRYPTION_SWITCH=off
   elif [[ $SPRAY_CAN_SERVER_SSL_ENCRYPTION == "true" ]]; then
    SPRAY_CAN_SERVER_SSL_ENCRYPTION_SWITCH=on
   else
    SPRAY_CAN_SERVER_SSL_ENCRYPTION_SWITCH=off
 fi
 sed -i "s|.*spray.can.server.ssl-encryption.*|spray.can.server.ssl-encryption = ${SPRAY_CAN_SERVER_SSL_ENCRYPTION_SWITCH}|" ${SPARTA_CONF_FILE}
}

function prepareNginx(){

   if [ -v SECURITY_TLS_ENABLE ] && [ ${#SECURITY_TLS_ENABLE} != 0 ] && [ $SECURITY_TLS_ENABLE == "true" ]; then
    rm /etc/nginx/sites-available/default
    touch /etc/nginx/nginx.conf
   fi

}
