# Stratio
#
# VERSION       0.1.0

FROM qa.stratio.com/stratio/spark-krb-dispatcher-support:2.1.0
MAINTAINER Sparta Team "sparta@stratio.com"

WORKDIR /

COPY docker-entrypoint.sh /docker-entrypoint.sh
COPY stratio-mesos.deb /stratio-mesos.deb
#ADD http://tools.stratio.com/mesos/mesos-0.22.1/stratio-mesos_0.22.1_amd64.deb .

RUN apt-get update -y && apt-get install -y curl wget vim jq libsvn1 libcurl4-nss-dev default-jre-headless docker \
 	&& rm -r /usr/lib/libmesos.so \
 	&& dpkg -i /stratio-mesos.deb \
# 	&& dpkg -i /stratio-mesos_0.22.1_amd64.deb \
    && rm -rf /stratio-mesos.deb \
#   && rm -rf /stratio-mesos_0.22.1_amd64.deb \
    && rm -rf /var/lib/apt/lists/* \
    && update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java


ENV SPARK_HOME=/opt/spark/dist

RUN chmod a+x /docker-entrypoint.sh

EXPOSE 5050 7070 8081 7077

ENTRYPOINT [ "/docker-entrypoint.sh" ]
