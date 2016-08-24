#!/bin/bash -xe
 SPARTA_CONF_FILE=/etc/sds/sparta/application.conf
  if [[ ! -v SPARK_VERSION ]]; then
   SPARK_VERSION=spark-1.6.2
 fi
 if [[ ! -v HADOOP_SPARK_VERSION ]]; then
   HADOOP_SPARK_VERSION=hadoop2.6
 fi
 if [[ ! -v HADOOP_VERSION ]]; then
   HADOOP_VERSION=hadoop-2.7.1
 fi
 if [[ ! -v EXECUTION_MODE ]]; then
   EXECUTION_MODE=local
 fi
 if [[ ! -v ZOOKEEPER_HOST ]]; then
   ZOOKEEPER_HOST=localhost:2181
 fi
 if [[ ! -v HDFS_MASTER ]]; then
   HDFS_MASTER=localhost
 fi
  if [[ ! -v HDFS_PORT ]]; then
   HDFS_PORT=8020
 fi
   if [[ ! -v SPARK_MASTER ]]; then
   SPARK_MASTER=local[*]:7077
 fi
   if [[ ! -v MESOS_MASTER ]]; then
   MESOS_MASTER=localhost:7077
 fi
 sed -i "s|executionMode.*|executionMode = \"${EXECUTION_MODE}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|connectionString.*|connectionString = \""${ZOOKEEPER_HOST}"\"|" ${SPARTA_CONF_FILE}
 sed -i "s|hdfsMaster.*|hdfsMaster = \"${HDFS_MASTER}\"|" ${SPARTA_CONF_FILE}
 sed -i "s|hdfsPort.*|hdfsPort = \"${HDFS_PORT}\"|" ${SPARTA_CONF_FILE}
 if [[ ! -v EXECUTION_MODE || "${EXECUTION_MODE}" == "local" ]]; then
   sed -i "s|spark.master.*|spark.master = \""${SPARK_MASTER:=local[*]}"\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${EXECUTION_MODE}" == "mesos" || "${EXECUTION_MODE}" == "yarn" || "${EXECUTION_MODE}" == "standalone" ]]; then
   SPARK_HADOOP_VERSION_FILE="${SPARK_VERSION}-bin-${HADOOP_SPARK_VERSION}.tgz"
   HADOOP_VERSION_FILE="${HADOOP_VERSION}.tar.gz"
   CORE_SITE=core-site.xml
   HDFS_SITE=hdfs-site.xml
   YARN_SITE=yarn-site.xml.xml
   MAPRED_SITE=mapred-site.xml
   SPARK_HOME="/${SPARK_VERSION}-bin-${HADOOP_SPARK_VERSION}"
   SPARTA_VARIABLES=/etc/default/sparta-variables
   wget "http://www.apache.org/dyn/closer.lua/spark/${SPARK_VERSION}/${SPARK_HADOOP_VERSION_FILE}"
   tar xvf ${SPARK_HADOOP_VERSION_FILE}
   rm ${SPARK_HADOOP_VERSION_FILE}
   wget "http://www.eu.apache.org/dist/hadoop/common/${HADOOP_VERSION}/${HADOOP_VERSION_FILE}"
   tar xvf ${HADOOP_VERSION_FILE}
   rm ${HADOOP_VERSION_FILE}
   mkdir "/${HADOOP_VERSION}/conf"
   wget "http://${HDFS_MASTER}:50070/conf/${CORE_SITE}"
   wget "http://${HDFS_MASTER}:50070/conf/${HDFS_SITE}"
   wget "http://${HDFS_MASTER}:50070/conf/${YARN_SITE}"
   wget "http://${HDFS_MASTER}:50070/conf/${MAPRED_SITE}"
   mv CORE_SITE "/${HADOOP_VERSION}/conf"
   mv HDFS_SITE "/${HADOOP_VERSION}/conf"
   mv YARN_SITE "/${HADOOP_VERSION}/conf"
   mv MAPRED_SITE "/${HADOOP_VERSION}/conf"
   echo "" >> ${SPARTA_VARIABLES}
   echo "export HADOOP_HOME="/${HADOOP_VERSION}"" >> ${SPARTA_VARIABLES}
   echo "export HADOOP_USER_NAME="stratio"" >> ${SPARTA_VARIABLES}
   echo "export HADOOP_CONF_DIR="/${HADOOP_VERSION}/conf"" >> ${SPARTA_VARIABLES}
   echo "export SPARK_HOME="${SPARK_HOME}"" >> ${SPARTA_VARIABLES}
 fi
 if [[ "${EXECUTION_MODE}" == "mesos" ]]; then
   sed -i "s|sparkHome.*|sparkHome = \""${SPARK_HOME}"\"|" ${SPARTA_CONF_FILE}
   sed -i "s|master = \"mesos.*|master = \""mesos://${MESOS_MASTER}"\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${EXECUTION_MODE}" == "yarn" ]]; then
   sed -i "s|sparkHome.*|sparkHome = \""${SPARK_HOME}"\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${EXECUTION_MODE}" == "standalone" ]]; then
   sed -i "s|sparkHome.*|sparkHome = \""${SPARK_HOME}"\"|" ${SPARTA_CONF_FILE}
   sed -i "s|master = \"spark.*|master = \""spark://${SPARK_MASTER}"\"|" ${SPARTA_CONF_FILE}
 fi
 if [[ "${SSH}" == "true" ]]; then
   /usr/sbin/sshd -e
 fi
 /etc/init.d/sparta start
 tail -F /var/log/sds/sparta/sparta.log
