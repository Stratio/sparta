#!/bin/bash -e

 INFO "[SPARTA-SERVER] Loading Sparta common functions ... "
 source /sparta-common.sh
 INFO "[SPARTA-SERVER] Loaded Sparta common functions"

 INFO "[SPARTA-SERVER] Loading Sparta server functions ... "
 source /sparta-server-utils.sh
 INFO "[SPARTA-SERVER] Loaded Sparta server functions"

 INFO "[SPARTA-SERVER] Creating Akka network variables ... "
 initAkkaNetwork
 INFO "[SPARTA-SERVER] Created Akka network variables"

 INFO "[SPARTA-MARATHON] Creating persistence paths ... "
 initPersistencePaths
 INFO "[SPARTA-MARATHON] Created persistence paths"

 INFO "[SPARTA-SERVER] Loading Sparta Java options ... "
 initJavaOptions
 INFO "[SPARTA-SERVER] Loaded Sparta Java options"

 INFO "[SPARTA-SERVER] Loading Crossdata plugin ... "
 initPluginCrossdata
 INFO "[SPARTA-SERVER] Loaded Crossdata plugin "

 INFO "[SPARTA-SERVER] Loading Sparta Spark options ... "
 initSpark
 initSparkEnvOptions
 initLocalSparkIp
 initSparkUICrossdata
 INFO "[SPARTA-SERVER] Loaded Sparta Spark options"

 INFO "[SPARTA-SERVER] Initializing Sparta Hdfs options ... "
 initHdfs
 INFO "[SPARTA-SERVER] Initialized Sparta Hdfs options"

 INFO "[SPARTA-SERVER] Initializing Sparta datastoreTls options ... "
 initDatastoreTls
 INFO "[SPARTA-SERVER] Initialized Sparta datastoreTls options"

 INFO "[SPARTA-SERVER] Loading Sparta and system variables ... "
 loadVariables
 INFO "[SPARTA-SERVER] Loaded Sparta and system variables"

 INFO "[SPARTA-SERVER] Loading Sparta API options ... "
 apiOptions
 INFO "[SPARTA-SERVER] Loaded Sparta API options"

 if [ -v MARATHON_APP_LABEL_HAPROXY_1_VHOST ] ; then
   INFO "[SPARTA-SERVER] Configuring Nginx environment..."
   prepareNginx
   INFO "[SPARTA-SERVER] Nginx environment configured"
 fi

 INFO "[SPARTA-SERVER] Selecting log appender ... "
 logLevelOptions
 logLevelAppender
 INFO "[SPARTA-SERVER] Log appender selected"

 INFO "[SPARTA-SERVER] Running Sparta server ... "
 export SPARTA_OPTS="$SPARTA_OPTS -Dconfig.file=$SPARTA_CONF_FILE -Djava.util.logging.config.file=file://$LOG_CONFIG_FILE"
 /opt/sds/sparta/bin/run

