
Entrypoint variables
=====================

## SPARTA JAVA OPTIONS
| PARAM        | 
| -------------:|
| SPARTA_HEAP_SIZE |
| SPARTA_HEAP_MINIMUM_SIZE |
| SPARTA_MAX_PERM_SIZE |
| SPARTA_CONFIG_JAAS_FILE |


## SPARTA GENERIC OPTIONS

| PARAM        | 
| -------------:|
| MAX_OPEN_FILES |


## HDFS OPTIONS
 
| PARAM        |
| -------------:|
| HADOOP_USER_NAME |
| HADOOP_CONF_FROM_URI |
| CORE_SITE_FROM_URI |
| HADOOP_CONF_FROM_DFS |
| HADOOP_CONF_FROM_DFS_NOT_SECURED |
| HADOOP_CONF_URI |
| HADOOP_FS_DEFAULT_NAME |
| HADOOP_SECURITY_AUTH |
| HADOOP_RPC_PROTECTION |
| HADOOP_DFS_ENCRYPT_DATA_TRANSFER |
| HADOOP_SECURITY_TOKEN_USE_IP |
| HADOOP_NAMENODE_KRB_PRINCIPAL |
| HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN |
| HADOOP_DFS_ENCRYPT_DATA_TRANSFER |
| HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES |
| HADOOP_SECURITY_AUTH |
| HADOOP_PORT |
| HDFS_KEYTAB_RELOAD |
| HDFS_KEYTAB_RELOAD_TIME |

## KERBEROS OPTIONS

| PARAM        |
| -------------:|
| SPARTA_PRINCIPAL_NAME |
| SPARTA_KEYTAB_PATH |


## SPARTA LOG LEVEL OPTIONS

| PARAM        |
| -------------:|
| SERVICE_LOG_LEVEL |
| SPARTA_LOG_LEVEL |
| SPARK_LOG_LEVEL |
| HADOOP_LOG_LEVEL |
| AVRO_NETTY_LOG_LEVEL |
| MORTBAY_LOG_LEVEL |
| JBOSS_LOG_LEVEL |


## SPARTA API OPTIONS
 
| PARAM        |
| -------------:|
| SPARTA_API_HOST |
| PORT_SPARTAAPI |
| SPARTA_TLS_KEYSTORE_LOCATION |
| SPARTA_TLS_KEYSTORE_PASSWORD |


## SPARTA ZOOKEEPER OPTIONS

| PARAM        |
| -------------:|
| SPARTA_ZOOKEEPER_CONNECTION_STRING |
| SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT |
| SPARTA_ZOOKEEPER_SESSION_TIMEOUT |
| SPARTA_ZOOKEEPER_RETRY_ATEMPTS |
| SPARTA_ZOOKEEPER_RETRY_INTERVAL |


## SPARTA AKKA OPTIONS
| PARAM        |
| -------------:|
| SPARTA_AKKA_CONTROLLER_INSTANCES |


## SPARTA CONFIGURATION OPTIONS
| PARAM        |
| -------------:|
| SPARTA_DRIVER_PACKAGE_LOCATION |
| SPARTA_PLUGIN_PACKAGE_LOCATION |
| SPARTA_DRIVER_URI |
| SPARTA_AWAIT_POLICY_CHANGE_STATUS |
| SPARTA_REMEMBER_PARTITIONER |
| SPARTA_CHECKPOINT_PATH |
| SPARTA_AUTO_DELETE_CHECKPOINT |
| SPARTA_ADD_TIME_TO_CHECKPOINT |


## MARATHON OPTIONS
| PARAM        |
| -------------:|
| SPARTA_MARATHON_JAR |
| SPARTA_MARATHON_TEMPLATE_FILE |
| SPARTA_MARATHON_MESOSPHERE_PACKAGES |
| SPARTA_MARATHON_MESOSPHERE_LIB |
| MARATHON_SSO_URI |
| MARATHON_SSO_USERNAME |
| MARATHON_SSO_PASSWORD |
| MARATHON_SSO_CLIENT_ID |
| MARATHON_SSO_REDIRECT_URI |
| MARATHON_TIKI_TAKKA_MARATHON_URI |
| MARATHON_TIKI_TAKKA_MARATHON_API_VERSION |


## OAUTH2 OPTIONS
| PARAM        |
| -------------:|
| OAUTH2_ENABLE |
| OAUTH2_COOKIE_NAME |
| OAUTH2_SSL_AUTHORIZE |
| OAUTH2_URL_ACCESS_TOKEN |
| OAUTH2_URL_PROFILE |
| OAUTH2_URL_LOGOUT |
| OAUTH2_URL_CALLBACK |
| OAUTH2_URL_ON_LOGIN_GO_TO |
| OAUTH2_CLIENT_ID |
| OAUTH2_CLIENT_SECRET |


## SPRAY OPTIONS
| PARAM        |
| -------------:| 
| SPRAY_CAN_SERVER_SSL_ENCRYPTION |


## EXECUTION OPTIONS
| PARAM        |
| -------------:|
|RUN_MODE |


Usage examples
===============

- Driver JAR provided by Sparta API:

```bash
docker run -dit --name sp -p 9090:9090 --env RUN_MODE=debug --env SERVICE_LOG_LEVEL=INFO 
   --env SPARTA_LOG_LEVEL=INFO --env SPARK_LOG_LEVEL=INFO 
   --env SPARTA_DRIVER_URI=http://sp.demo.stratio.com:9090/driver/sparta-driver.jar 
   --env SPARTA_ZOOKEEPER_CONNECTION_STRING=zk.demo.stratio.com 
   --env SPARTA_CHECKPOINT_PATH=/user/stratio/checkpoint 
   --env SPARK_MESOS_MASTER=mesos://mm11.demo.stratio.com:7077 qa.stratio.com/stratio/sparta:latest
```

- Driver JAR uploaded to HDFS: 

```bash
docker run -dit --name sp -p 9090:9090 --env RUN_MODE=debug --env SERVICE_LOG_LEVEL=INFO 
   --env SPARTA_LOG_LEVEL=INFO --env SPARK_LOG_LEVEL=INFO 
   --env SPARTA_ZOOKEEPER_CONNECTION_STRING=zk.demo.stratio.com --env SPARTA_EXECUTION_MODE=mesos 
   --env SPARTA_CHECKPOINT_PATH=/user/stratio/checkpoint 
   --env HDFS_MASTER=hm.demo.stratio.com 
   --env HADOOP_PORT=8020 qa.stratio.com/stratio/sparta:latest
```
   
Usage examples with VAULT 
===============

For vault to work you must have a vault with the secrets of sparta.

The parameters involved are :

| PARAM        | Description|
| -------------|:-------------:|
| VAULT_HOST   | The host of the vault installation.If not present the docker image will not attempt to download any secrets |
| VAULT_TOKEN  | The vault token|

In the integration.env file there is an example for a vault configuration.
In the example we are mapping an ip to a host (like adding an entry to /etc/hosts )

```bash

docker run -d 
    --env-file example/integration.env 
    -p 9090:9090 -p 4040:4040 
    --add-host gosec2.labs.stratio.com:10.1.1.1 
    --name sparta qa.stratio.com/stratio/sparta:latest

```

Weave commands
===============

```bash
weave launch-router --dns-domain="demo.stratio.com." --init-peer-count 1 && weave launch-proxy -H tcp://0.0.0.0:12375 --hostname-from-label hostname --no-detect-tls
weave expose
eval $(weave env)
weave status dns
```

Running dependant Dockers
==========================

### ZOOKEEPER ###
```bash
docker run -dit --name zk qa.stratio.com/stratio/zookeeper:3.4.6
```
### HADOOP ###
```bash
docker run -dit --name hm --env NAMENODE_MODE=true qa.stratio.com/stratio/hadoop:2.7.2
```
### MESOS-SPARK 1.6.2 SCALA 2.11 (Deprecated) ###

  - Dockerfile:

  Build from command -> docker build -f Dockerfile -t mesos-spark-1.6.2-scala-2.11 .

  - MASTER: 
    ```bash    
        docker run -dit --volumes-from hm --name mm11 --env MODE=master \
          --env MESOS_ZK=zk://zk.demo.stratio.com:2181/mesos11 --env JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 \
          --env HADOOP_HOME=/opt/sds/hadoop --env HADOOP_USER_NAME=stratio  \
          --env HADOOP_CONF_DIR=/opt/sds/hadoop/conf mesos-spark-1.6.2-scala-2.11
    ```

  - SLAVE: 
    ```bash    
        docker run -dit --volumes-from hm --name ms11 --env MODE=slave \
                --env MESOS_MASTER=zk://zk.demo.stratio.com:2181/mesos11 --env JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 \
                --env HADOOP_HOME=/opt/sds/hadoop --env HADOOP_USER_NAME=stratio \
                --env HADOOP_CONF_DIR=/opt/sds/hadoop/conf mesos-spark-1.6.2-scala-2.11
          
    ```
