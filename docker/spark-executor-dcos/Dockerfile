# Stratio
#
# VERSION       0.1.0

FROM qa.stratio.com/stratio/mesosphere-spark-scala211:1.6.2
MAINTAINER Sparta Team "sparta@stratio.com"

RUN apt-get update && apt-get install -y curl jq libsvn1 libcurl4-nss-dev default-jre-headless

#ENV MESOS_NATIVE_JAVA_LIBRARY /opt/mesosphere/lib/libmesos.so

WORKDIR /opt/sds/spark
