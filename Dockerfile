FROM qa.stratio.com/stratio/spark-stratio-driver:2.2.0-2.5.1-6bc6f53
MAINTAINER Stratio "jenkins@stratio.com"

COPY docker/* /
COPY ./dist/target/sparta*.all.deb /

EXPOSE 9090 10000 11000 7777
ADD http://sodio.stratio.com/repository/paas/kms_utils/0.4.0/kms_utils-0.4.0.sh /kms_utils.sh
WORKDIR /

RUN apt-get update -y && apt-get install -y htop xmlstarlet nginx realpath coreutils krb5-user libpam-krb5 libpam-ccreds auth-client-config curl wget php5-curl make jq vim && update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java && wget -qO- https://www.openssl.org/source/openssl-1.0.2l.tar.gz | tar xz && cd openssl-1.0.2l && sudo ./config && sudo make && sudo make install && sudo ln -sf /usr/local/ssl/bin/openssl /usr/bin/openssl && wget https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 && chmod +x jq-linux64 && mv jq-linux64 /usr/bin/jq && ls && apt-get install uuid-runtime && dpkg -i /*.deb && java -version

ENTRYPOINT ["/docker-entrypoint.sh"]
ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64
