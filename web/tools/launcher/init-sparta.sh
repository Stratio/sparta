#!/bin/bash

POSTGRES_PORT="5432"
SERVING_API_PORT="9090"

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

checkPortFast() {
   PORT=$1
   DOCKER=$2
   OUTPUT="$(CHECK_PORT_PORT=${PORT} CHECK_PORT_DOCKER=${DOCKER} ./node_modules/babel-cli/bin/babel-node.js ./tools/launcher/check-port.js)"
   echo "$OUTPUT"
}

OWNER="[${GREEN_SOFT}init-sparta.sh${NO_COLOR}] "

###### POSTGRES ######

source ./.env
postgresRunning="$(checkPortFast ${POSTGRES_PORT})"
if [ "$postgresRunning" != "" ]; then
   printf "\n${OWNER}${GREEN}Postgres database is ready to work.${NO_COLOR}\n"
   printf "${OWNER}${GREEN}$postgresRunning${NO_COLOR}\n\n"
else
   containerUp="$(docker ps -q -f name=postgres)"
   container="$(docker ps -a -q -f name=postgres)"
   if [ "$containerUp" != "" ]; then
      printf "\n${OWNER}${GREEN}Docker postgres is running on port ${POSTGRES_PORT}.${NO_COLOR}\n\n"
   elif [ "$container" == "" ]; then
      docker run --name postgres -p ${POSTGRES_PORT}:${POSTGRES_PORT} -d postgres
      printf "\n${OWNER}${GREEN}Docker postgres created and started. Running on port ${POSTGRES_PORT}.${NO_COLOR}\n\n"
   else
      docker start ${container}
      printf "\n${OWNER}${GREEN}Docker postgres started. Running on port ${POSTGRES_PORT}.${NO_COLOR}\n\n"
   fi

   portPostgres="$(checkPort ${POSTGRES_PORT} docker)"
   if [ "$portPostgres" != "" ]; then
      printf "\n${OWNER}${GREEN}Postgres database is ready to work.${NO_COLOR}\n"
      printf "${OWNER}${GREEN}$portPostgres${NO_COLOR}\n\n"
   else
      printf "\n${OWNER}${YELLOW}Unable to check if database port is ready.${NO_COLOR}\n\n"
   fi
fi

###### PG ADMIN ######

pgAdminUp="$(docker ps -q -f name=pgadmin)"
container="$(docker ps -q -f name=postgres)"
line="$(docker exec ${container} cat etc/hosts | grep ${container})"
ip="$(grep -oE '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' <<< "${line}")"

if [ "$pgAdminUp" != "" ]; then
   printf "\n${OWNER}${GREEN}Docker pgAdmin is running on port 80.${NO_COLOR}\n"
   printf "To configure pgAdmin to connect database you will need Postgres internal container IP:\n"
   printf "HOST: ${RED}${ip}${NO_COLOR}\n"
   printf "USER: ${RED}postgres${NO_COLOR}\n"
   printf "PASS: ${RED}[VOID: No password needed]${NO_COLOR}\n\n"
   printf "To login into pgAdmin:\n"
   printf "USER: ${RED}megaparty@stratio.com${NO_COLOR}\n"
   printf "PASS: ${RED}lalala${NO_COLOR}\n\n"
else
   printf "\nDo you want to start a pgAdmin Docker image?: [y/n]\n"
   read pgAdminStart
   if [ "$pgAdminStart" == "y" ]; then
      pgAdmin="$(docker ps -a -q -f name=pgadmin)"
      if [ "$pgAdmin" == "" ]; then
         docker run --name=pgadmin -p 80:80 -e "PGADMIN_DEFAULT_EMAIL=megaparty@stratio.com" -e "PGADMIN_DEFAULT_PASSWORD=lalala" -d dpage/pgadmin4
         printf "\n${OWNER}${GREEN}Docker pgAdmin created and started. Running on port 80.${NO_COLOR}\n"
      else
         docker start ${pgAdmin}
         printf "\n${OWNER}${GREEN}Docker pgAdmin started. Running on port 80.${NO_COLOR}\n"
      fi
      printf "To configure pgAdmin to connect database you will need Postgres internal container IP:\n"
      printf "HOST: ${RED}${ip}${NO_COLOR}\n"
      printf "USER: ${RED}postgres${NO_COLOR}\n"
      printf "PASS: ${RED}[VOID: No password needed]${NO_COLOR}\n\n"
      printf "To login into pgAdmin:\n"
      printf "USER: ${RED}megaparty@stratio.com${NO_COLOR}\n"
      printf "PASS: ${RED}lalala${NO_COLOR}\n\n"
   fi
fi

###### ZOOKEEPER ######

zooPort=""
zooServer=""
zooConfigNumberOfFiles="$(locate -c *zoo.cfg)"
if [ "$zooConfigNumberOfFiles" == 1 ]; then
   zooConfiFile="$(locate *zoo.cfg)"
   zooPort="$(cat ${zooConfiFile} | grep clientPort= | grep -Eo '[0-9]{4}')"
   if [ "$zooPort" != "" ]; then
      zooPortUp="$(checkPortFast ${zooPort})"
      if [ "$zooPortUp" == "" ]; then
         zooMainPath="$(cut -d/ -f2 <<< ${zooConfiFile})"
         zooServerNumberOfFiles="$(locate -r /bin/zkServer.sh$ | grep -c ^/${zooMainPath}/)"
         if [ "$zooServerNumberOfFiles" == 1 ]; then
            zooServer="$(locate */bin/zkServer.sh | grep ^/${zooMainPath}/)"
            ${zooServer} start
         else
            printf "${OWNER}${YELLOW}It is not possible to determine the start script of Zookeeper server (zkServer.sh).${NO_COLOR}\n"
            printf "${OWNER}${YELLOW}Cannot to start Zookeeper automatically.${NO_COLOR}\n"
            printf "${OWNER}${YELLOW}You will need to start it manually.${NO_COLOR}\n"
         fi
      else
         printf "\n${OWNER}${GREEN}Zookeeper started. Running on port ${zooPort}.${NO_COLOR}\n"
         zooServer="OK"
      fi
   else
      printf "${OWNER}${YELLOW}No port defined into Zookeeper config file (zoo.cfg).${NO_COLOR}\n"
      printf "${OWNER}${YELLOW}Impossible to start Zookeeper automatically.${NO_COLOR}\n"
      printf "${OWNER}${YELLOW}You will need to start it manually.${NO_COLOR}\n"
   fi
elif [ "$zooConfigNumberOfFiles" -gt 1 ]; then
   printf "${OWNER}${YELLOW}Found more than one Zookeeper config file (zoo.cfg).${NO_COLOR}\n"
   printf "${OWNER}${YELLOW}Impossible to start Zookeeper automatically.${NO_COLOR}\n"
   printf "${OWNER}${YELLOW}You will need to start it manually.${NO_COLOR}\n"
else
   printf "${OWNER}${YELLOW}Not found Zookeeper config file (zoo.cfg).${NO_COLOR}\n"
   printf "${OWNER}${YELLOW}You need to configure Zookeeper.${NO_COLOR}\n"
fi

if [ "$zooPort" != "" ] && [ "$zooServer" != "" ]; then
   portZookeeper="$(checkPort ${zooPort})"
   if [ "$portZookeeper" != "" ]; then
      printf "\n${OWNER}${GREEN}Zookeeper is ready to work.${NO_COLOR}\n"
      printf "${OWNER}${GREEN}$portZookeeper${NO_COLOR}\n\n"
   else
      printf "\n${OWNER}${YELLOW}Unable to check if Zookeeper is ready.${NO_COLOR}\n\n"
   fi
fi

###### SPARTA SERVING API ######

printf "\nDo you need to do Maven cleaning?: [y/n]\n"
read mavenClean
if [ "$mavenClean" == "y" ]; then
   __dirname="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
   mainPath="${__dirname}/../../../"
   mvn -f ${mainPath} clean install -DskipUTs -DskipITs -DskipTest
fi

portApiUp="$(checkPortFast ${SERVING_API_PORT})"
if [ "$portApiUp" == "" ]; then
   __dirname="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
   servingApiPath="${__dirname}/../../../serving-api/"
   ./tools/launcher/check-serving-api.sh &
   mvn -f ${servingApiPath} exec:java -Dexec.mainClass="com.stratio.sparta.serving.api.Sparta" -DSPARTA_MIGRATION_CASSIOPEIA=false
else
   printf "\n${OWNER}${GREEN}Serving API started. Running on port ${SERVING_API_PORT}.${NO_COLOR}\n"
   portApi="$(checkPort ${SERVING_API_PORT})"
   if [ "$portApi" != "" ]; then
      printf "\n${OWNER}${GREEN}Serving API is ready to work.${NO_COLOR}\n"
      printf "${OWNER}${GREEN}$portApi${NO_COLOR}\n\n"
   else
      printf "\n${OWNER}${YELLOW}Unable to check if Zookeeper is ready.${NO_COLOR}\n\n"
   fi
fi
