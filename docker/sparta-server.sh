#!/bin/bash -e

function _log_sparta_server() {
    local message=$1
    echo "[SPARTA-SERVER] $message"
}

_log_sparta_server "Loading Sparta server functions ... "
 source /sparta-server-utils.sh
 _log_sparta_server "Loaded Sparta server functions"

 _log_sparta_server "Loading Sparta Java options ... "
 initJavaOptions
 _log_sparta_server "Loaded Sparta Java options"

 _log_sparta_server "Loading Sparta Spark options ... "
 initSpark
 _log_sparta_server "Loaded Sparta Spark options"

 _log_sparta_server "Initializing Sparta Hdfs options ... "
 initHdfs
 _log_sparta_server "Initialized Sparta Hdfs options"

 _log_sparta_server "Loading Sparta and system variables ... "
 loadVariables
 _log_sparta_server "Loaded Sparta and system variables"

 # NOW SUBSTITUTE CONFIGURATION OPTIONS IN SPARTA SERVER CONFIGURATION FILE FROM ENVIRONMENT VARIABLES

 _log_sparta_server "Loading Sparta Hdfs options ... "
 hdfsOptions
 _log_sparta_server "Loaded Sparta Hdfs options"

 _log_sparta_server "Loading Sparta Log options ... "
 logLevelOptions
 _log_sparta_server "Loaded Sparta Log options"

 _log_sparta_server "Loading Sparta API options ... "
 apiOptions
 _log_sparta_server "Loaded Sparta API options"

 _log_sparta_server "Loading Sparta OAUTH options ... "
 oauthOptions
 _log_sparta_server "Loaded Sparta OAUTH options"

 _log_sparta_server "Loading Sparta Zookeeper options ... "
 zookeeperOptions
 _log_sparta_server "Loaded Sparta Zookeeper options"

 _log_sparta_server "Loading Sparta Config options ... "
 configOptions
 _log_sparta_server "Loaded Sparta Config options"

 _log_sparta_server "Loading Sparta local Spark options ... "
 localSparkOptions
 _log_sparta_server "Loaded Sparta local Spark options"

 _log_sparta_server "Loading Sparta Mesos Spark options ... "
 mesosSparkOptions
 _log_sparta_server "Loaded Sparta Mesos Spark options"

 _log_sparta_server "Loading Sparta Marathon options ... "
 marathonOptions
 _log_sparta_server "Loaded Sparta Marathon options"

 # GOSEC OPTIONS
 goSecOptions

 if [[ ! -v RUN_MODE ]]; then
   RUN_MODE="production"
 fi
 case "$RUN_MODE" in
   "debug") # In this mode, Sparta will be launched as a service within the docker container.
     _log_sparta_server "Running Sparta server in debug mode ... "
     logLevelToFile
     service sparta start
     tail -F /var/log/sds/sparta/sparta.log
     ;;
   *) # Default mode: Sparta run as a docker application
     _log_sparta_server "Running Sparta server in production mode ... "
     logLevelToStdout
     /opt/sds/sparta/bin/run
     ;;
 esac
