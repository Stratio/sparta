#!/bin/bash

RED="\e[1;31m"
RED_SOFT="\e[0;31m"
GREEN="\e[1;32m"
GREEN_SOFT="\e[0;32m"
YELLOW="\e[1;33m"
NO_COLOR="\e[0m"

checkPort() {
   PORT=$1
   DOCKER=$2
   OUTPUT=""
   sentinel=0
   while [ "$OUTPUT" == "" ] && [ "$sentinel" -lt 6 ]
      do
         OUTPUT="$(CHECK_PORT_PORT=${PORT} CHECK_PORT_DOCKER=${DOCKER} ./node_modules/babel-cli/bin/babel-node.js ./tools/launcher/check-port.js)"
         if [ "$OUTPUT" == "" ]; then
            sleep 5
            let sentinel=sentinel+1
            if [ "$sentinel" -eq 1 ]; then
               printf >&2 "\n"
            fi
            printf >&2 "[${RED}$sentinel/6${NO_COLOR}] Trying to check port ${YELLOW}$PORT${NO_COLOR}\n"
         fi
      done
   echo "$OUTPUT"
}

OWNER="[${GREEN_SOFT}check-server.sh${NO_COLOR}] "

portAPI="$(checkPort 9090)"
if [ "$portAPI" != "" ]; then
  printf "\n${OWNER}${GREEN}Serving API is ready to work. Listening on port 9090${NO_COLOR}\n"
  printf "${OWNER}${GREEN}$portAPI${NO_COLOR}\n\n"
fi
