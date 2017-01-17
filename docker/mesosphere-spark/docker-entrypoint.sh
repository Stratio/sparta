#!/bin/bash

set -e

export MESOS_WORK_DIR="/tmp"
export MESOS_QUORUM="1"

MESOS_MODE=${MODE:=master}
MESOS_VARIABLES=/etc/sds/mesos/mesos-variables-env.sh

mkdir -p /var/log/sds/mesos

sed -i "s|export SPARK_HOME.*||" ${MESOS_VARIABLES}
sed -i "s|export SPARTA_HOME.*||" ${MESOS_VARIABLES}

nohup /opt/sds/mesos/sbin/mesos-${MESOS_MODE} > /var/log/sds/mesos/mesos.log &

if [[ ${MESOS_MODE} == "master" ]]; then
  echo "CONF DISPATCHER"
  sleep 5
  cd /spark-1.6.2-bin-2.6.0/sbin
  ./start-mesos-dispatcher.sh --master mesos://`hostname`:5050
fi

###############
# HDFS CONFIG #
###############

if [[ ! -z ${HDFS_MANAGER} ]]; then
	wget http://prerepository.stratio.com/DEV/1.6/ubuntu/13.10/binary/stratio-hadoop_2.7.2_amd64.deb
	apt-get update
	apt-get install -y openjdk-8-jdk
	echo "export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64" >> /etc/profile
	apt-get install libsnappy1
	dpkg -i stratio-hadoop_2.7.2_amd64.deb
	rm -rf stratio-hadoop_2.7.2_amd64.deb
	mkdir -p /tmp/data/hdfs
	chmod -R 775 /tmp/data/hdfs
	mkdir -p /tmp/data/hdfs/namenode
	mkdir -p /tmp/data/hdfs/datanode
	
	echo "<configuration> <property> <name>hadoop.tmp.dir</name> <value>/tmp</value> </property> <property> <name>fs.defaultFS</name> <value>hdfs://"$HDFS_MANAGER":8020</value> </property> </configuration>" > /opt/sds/hadoop/conf/core-site.xml
	echo "<configuration> <property> <name>dfs.namenode.name.dir</name> <value>/tmp/data/hdfs/namenode</value> </property> <property> <name>dfs.datanode.name.dir</name> <value>/tmp/data/hdfs/datanode</value> </property> <property><name>dfs.permissions.enabled</name><value>false</value></property><property> <name>dfs.replication</name> <value>1</value> </property> </configuration>" > /opt/sds/hadoop/conf/hdfs-site.xml

	nohup /opt/sds/hadoop/sbin/hadoop-daemon.sh --config /opt/sds/hadoop/conf start datanode > /var/log/sds/hadoop-hdfs/hdfs-datanode.log &
fi

###############

tail -F /var/log/sds/mesos/mesos.log
