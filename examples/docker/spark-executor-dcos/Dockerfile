# Stratio
#
# VERSION       0.1.0

FROM qa.stratio.com/stratio/spark-krb-dispatcher-support:2.1.0
MAINTAINER Sparta Team "sparta@stratio.com"

RUN apt-get update -y && apt-get upgrade -y && apt-get install -y curl wget vim jq libsvn1 libcurl4-nss-dev default-jre-headless && update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java

ENV SPARK_HOME=/opt/spark/dist

WORKDIR /opt/spark/dist
