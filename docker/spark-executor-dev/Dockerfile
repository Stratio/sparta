# Stratio
#
# VERSION       0.1.0

FROM stratio/mesosphere-spark-scala211:1.6.2
MAINTAINER Sparta Team "sparta@stratio.com"

# MESOS LIBRARY 0.22.1
COPY libmesos.so /usr/lib/libmesos.so

# If save checkpoint in HDFS
#COPY core-site.xml /etc/sds/hadoop/conf/core-site.xml
#ENV HADOOP_CONF_DIR /etc/sds/hadoop/conf
#ENV HADOOP_USER_NAME root

RUN apt-get update && apt-get install -y curl jq libsvn1 libcurl4-nss-dev default-jre-headless

ENV MESOS_NATIVE_JAVA_LIBRARY /usr/lib/libmesos.so

WORKDIR /opt/sds/spark
