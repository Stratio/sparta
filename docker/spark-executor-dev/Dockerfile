# Stratio
#
# VERSION       0.1.0

FROM qa.stratio.com/stratio/spark-krb-dispatcher-support:2.1.0
MAINTAINER Sparta Team "sparta@stratio.com"

# MESOS LIBRARY 0.22.1
#RUN mkdir -p /opt/mesosphere/lib
#COPY libmesos.so /opt/mesosphere/lib/libmesos.so

# If save checkpoint in HDFS
#COPY core-site.xml /etc/sds/hadoop/conf/core-site.xml
#ENV HADOOP_CONF_DIR /etc/sds/hadoop/conf
#ENV HADOOP_USER_NAME root

RUN apt-get update && apt-get install -y curl wget vim jq libsvn1 libcurl4-nss-dev default-jre-headless

#ENV MESOS_NATIVE_JAVA_LIBRARY /opt/mesosphere/lib/libmesos.so

WORKDIR /opt/spark/dist
