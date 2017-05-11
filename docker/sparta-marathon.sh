#!/bin/bash -e

function _log_sparta_marathon() {
    local message=$1
    echo "[SPARTA-MARATHON] $message"
}

 _log_sparta_marathon "Loading Sparta and system variables ... "
 loadVariables
 _log_sparta_marathon "Loaded Sparta and system variables"

 SPARTA_MARATHON_CONF_FILE=/etc/sds/sparta/marathon/reference.conf
 cp ${SPARTA_MARATHON_CONF_FILE} ${SPARTA_CONF_FILE}

 _log_sparta_marathon "Loading Sparta marathon functions ... "
 source /sparta-marathon-utils.sh
 _log_sparta_marathon "Loaded Sparta marathon functions"

 _log_sparta_marathon "Loading Sparta Java options ... "
 initJavaOptions
 _log_sparta_marathon "Loaded Sparta Java options"

 _log_sparta_marathon "Loading Sparta Spark options ... "
 initSpark
 initSparkEnvOptions
 initSparkDefaultsOptions
 _log_sparta_marathon "Loaded Sparta Spark options"

 _log_sparta_marathon "Loading Sparta Hdfs options ... "
 initHdfs
 _log_sparta_marathon "Loaded Sparta Hdfs options"

 _log_sparta_marathon "Loading Sparta Log options ... "
 logLevelOptions
 logLevelToStdout
 _log_sparta_marathon "Loaded Sparta Log options"

 # Run Sparta Marathon jar
 #run-marathon-app.sh >> /dev/null 2>$LOG_FILE & echo $! >$PIDFILE
 _log_sparta_marathon "Running Sparta marathon application ... "
 source /run-marathon-app.sh
