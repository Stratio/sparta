#!/bin/bash
#
# © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
#
# This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
#

#
#   /etc/rc.d/init.d/sparta
#
#   Sparta
#
### BEGIN INIT INFO
# Provides:          Sparta
# Required-Start:    $network $remote_fs $named
# Required-Stop:     $network $remote_fs $named
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Starts Sparta
# Description:       Starts Sparta real time aggregation engine
### END INIT INFO

# Source function library.
. /etc/init.d/functions

# Only configuration needed here
NAME="sparta"
DESC="Sparta: Real-Time Analytics Engine"

PATH=/bin:/usr/bin:/sbin:/usr/sbin
VARIABLES="/etc/default/$NAME-variables"
FUNCTIONS="/etc/default/$NAME-functions"

# Source function library.
. /etc/rc.d/init.d/functions

# Source default configuration
if [ -r $VARIABLES ]; then
    . $VARIABLES
fi

# Source check functions & validate service configuration
. $FUNCTIONS && validateService

start() {
    validateStart
    echo -n "Starting $NAME: "
    daemon --user $USER --pidfile $PIDFILE "$DAEMON start $DAEMON_OPTS"
    echo
    touch $LOCKFILE
}

stop() {
    validateStop
    echo -n "Shutting down $NAME: "
    killproc -p $PIDFILE $NAME
    local exit_status=$?
    echo
    rm -f $LOCKFILE
    return $exit_status
}

restart(){
    validateRestart
    stop
    sleep 1
    start
}

condrestart(){
    if [ -f /var/lock/subsys/$NAME ]; then
        restart || { echo "won't restart: $NAME is stopped" ; exit 1 ; }
    fi
}

getStatus(){
    status -p $PIDFILE $NAME -l $LOCKFILE
}

usage(){
    echo "Usage: $NAME {start|stop|status|restart|reload|force-reload|condrestart}"
    exit 1
}

case "$1" in
    start)                          start ;;
    stop)                           stop ;;
    status)                         getStatus ;;
    restart|reload|force-reload)    restart ;;
    condrestart)                    condrestart ;;
    *)                              usage ;;
esac
exit $?
