#!/bin/bash
#
# © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
#
# This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
#

NAME="sparta"
VARIABLES="/etc/default/$NAME-variables"

# Source service configuration
# Source service configuration
if [ -r $VARIABLES ]; then
    . $VARIABLES
fi

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# Get options
while getopts "p:l:D:X:" option; do
   case $option in
      p)    PIDFILE="${OPTARG}" ;;
      l)    LOGFILE="${OPTARG}" ;;
      *)    echo "Unknown option" ; exit 1 ;;
   esac
done

bash $DIR/run >> /dev/null 2>$LOG_FILE & echo $! >$PIDFILE