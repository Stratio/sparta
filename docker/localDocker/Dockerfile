# Stratio distribution Dockerfile
#
# VERSION       0.3.0

FROM qa.stratio.com/stratio/mesosphere-spark-scala211:1.6.2

EXPOSE 9090 9091
ENTRYPOINT ["/docker-entrypoint.sh"]
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64

COPY . /

RUN wget "http://apt.repository.stratio.com/pool/trusty/1.7/main/stratio-release_1.0.0_all.deb"  \
    && dpkg -i stratio-release_1.0.0_all.deb \
    && rm -rf stratio-release_1.0.0_all.deb \
    && apt-get update \
    && /dependencyfix.sh /PKG.deb\
    && dpkg -i /PKG.deb \
    && rm -rf /PKG.deb

