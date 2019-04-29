#!/bin/bash -e

 SPARTA_MARATHON_CONF_FILE=/etc/sds/sparta/marathon/reference.conf
 SPARTA_SPARK_CONF_FILE=/etc/sds/sparta/spark/reference.conf
 cp ${SPARTA_MARATHON_CONF_FILE} ${SPARTA_CONF_FILE}

 INFO "[SPARTA-MARATHON] Loading Sparta common functions ... "
 source /sparta-common.sh
 INFO "[SPARTA-MARATHON] Loaded Sparta common functions"

 INFO "[SPARTA-MARATHON] Loading Sparta marathon functions ... "
 source /sparta-marathon-utils.sh
 INFO "[SPARTA-MARATHON] Loaded Sparta marathon functions"

 INFO "[SPARTA-MARATHON] Creating Akka network variables ... "
 initAkkaNetwork
 INFO "[SPARTA-MARATHON] Created Akka network variables"

 INFO "[SPARTA-MARATHON] Creating metrics variables ... "
 initMetricsVariables
 INFO "[SPARTA-MARATHON] Created metrics variables"

 INFO "[SPARTA-MARATHON] Creating persistence paths ... "
 initPersistencePaths
 INFO "[SPARTA-MARATHON] Created persistence paths"

 INFO "[SPARTA-MARATHON] Loading Sparta Java options ... "
 initJavaOptions
 INFO "[SPARTA-MARATHON] Loaded Sparta Java options"

 INFO "[SPARTA-MARATHON] Loading Crossdata plugin ... "
 initPluginCrossdata
 INFO "[SPARTA-MARATHON] Loaded Crossdata plugin "

 INFO "[SPARTA-MARATHON] Loading Sparta Spark options ... "
 initSpark
 initSparkEnvOptions
 initClusterSparkIp
 initSparkDefaultsOptions
 INFO "[SPARTA-MARATHON] Loaded Sparta Spark options"

 INFO "[SPARTA-MARATHON] Loading Sparta Hdfs options ... "
 initHdfs
 INFO "[SPARTA-MARATHON] Loaded Sparta Hdfs options"

 INFO "[SPARTA-MARATHON] Loading Sparta and system variables ... "
 loadVariables
 INFO "[SPARTA-MARATHON] Loaded Sparta and system variables"

 INFO "[SPARTA-MARATHON] Selecting log appender ... "
 logLevelOptions
 logLevelAppender
 INFO "[SPARTA-MARATHON] Log appender selected"

 INFO "[SPARTA-MARATHON] Adding extra properties ... "
 extraProperties ${SPARTA_CONF_FILE}
 extraProperties ${SPARTA_MARATHON_CONF_FILE}
 extraProperties ${SPARTA_SPARK_CONF_FILE}
 INFO "[SPARTA-MARATHON] Extra properties added"

 # Run Sparta Marathon jar
 INFO "[SPARTA-MARATHON] Running Sparta marathon application ... "
 source /run-marathon-app.sh
