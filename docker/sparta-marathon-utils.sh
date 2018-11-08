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
   export MARATHON_APP_HEAP_SIZE=-Xmx512m
 fi

 if [[ ! -v MARATHON_APP_HEAP_MINIMUM_SIZE ]]; then
   export MARATHON_APP_HEAP_MINIMUM_SIZE=-Xms256m
 fi

 if [ -v SPARTA_JAAS_FILE ] && [ ${#SPARTA_JAAS_FILE} != 0 ]; then
   export SPARTA_CONFIG_JAAS_FILE="-Djava.security.auth.login.config=${SPARTA_JAAS_FILE}"
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

function initSparkDefaultsOptions() {

  if [ -v PORT_SPARKUI ] && [ ${#PORT_SPARKUI} != 0 ]; then
    echo "spark.ui.port=${PORT_SPARKUI}" >> ${SPARK_CONF_DEFAULTS_FILE}
  fi

}