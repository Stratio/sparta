
Entrypoint variables
=====================

 # SPARTA JAVA OPTIONS

SPARTA_HEAP_SIZE
SPARTA_HEAP_MINIMUM_SIZE
SPARTA_MAX_PERM_SIZE


 # SPARTA JMX OPTIONS
 
SPARTA_JMX_PORT
SPARTA_JMX_AUTHENTICATE
SPARTA_JMX_SSL
 
 
 # SPARTA GENERIC OPTIONS
  
MAX_OPEN_FILES


 # HDFS OPTIONS
 
HDFS_USER_NAME
HDFS_MASTER
HDFS_PORT
HDFS_PRINCIPAL_NAME
HDFS_PRINCIPAL_NAME_SUFFIX
HDFS_PRINCIPAL_NAME_PREFIX
HDFS_KEYTAB
HDFS_KEYTAB_RELOAD


 # SPARTA LOG LEVEL OPTIONS
 
SERVICE_LOG_LEVEL
SPARTA_LOG_LEVEL
SPARK_LOG_LEVEL
HADOOP_LOG_LEVEL
AVRO_NETTY_LOG_LEVEL
MORTBAY_LOG_LEVEL
JBOSS_LOG_LEVEL


 # SPARTA API OPTIONS
 
SPARTA_API_HOST
SPARTA_API_PORT
SPARTA_API_CERTIFICATE_FILE
SPARTA_API_CERTIFICATE_PASSWORD


 # SPARTA SWAGGER OPTIONS
 
SPARTA_SWAGGER_HOST
SPARTA_SWAGGER_PORT


 # SPARTA ZOOKEEPER OPTIONS
 
SPARTA_ZOOKEEPER_CONNECTION_STRING
SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT
SPARTA_ZOOKEEPER_SESSION_TIMEOUT
SPARTA_ZOOKEEPER_RETRY_ATEMPTS
SPARTA_ZOOKEEPER_RETRY_INTERVAL


 # SPARTA AKKA OPTIONS
 
SPARTA_AKKA_CONTROLLER_INSTANCES


 # SPARTA CONFIGURATION OPTIONS
 
SPARTA_EXECUTION_MODE
SPARTA_DRIVER_PACKAGE_LOCATION
SPARTA_PLUGIN_PACKAGE_LOCATION
SPARTA_DRIVER_URI
SPARTA_STOP_GRACEFULLY
SPARTA_AWAIT_POLICY_CHANGE_STATUS
SPARTA_REMEMBER_PARTITIONER
SPARTA_CHECKPOINT_PATH
SPARTA_AUTO_DELETE_CHECKPOINT


 # LOCAL EXECUTION OPTIONS
 
SPARK_LOCAL_MASTER
SPARK_LOCAL_APP_NAME
SPARK_LOCAL_DRIVER_MEMORY
SPARK_LOCAL_DRIVER_CORES
SPARK_LOCAL_EXECUTOR_MEMORY
SPARK_LOCAL_CONCURRENT_JOBS
SPARK_LOCAL_GRACEFUL_STOP
SPARK_LOCAL_METRICS_CONF
SPARK_LOCAL_SERIALIZER
SPARK_LOCAL_PARQUET_BINARY_AS_STRING


 # MESOS EXECUTION OPTIONS

SPARK_MESOS_HOME
SPARK_MESOS_COARSE
SPARK_MESOS_DEPLOY
SPARK_MESOS_MASTER
SPARK_MESOS_KILL_URL
SPARK_MESOS_JARS
SPARK_MESOS_PACKAGES
SPARK_MESOS_EXCLUDE_PACKAGES
SPARK_MESOS_REPOSITORIES
SPARK_MESOS_PROXY_USER
SPARK_MESOS_DRIVER_JAVA_OPTIONS
SPARK_MESOS_DRIVER_LIBRARY_PATH
SPARK_MESOS_DRIVER_CLASSPATH
SPARK_MESOS_EXECUTOR_MEMORY
SPARK_MESOS_TOTAL_EXECUTOR_CORES
SPARK_MESOS_EXECUTOR_CORES
SPARK_MESOS_EXTRA_CORES
SPARK_MESOS_DRIVER_CORES
SPARK_MESOS_DRIVER_MEMORY
SPARK_MESOS_SUPERVISE
SPARK_MESOS_CONCURRENT_JOBS
SPARK_MESOS_GRACEFUL_STOP
SPARK_MESOS_METRICS_CONF
SPARK_MESOS_SERIALIZER
SPARK_MESOS_PARQUET_BINARY_AS_STRING
SPARK_MESOS_PROPERTIES_FILE
SPARK_MESOS_EXECUTOR_DOCKER_IMAGE
SPARK_MESOS_EXECUTOR_FORCE_PULL_IMAGE
SPARK_MESOS_EXECUTOR_HOME
SPARK_MESOS_EXECUTOR_URI
SPARK_MESOS_DRIVER_EXTRA_CLASSPATH
SPARK_MESOS_DRIVER_EXTRA_JAVA_OPTIONS
SPARK_MESOS_DRIVER_EXTRA_LIBRARY_PATH
SPARK_MESOS_SPARK_JARS
SPARK_MESOS_SPARK_FILES
SPARK_MESOS_SPARK_JARS_IVY


 # OAUTH2 OPTIONS

OAUTH2_ENABLE
OAUTH2_COOKIE_NAME
OAUTH2_SSL_AUTHORIZE
OAUTH2_URL_ACCESS_TOKEN
OAUTH2_URL_PROFILE
OAUTH2_URL_LOGOUT
OAUTH2_URL_CALLBACK
OAUTH2_URL_ON_LOGIN_GO_TO
OAUTH2_CLIENT_ID
OAUTH2_CLIENT_SECRET

 # AKKA OPTIONS
 
AKKA_LOG_DEAD_LETTERS


 # SPRAY OPTIONS
 
SPRAY_CAN_SERVER_SSL_ENCRYPTION


 # EXECUTION OPTIONS
 
RUN_MODE


Usage examples
===============

- Driver JAR provided by Sparta API:

docker run -dit --name sp -p 9090:9090 -p 9091:9091 --env RUN_MODE=debug --env SERVICE_LOG_LEVEL=INFO 
   --env SPARTA_LOG_LEVEL=INFO --env SPARK_LOG_LEVEL=INFO 
   --env SPARTA_DRIVER_URI=http://sp.demo.stratio.com:9090/drivers/driver-plugin.jar 
   --env SPARTA_DRIVER_LOCATION=provided --env SPARTA_ZOOKEEPER_CONNECTION_STRING=zk.demo.stratio.com 
   --env SPARTA_EXECUTION_MODE=mesos --env SPARTA_CHECKPOINT_PATH=/user/stratio/checkpoint 
   --env SPARK_MESOS_MASTER=mesos://mm11.demo.stratio.com:7077 qa.stratio.com:8443/stratio/sparta:latest
  

- Driver JAR uploaded to HDFS: 

docker run -dit --name sp -p 9090:9090 -p 9091:9091 --env RUN_MODE=debug --env SERVICE_LOG_LEVEL=INFO 
   --env SPARTA_LOG_LEVEL=INFO --env SPARK_LOG_LEVEL=INFO --env SPARTA_DRIVER_LOCATION=hdfs 
   --env SPARTA_ZOOKEEPER_CONNECTION_STRING=zk.demo.stratio.com --env SPARTA_EXECUTION_MODE=mesos 
   --env SPARTA_CHECKPOINT_PATH=/user/stratio/checkpoint 
   --env SPARK_MESOS_MASTER=mesos://mm11.demo.stratio.com:7077 --env HDFS_MASTER=hm.demo.stratio.com 
   --env HDFS_PORT=8020 qa.stratio.com:8443/stratio/sparta:latest
   
   
Usage examples with VAULT 
===============

For vault to work you must have a vault with the secrets of sparta.

The parameters involved are :

| PARAM        | Description|
| -------------|:-------------:|
| VAULT_HOST   | The host of the vault installation.If not present the docker image will not attempt to download any secrets |
| VAULT_TOKEN  | The vault token|
| VAULT_PORT   | The vault port      |

In the integration.env file there is an example for a vault configuration.
In the example we are mapping an ip to a host (like adding an entry to /etc/hosts )

```bash

docker run -d 
    --env-file integration.env 
    -p 9090:9090 -p 9091:9091 -p 4040:4040 
    --add-host gosec2.labs.stratio.com:10.1.1.1 
    --name sparta qa.stratio.com:8443/stratio/sparta:latest

```

Weave commands
===============

weave launch-router --dns-domain="demo.stratio.com." --init-peer-count 1 && weave launch-proxy -H tcp://0.0.0.0:12375 --hostname-from-label hostname --no-detect-tls
weave expose
eval $(weave env)
weave status dns


Running dependant Dockers
==========================

### ZOOKEEPER ###

docker run -dit --name zk qa.stratio.com/stratio/zookeeper:3.4.6

### HADOOP ###

docker run -dit --name hm --env NAMENODE_MODE=true qa.stratio.com/stratio/hadoop:2.7.2

### MESOS-SPARK 1.6.2 SCALA 2.11 ###

  - Dockerfile:
  
      Build from command -> docker build -f Dockerfile -t mesos-spark-1.6.2-scala-2.11 .
      
  - MASTER: 
    
    docker run -dit --volumes-from hm --name mm11 --env MODE=master \
      --env MESOS_ZK=zk://zk.demo.stratio.com:2181/mesos11 --env JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 \
      --env HADOOP_HOME=/opt/sds/hadoop --env HADOOP_USER_NAME=stratio  \
      --env HADOOP_CONF_DIR=/opt/sds/hadoop/conf mesos-spark-1.6.2-scala-2.11

  - SLAVE:
  
    docker run -dit --volumes-from hm --name ms11 --env MODE=slave \
        --env MESOS_MASTER=zk://zk.demo.stratio.com:2181/mesos11 --env JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 \
        --env HADOOP_HOME=/opt/sds/hadoop --env HADOOP_USER_NAME=stratio \
        --env HADOOP_CONF_DIR=/opt/sds/hadoop/conf mesos-spark-1.6.2-scala-2.11
