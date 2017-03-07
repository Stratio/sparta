#!/bin/bash

function make_directory() {
	local dir=$1
	local module=$2

	mkdir -p $dir \
	&& echo "[$module] Created $dir directory" \
	|| echo "[$module] Something was wrong creating $dir directory or already exists"
}

function configure_core-site() {
	local defaultfs=$1
  local namenode_principal=$2
  local namenode_principal_pattern=$3
	local ha_enable=$4
	local nameservice=$5
	local namenode1_address=$6
	local namenode2_address=$7
	local namenode1_http_address=$8
	local namenode2_http_address=$9

	sed -i "s#_<DEFAULTFS>_#$defaultfs#" /tmp/core-site.xml.tmp \
	&& echo "[CORE-SITE] HOSTNAME: $defaultFS configured in /tmp/core-site.xml.tmp" \
	|| echo "[CORE-SITE] Something went wrong when configuring defaultfs"

  sed -i "s#_<NAMENODE_PRINCIPAL>_#$namenode_principal#" /tmp/core-site.xml.tmp \
	&& echo "[CORE-SITE] DOMAIN: $namenode_principal configured in /tmp/core-site.xml.tmp" \
	|| echo "[CORE-SITE] Something went wrong when configuring hadoop namenode_principal"


  if [[ $ha_enable == 'true' ]]; then
  	sed -i "s#_<NAMESERVICE>_#$nameservice#" /tmp/core-site.xml.tmp \
    && echo "[CORE-SITE] NAMESERVICE: $nameservice configured in /tmp/core-site.xml.tmp" \
    || echo "[CORE-SITE] Something went wrong when configuring nameservice"

    sed -i "s#_<NAMENODE1_ADDRESS>_#$namenode1_address#g" /tmp/core-site.xml.tmp \
    && echo "[CORE-SITE] NN1: $namenode1_address configured in /tmp/core-site.xml.tmp" \
    || echo "[CORE-SITE] Something went wrong when configuring namenode1 address"

    sed -i "s#_<NAMENODE2_ADDRESS>_#$namenode2_address#g" /tmp/core-site.xml.tmp \
    && echo "[CORE-SITE] NN2: $namenode2_address configured in /tmp/core-site.xml.tmp" \
    || echo "[CORE-SITE] Something went wrong when configuring namenode2 address"

    sed -i "s#_<NAMENODE1_HTTP_ADDRESS>_#$namenode1_http_address#g" /tmp/core-site.xml.tmp \
    && echo "[CORE-SITE] NN1 HTTP ADDRESS: $namenode1_http_address configured in /tmp/core-site.xml.tmp" \
    || echo "[CORE-SITE] Something went wrong when configuring namenode1 http address"

    sed -i "s#_<NAMENODE2_HTTP_ADDRESS>_#$namenode2_http_address#g" /tmp/core-site.xml.tmp \
    && echo "[CORE-SITE] NN2 HTTP ADDRESS: $namenode2_http_address configured in /tmp/core-site.xml.tmp" \
    || echo "[CORE-SITE] Something went wrong when configuring namenode2 http address"

  fi

}

function generate_core-site() {
	local namenode_principal=$1
	local ha_enable=$2

	if [[ $ha_enable == 'false' ]]; then
	cat << EOF > /tmp/core-site.xml.tmp
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://_<DEFAULTFS>_</value>
  </property>
  <property>
   <name>dfs.permissions.enabled</name>
   <value>true</value>
  </property>
  <property>
   <name>dfs.permissions</name>
   <value>true</value>
  </property>
</configuration>

EOF
else
cat << EOF > /tmp/core-site.xml.tmp
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://_<DEFAULTFS>_</value>
  </property>
  <property>
   <name>dfs.permissions.enabled</name>
   <value>false</value>
  </property>
  <property>
   <name>dfs.permissions</name>
   <value>false</value>
  </property>
   <property>
    <name>dfs.nameservices</name>
    <value>_<NAMESERVICE>_</value>
  </property>
  <property>
    <name>dfs.ha.namenodes._<NAMESERVICE>_</name>
    <value>nn1,nn2</value>
  </property>
  <property>
    <name>dfs.namenode.rpc-address.mycluster.nn1</name>
    <value>_<NAMENODE1_ADDRESS>_</value>
  </property>
  <property>
    <name>dfs.namenode.rpc-address.mycluster.nn2</name>
    <value>_<NAMENODE2_ADDRESS>_</value>
  </property>
  <property>
    <name>dfs.namenode.http-address.mycluster.nn1</name>
    <value>_<NAMENODE1_HTTP_ADDRESS>_</value>
  </property>
  <property>
    <name>dfs.namenode.http-address.mycluster.nn2</name>
    <value>_<NAMENODE2_HTTP_ADDRESS>_</value>
  </property>
</configuration>

EOF
fi

  echo -e "[CORE-SITE] hadoop core-site.xml template was created"
}

function generate_core-site-from-uri() {
  make_directory $HADOOP_CONF_DIR "HADOOP"
  CORE_SITE="${HADOOP_CONF_DIR}/core-site.xml"
  wget "http://${DEFAULT_FS}:50070/conf"
  cp conf "${CORE_SITE}"
  rm -f conf
  sed -i "s|0.0.0.0|${DEFAULT_FS}|" ${CORE_SITE}

  if [[ $? == 0 ]]; then
    echo "[CORE-SITE] HADOOP $HADOOP_CONF_DIR/core-site.xml configured succesfully"
  else
    echo "[CORE-SITE] HADOOP $HADOOP_CONF_DIR/core-site.xml was NOT configured"
    exit 1
  fi
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
}

function generate_core-site-from-fs() {
  make_directory $HADOOP_CONF_DIR "HADOOP"

  ENABLE_HA=${ENABLE_HA:=false}
  NAMESERVICE=${NAMESERVICE:=stratio}
  NAMENODE1_ADDRESS=${NAMENODE1_ADDRESS:=namenode1:8020}
  NAMENODE2_ADDRESS=${NAMENODE2_ADDRESS:=namenode2:8020}
  NAMENODE1_HTTP_ADDRESS=${NAMENODE1_HTTP_ADDRESS:=namenode1:50070}
  NAMENODE2_HTTP_ADDRESS=${NAMENODE2_HTTP_ADDRESS:=namenode2:50070}
  NAMENODE_PRINCIPAL=${NAMENODE_PRINCIPAL:=hdfs/localhost}

  generate_core-site ${NAMENODE_PRINCIPAL} ${ENABLE_HA}

  configure_core-site $DEFAULT_FS $NAMENODE_PRINCIPAL ${ENABLE_HA} ${NAMESERVICE} ${NAMENODE1_ADDRESS} ${NAMENODE2_ADDRESS} ${NAMENODE1_HTTP_ADDRESS} ${NAMENODE2_HTTP_ADDRESS}

  mv -f /tmp/core-site.xml.tmp $HADOOP_CONF_DIR/core-site.xml

  if [[ $? == 0 ]]; then
    echo "[CORE-SITE] HADOOP $HADOOP_CONF_DIR/core-site.xml configured succesfully"
  else
    echo "[CORE-SITE] HADOOP $HADOOP_CONF_DIR/core-site.xml was NOT configured"
    exit 1
  fi
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
}
