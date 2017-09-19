#!/usr/bin/env bash

set -u -e -o pipefail

set +x

# Default options
COMPILE=false
API_URL="http://localhost:9000"
BASE_PATH="/"
APP_PORT=80

# Parse options
while [[ $# -gt 0 ]]; do
   key="$1"

   case $key in
      -c|--compile)
         COMPILE=true
      ;;
      -a|--api)
         API_URL="$2"
         shift # past argument
      ;;
      -p|--port)
         APP_PORT="$2"
         shift # past argument
      ;;
      -p|--basepath)
         BASE_PATH="$2"
         shift # past argument
      ;;
      --default) # unknown option
         DEFAULT=YES
      ;;
      *)

      # echo "Unknown option, use -m|--mock for up mockserver -a|--api for api url, -b|--backoffice for backoffice url, -c|--compile for compile before, -p|--basepath for base path"
      echo "Unknown option, use -m|--mock for up mockserver -a|--api for api url, -b|--backoffice for backoffice url, -c|--compile for compile before"
      exit 1;
      ;;
   esac
   shift
done


# compile if request by user
if [[ $COMPILE == true ]]; then
  npm run build
fi


# Remove old image if exists
IMG_ID=`docker ps -aq --filter "ancestor=egeo-starter"`
if [[ $IMG_ID ]]; then
   docker rm -f $IMG_ID
fi

docker build -t egeo-starter .
docker run -p ${APP_PORT}:8080 -e "API_URL=${API_URL}" -e "BASE_PATH=${BASE_PATH}" -itd egeo-starter

set -x