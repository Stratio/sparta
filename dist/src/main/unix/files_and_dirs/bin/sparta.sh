#!/bin/bash
#
# Copyright (C) 2015 Stratio (http://stratio.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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
PIDFILE=${PIDFILE:-"/var/run/sds/sparta.pid"}

bash $DIR/run >> /dev/null 2>/var/log/sds/sparta/sparta.log & echo $! >$PIDFILE