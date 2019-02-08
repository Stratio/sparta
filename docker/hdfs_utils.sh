#!/bin/bash

function make_directory() {
	local dir=$1
	local module=$2

	mkdir -p $dir \
	&& INFO "[$module] Created $dir directory" \
	|| ERROR "[$module] Something was wrong creating $dir directory or already exists"
}

function generate_hdfs-conf-from-uri() {
  make_directory $HADOOP_CONF_DIR "HADOOP-CONF"
  CORE_SITE="${HADOOP_CONF_DIR}/core-site.xml"
  CORE_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/core-site.xml"
  HDFS_SITE="${HADOOP_CONF_DIR}/hdfs-site.xml"
  HDFS_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/hdfs-site.xml"

  if [[ -v SECURITY_TLS_ENABLE ]] && [[ ${#SECURITY_TLS_ENABLE} != 0 ]] && [[ $SECURITY_TLS_ENABLE == "true" ]]; then
    wget --certificate=$SPARTA_SECRET_FOLDER/${TENANT_NAME}.pem "${HADOOP_CONF_URI}/core-site.xml" &&\
    wget --certificate=$SPARTA_SECRET_FOLDER/${TENANT_NAME}.pem "${HADOOP_CONF_URI}/hdfs-site.xml"
    export RESULT_WGET=$?
  elif [[ ! -v SECURITY_TLS_ENABLE ]]  ||  [[ -v RESULT_WGET ]] && [[ $RESULT_WGET != 0 ]]; then
    wget "${HADOOP_CONF_URI}/core-site.xml"
    wget "${HADOOP_CONF_URI}/hdfs-site.xml"
  fi


  if [[ -v SPARTA_PRINCIPAL_NAME ]] ; then
    # Check if the property has already been defined count=1 or not count=0
    count=$(xmlstarlet sel -t -v "count(/configuration/property[name='yarn.resourcemanager.principal'])" "hdfs-site.xml")

    if [[ ${count} == 0 ]]; then
     xmlstarlet ed -L -a '/configuration/property[last()]' -t elem -n 'xx' \
         -s '/configuration/xx' -t elem -n 'name' -v "yarn.resourcemanager.principal"\
         -s '/configuration/xx' -t elem -n 'value' -v "${SPARTA_PRINCIPAL_NAME}"\
         -r '/configuration/xx' -v 'property' "hdfs-site.xml"
    else
        xmlstarlet ed -L -u "/configuration/property[name='yarn.resourcemanager.principal']/value" \
        -v "${SPARTA_PRINCIPAL_NAME}" "hdfs-site.xml"
    fi
  fi

  cp core-site.xml "${CORE_SITE}"
  cp core-site.xml "${CORE_SITE_CLASSPATH}"
  cp hdfs-site.xml "${HDFS_SITE}"
  cp hdfs-site.xml "${HDFS_SITE_CLASSPATH}"
  rm -f core-site.xml
  rm -f hdfs-site.xml

  if [[ $? == 0 ]]; then
    INFO "[HADOOP-CONF] HADOOP $CORE_SITE and $HDFS_SITE configured succesfully"
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${VARIABLES}
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
  else
    ERROR "[HADOOP-CONF] HADOOP $CORE_SITE and $HDFS_SITE was NOT configured"
    exit 1
  fi
}

function generate_hdfs-conf-from-fs() {
  make_directory $HADOOP_CONF_DIR "HADOOP-CONF"

cat > "${HADOOP_CONF_DIR}/core-site.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
        <configuration>
          <property>
            <name>hadoop.security.authentication</name>
            <value>__<HADOOP_SECURITY_AUTH>__</value>
          </property>
          <property>
            <name>fs.default.name</name>
            <value>__<HADOOP_FS_DEFAULT_NAME>__</value>
          </property>
          <property>
            <name>hadoop.rpc.protection</name>
            <value>__<HADOOP_RPC_PROTECTION>__</value>
          </property>
          <property>
            <name>hadoop.security.token.service.use_ip</name>
            <value>__<HADOOP_SECURITY_TOKEN_USE_IP>__</value>
          </property>
          __HADOOP_CORE_SITE_EXTRA_PROPERTIES__
        </configuration>
EOF

 if [[ -v HADOOP_CORE_SITE_EXTRA_PROPERTIES ]]; then
   sed -i "s|.*__HADOOP_CORE_SITE_EXTRA_PROPERTIES__.*|${HADOOP_CORE_SITE_EXTRA_PROPERTIES}s|" "${HADOOP_CONF_DIR}/core-site.xml"
 fi

sed -i "s#__<HADOOP_SECURITY_AUTH>__#$HADOOP_SECURITY_AUTH#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& INFO "[HADOOP-CONF] hadoop.security.authentication configured in core-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_SECURITY_AUTH was configured in core-site.xml"

sed -i "s#__<HADOOP_FS_DEFAULT_NAME>__#$HADOOP_FS_DEFAULT_NAME#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& INFO "[HADOOP-CONF] fs.default.name in core-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_FS_DEFAULT_NAME was configured in core-site.xml"

sed -i "s#__<HADOOP_RPC_PROTECTION>__#$HADOOP_RPC_PROTECTION#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& INFO "[HADOOP-CONF] hadoop.rpc.protection in core-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_RPC_PROTECTION was configured in core-site.xml"

sed -i "s#__<HADOOP_SECURITY_TOKEN_USE_IP>__#$HADOOP_SECURITY_TOKEN_USE_IP#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& INFO "[HADOOP-CONF] hadoop.security.token.service.use_ip configured in core-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_SECURITY_TOKEN_USE_IP was configured in core-site.xml"

cat > "${HADOOP_CONF_DIR}/hdfs-site.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
        <configuration>
          <property>
            <name>yarn.resourcemanager.principal</name>
            <value>__<SPARTA_PRINCIPAL_NAME>__</value>
          </property>
          <property>
               <name>dfs.namenode.kerberos.principal</name>
               <value>__<HADOOP_NAMENODE_KRB_PRINCIPAL>__</value>
          </property>
          <property>
               <name>dfs.namenode.kerberos.principal.pattern</name>
               <value>__<HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN>__</value>
          </property>
          <property>
               <name>dfs.encrypt.data.transfer</name>
               <value>__<HADOOP_DFS_ENCRYPT_DATA_TRANSFER>__</value>
          </property>
          <property>
               <name>dfs.encrypt.data.transfer.cipher.suites</name>
               <value>__<HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES>__</value>
          </property>
          <property>
               <name>dfs.encrypt.data.transfer.cipher.key.bitlength</name>
               <value>__<HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH>__</value>
          </property>
          <property>
               <name>map.reduce.framework.name</name>
               <value>__<HADOOP_MAP_REDUCE_FRAMEWORK_NAME>__</value>
          </property>
          __HADOOP_HDFS_SITE_EXTRA_PROPERTIES__
        </configuration>
EOF

 if [[ -v HADOOP_HDFS_SITE_EXTRA_PROPERTIES ]]; then
   sed -i "s|.*__HADOOP_HDFS_SITE_EXTRA_PROPERTIES__.*|${HADOOP_HDFS_SITE_EXTRA_PROPERTIES}s|" "${HADOOP_CONF_DIR}/hdfs-site.xml"
 fi

sed -i "s#__<SPARTA_PRINCIPAL_NAME>__#$SPARTA_PRINCIPAL_NAME#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& INFO "[HADOOP-CONF] yarn.resourcemanager.principal in hdfs-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when KERBEROS_PRINCIPAL_NAME was configured in hdfs-site.xml"

sed -i "s#__<HADOOP_NAMENODE_KRB_PRINCIPAL>__#$HADOOP_NAMENODE_KRB_PRINCIPAL#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& INFO "[HADOOP-CONF] dfs.namenode.kerberos.principal in hdfs-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_NAMENODE_KRB_PRINCIPAL was configured in hdfs-site.xml"

sed -i "s#__<HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN>__#$HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& INFO "[HADOOP-CONF] dfs.namenode.kerberos.principal.pattern in hdfs-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN was configured in hdfs-site.xml"

sed -i "s#__<HADOOP_DFS_ENCRYPT_DATA_TRANSFER>__#$HADOOP_DFS_ENCRYPT_DATA_TRANSFER#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& INFO "[HADOOP-CONF] dfs.encrypt.data.transfer in hdfs-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_DFS_ENCRYPT_DATA_TRANSFER was configured in hdfs-site.xml"

sed -i "s#__<HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES>__#$HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& INFO "[HADOOP-CONF] dfs.encrypt.data.transfer.cipher.suites in hdfs-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES was configured in hdfs-site.xml"

sed -i "s#__<HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH>__#$HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& INFO "[HADOOP-CONF] dfs.encrypt.data.transfer.cipher.key.bitlength in hdfs-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH was configured in hdfs-site.xml"

sed -i "s#__<HADOOP_MAP_REDUCE_FRAMEWORK_NAME>__#$HADOOP_MAP_REDUCE_FRAMEWORK_NAME#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& INFO "[HADOOP-CONF] map.reduce.framework.name in hdfs-site.xml" \
|| ERROR "[HADOOP-CONF] Something went wrong when HADOOP_MAP_REDUCE_FRAMEWORK_NAME was configured in hdfs-site.xml"

  if [[ $? == 0 ]]; then
    INFO "[HADOOP-CONF] HADOOP $CORE_SITE and $HDFS_SITE configured succesfully"
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${VARIABLES}
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
    CORE_SITE="${HADOOP_CONF_DIR}/core-site.xml"
    CORE_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/core-site.xml"
    HDFS_SITE="${HADOOP_CONF_DIR}/hdfs-site.xml"
    HDFS_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/hdfs-site.xml"
    cp "${CORE_SITE}" "${CORE_SITE_CLASSPATH}"
    cp "${HDFS_SITE}" "${HDFS_SITE_CLASSPATH}"
  else
    ERROR "[HADOOP-CONF] HADOOP $CORE_SITE and $HDFS_SITE was NOT configured"
    exit 1
  fi
}