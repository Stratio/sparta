# Stratio
#
# VERSION       0.1.0

FROM qa.stratio.com/stratio/ubuntu-base:15.04
MAINTAINER Alicia Doblas "adoblas@stratio.com"

COPY docker-entrypoint.sh /docker-entrypoint.sh

ADD http://tools.stratio.com/mesos/mesos-0.22.1/stratio-mesos_0.22.1_amd64.deb .
ADD http://tools.stratio.com/spark/spark-mesosphere-scala211-1.6.2-bin-hadoop2.6.0.tgz .
RUN apt-get update && apt-get install -y --no-install-recommends apt-utils \
    && apt-get install -y libcurl3  libsvn1 libcurl4-nss-dev supervisor \
    && dpkg -i stratio-mesos_0.22.1_amd64.deb \
    && rm -rf stratio-mesos_0.22.1_amd64.deb \
    && rm -rf /var/lib/apt/lists/* \
    && tar xvfm spark-mesosphere-scala211-1.6.2-bin-hadoop2.6.0.tgz
    && rm -Rf spark-mesosphere-scala211-1.6.2-bin-hadoop2.6.0.tgz

RUN chmod a+x /docker-entrypoint.sh

EXPOSE 5050 7070

ENTRYPOINT [ "/docker-entrypoint.sh" ]
