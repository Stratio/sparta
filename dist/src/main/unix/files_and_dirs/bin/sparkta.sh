#!/bin/bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# Get options
while getopts "p:l:" option; do
  case $option in
     p)      PIDFILE=$OPTARG ;;
     l)      LOGFILE=$OPTARG ;;
     *)      echo "Unknown option" ; exit 1 ;;
  esac
done

# Set defatult values
PIDFILE=${PIDFILE:-"/var/run/sds/sparkta.pid"}

bash $DIR/run >> /dev/null 2>&1 & echo $! >$PIDFILE