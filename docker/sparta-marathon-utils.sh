#!/bin/bash

function initJavaOptions() {

 if [[ ! -v MARATHON_APP_HEAP_SIZE ]]; then
   export MARATHON_APP_HEAP_SIZE=-Xmx2048m
 fi

 if [[ ! -v MARATHON_APP_HEAP_MINIMUM_SIZE ]]; then
   export MARATHON_APP_HEAP_MINIMUM_SIZE=-Xms1024m
 fi
}
