# Stratio distribution Dockerfile
#
# VERSION       0.3.0

FROM qa.stratio.com/stratio/spark-krb-dispatcher-support:2.1.0

EXPOSE 9090
ENTRYPOINT ["/docker-entrypoint.sh"]
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64

COPY . /

RUN apt-get update -y \
    && apt-get upgrade -y \
    && update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
    && apt-get install -y wget \
    && wget "http://apt.repository.stratio.com/pool/trusty/1.7/main/stratio-release_1.0.0_all.deb"  \
    && dpkg -i stratio-release_1.0.0_all.deb \
    && rm -rf stratio-release_1.0.0_all.deb \
    && apt-get update \
    && /dependencyfix.sh /PKG.deb\
    && dpkg -i /PKG.deb \
    && rm -rf /PKG.deb

