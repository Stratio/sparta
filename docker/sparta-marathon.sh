#!/bin/bash -e

 SPARTA_MARATHON_CONF_FILE=/etc/sds/sparta/marathon/reference.conf
 cp SPARTA_MARATHON_CONF_FILE SPARTA_CONF_FILE

 source sparta-marathon-utils.sh

 # SPARTA JAVA OPTIONS
 initJavaOptions

 # SPARK OPTIONS
 initSpark

 # HDFS OPTIONS
 initHdfs

 # Load Sparta and system variables
 loadVariables

  # Marathon App LOG OPTIONS
 logLevelOptions
 logLevelToStdout

 # Run Sparta Marathon jar
 run-marathon-app.sh >> /dev/null 2>$LOG_FILE & echo $! >$PIDFILE
