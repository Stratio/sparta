#!/bin/bash -e

 source sparta-server-utils.sh

 # SPARTA JAVA OPTIONS
 initJavaOptions

 # SPARK OPTIONS
 initSpark

 # HDFS OPTIONS
 initHdfs

 # Load Sparta and system variables
 loadVariables

 # NOW SUBSTITUTE CONFIGURATION OPTIONS IN SPARTA SERVER CONFIGURATION FILE FROM ENVIRONMENT VARIABLES

 # HDFS
 hdfsOptions

 # SPARTA LOG LEVEL OPTIONS
 logLevelOptions

 # SPARTA API OPTIONS
 apiOptions

 # OAUTH2 OPTIONS
 oauthOptions

 # SPARTA ZOOKEEPER OPTIONS
 zookeeperOptions

 # SPARTA CONFIGURATION OPTIONS
 configOptions

 # LOCAL EXECUTION OPTIONS
 localSparkOptions

 # MESOS EXECUTION OPTIONS
 mesosSparkOptions

 # MARATHON OPTIONS
 marathonOptions

 if [[ ! -v RUN_MODE ]]; then
   RUN_MODE="production"
 fi
 case "$RUN_MODE" in
   "debug") # In this mode, Sparta will be launched as a service within the docker container.
     logLevelToFile
     service sparta start
     tail -F /var/log/sds/sparta/sparta.log
     ;;
   *) # Default mode: Sparta run as a docker application
     logLevelToStdout
     /opt/sds/sparta/bin/run
     ;;
 esac
