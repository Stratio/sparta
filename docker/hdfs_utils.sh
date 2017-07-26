#!/bin/bash

function make_directory() {
	local dir=$1
	local module=$2

	mkdir -p $dir \
	&& echo "[$module] Created $dir directory" \
	|| echo "[$module] Something was wrong creating $dir directory or already exists"
}

function generate_core-site-from-uri() {
  make_directory $HADOOP_CONF_DIR "HADOOP"
  CORE_SITE="${HADOOP_CONF_DIR}/core-site.xml"
  CORE_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/core-site.xml"
  wget "${HADOOP_CONF_URI}/conf"
  cp conf "${CORE_SITE}"
  cp conf "${CORE_SITE_CLASSPATH}"
  rm -f conf
  sed -i "s|0.0.0.0|${HADOOP_FS_DEFAULT_NAME}|" ${CORE_SITE}

  if [[ $? == 0 ]]; then
    echo "[CORE-SITE] HADOOP $HADOOP_CONF_DIR/core-site.xml configured succesfully"
  else
    echo "[CORE-SITE] HADOOP $HADOOP_CONF_DIR/core-site.xml was NOT configured"
    exit 1
  fi
  echo "" >> ${VARIABLES}
  echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${VARIABLES}
  echo "" >> ${SYSTEM_VARIABLES}
  echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
}

function generate_hdfs-conf-from-uri() {
  make_directory $HADOOP_CONF_DIR "HADOOP"
  CORE_SITE="${HADOOP_CONF_DIR}/core-site.xml"
  CORE_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/core-site.xml"
  HDFS_SITE="${HADOOP_CONF_DIR}/hdfs-site.xml"
  HDFS_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/hdfs-site.xml"
  wget "${HADOOP_CONF_URI}/core-site.xml"
  wget "${HADOOP_CONF_URI}/hdfs-site.xml"
  cp core-site.xml "${CORE_SITE}"
  cp core-site.xml "${CORE_SITE_CLASSPATH}"
  cp hdfs-site.xml "${HDFS_SITE}"
  cp hdfs-site.xml "${HDFS_SITE_CLASSPATH}"
  rm -f core-site.xml
  rm -f hdfs-site.xml

  if [[ $? == 0 ]]; then
    echo "[HADOOP-CONF] HADOOP $CORE_SITE and $HDFS_SITE configured succesfully"
    echo "" >> ${VARIABLES}
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${VARIABLES}
    echo "" >> ${SYSTEM_VARIABLES}
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
  else
    echo "[HADOOP-CONF] HADOOP $CORE_SITE and $HDFS_SITE was NOT configured"
    exit 1
  fi
}

function generate_hdfs-conf-from-fs() {
  make_directory $HADOOP_CONF_DIR "HADOOP"

cat > "${HADOOP_CONF_DIR}/core-site.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
        <configuration>
          <property>
            <name>hadoop.security.authorization</name>
            <value>True</value>
          </property>
          <property>
            <name>hadoop.security.authentication</name>
            <value>__<SECURITY_AUTH>__</value>
          </property>
          <property>
            <name>fs.default.name</name>
            <value>__<FS_DEFAULT_NAME>__</value>
          </property>
          <property>
            <name>hadoop.rpc.protection</name>
            <value>__<RPC_PROTECTION>__</value>
          </property>
          <property>
            <name>dfs.encrypt.data.transfer</name>
            <value>__<ENCRYPT_DATA_TRANSFER>__</value>
          </property>
          <property>
            <name>hadoop.security.token.service.use_ip</name>
            <value>__<SECURITY_TOKEN_USE_IP>__</value>
          </property>
        </configuration>
EOF

sed -i "s#__<SECURITY_AUTH>__#$HADOOP_SECURITY_AUTH#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& echo "[core-site.xml] hadoop.security.authentication configured in core-site.xml" \
|| echo "[core-site.xml-ERROR] Something went wrong when HADOOP_SECURITY_AUTH was configured in core-site.xml"

sed -i "s#__<FS_DEFAULT_NAME>__#$HADOOP_FS_DEFAULT_NAME#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& echo "[core-site.xml] fs.default.name in core-site.xml" \
|| echo "[core-site.xml-ERROR] Something went wrong when HADOOP_FS_DEFAULT_NAME was configured in core-site.xml"

sed -i "s#__<RPC_PROTECTION>__#$HADOOP_RPC_PROTECTION#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& echo "[core-site.xml] hadoop.rpc.protection in core-site.xml" \
|| echo "[core-site.xml-ERROR] Something went wrong when HADOOP_RPC_PROTECTION was configured in core-site.xml"

sed -i "s#__<ENCRYPT_DATA_TRANSFER>__#$HADOOP_DFS_ENCRYPT_DATA_TRANSFER#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& echo "[core-site.xml] dfs.encrypt.data.transfer in core-site.xml" \
|| echo "[core-site.xml-ERROR] Something went wrong when HADOOP_DFS_ENCRYPT_DATA_TRANSFER was configured in core-site.xml"

sed -i "s#__<SECURITY_TOKEN_USE_IP>__#$HADOOP_SECURITY_TOKEN_USE_IP#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& echo "[core-site.xml] hadoop.security.token.service.use_ip configured in core-site.xml" \
|| echo "[core-site.xml-ERROR] Something went wrong when HADOOP_SECURITY_TOKEN_USE_IP was configured in core-site.xml"

cat > "${HADOOP_CONF_DIR}/hdfs-site.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
        <configuration>
          <property>
               <name>dfs.namenode.kerberos.principal</name>
               <value>__<KERBEROS_PRINCIPAL>__</value>
          </property>
          <property>
               <name>dfs.namenode.kerberos.principal.pattern</name>
               <value>__<KERBEROS_PRINCIPAL_PATTERN>__</value>
          </property>
          <property>
               <name>dfs.encrypt.data.transfer</name>
               <value>__<ENCRYPT_DATA_TRANSFER>__</value>
          </property>
          <property>
               <name>dfs.encrypt.data.transfer.cipher.suites</name>
               <value>__<ENCRYPT_DATA_TRANSFER_CIPHER_SUITES>__</value>
          </property>
          <property>
               <name>dfs.encrypt.data.transfer.cipher.key.bitlength</name>
               <value>__<ENCRYPT_DATA_TRANSFER_CIPHER_KEY_BITLENGTH>__</value>
          </property>
        </configuration>
EOF

sed -i "s#__<KERBEROS_PRINCIPAL>__#$HADOOP_NAMENODE_KRB_PRINCIPAL#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& echo "[hdfs-site.xml] dfs.namenode.kerberos.principal in hdfs-site.xml" \
|| echo "[hdfs-site.xml-ERROR] Something went wrong when HADOOP_NAMENODE_KRB_PRINCIPAL was configured in hdfs-site.xml"

sed -i "s#__<KERBEROS_PRINCIPAL_PATTERN>__#$HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& echo "[hdfs-site.xml] dfs.namenode.kerberos.principal.pattern in hdfs-site.xml" \
|| echo "[hdfs-site.xml-ERROR] Something went wrong when HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN was configured in hdfs-site.xml"

sed -i "s#__<ENCRYPT_DATA_TRANSFER>__#$HADOOP_DFS_ENCRYPT_DATA_TRANSFER#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& echo "[hdfs-site.xml] dfs.encrypt.data.transfer in hdfs-site.xml" \
|| echo "[hdfs-site.xml-ERROR] Something went wrong when HADOOP_DFS_ENCRYPT_DATA_TRANSFER was configured in hdfs-site.xml"

sed -i "s#__<ENCRYPT_DATA_TRANSFER_CIPHER_SUITES>__#$HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& echo "[hdfs-site.xml] dfs.encrypt.data.transfer.cipher.suites in hdfs-site.xml" \
|| echo "[hdfs-site.xml-ERROR] Something went wrong when HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES was configured in hdfs-site.xml"

sed -i "s#__<ENCRYPT_DATA_TRANSFER_CIPHER_KEY_BITLENGTH>__#$HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH#" "${HADOOP_CONF_DIR}/hdfs-site.xml" \
&& echo "[hdfs-site.xml] dfs.encrypt.data.transfer.cipher.key.bitlength in hdfs-site.xml" \
|| echo "[hdfs-site.xml-ERROR] Something went wrong when HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH was configured in hdfs-site.xml"


  if [[ $? == 0 ]]; then
    echo "[HADOOP-CONF] HADOOP $CORE_SITE and $HDFS_SITE configured succesfully"
    echo "" >> ${VARIABLES}
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${VARIABLES}
    echo "" >> ${SYSTEM_VARIABLES}
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
    CORE_SITE="${HADOOP_CONF_DIR}/core-site.xml"
    CORE_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/core-site.xml"
    HDFS_SITE="${HADOOP_CONF_DIR}/hdfs-site.xml"
    HDFS_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/hdfs-site.xml"
    cp "${CORE_SITE}" "${CORE_SITE_CLASSPATH}"
    cp "${HDFS_SITE}" "${HDFS_SITE_CLASSPATH}"
  else
    echo "[HADOOP-CONF] HADOOP $CORE_SITE and $HDFS_SITE was NOT configured"
    exit 1
  fi
}

function generate_hdfs-conf-from-fs-not-secured() {
  make_directory $HADOOP_CONF_DIR "HADOOP"

cat > "${HADOOP_CONF_DIR}/core-site.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
        <configuration>
           <property>
             <name>fs.default.name</name>
             <value>__<FS_DEFAULT_NAME>__</value>
           </property>
           <property>
             <name>hadoop.security.authentication</name>
             <value>simple</value>
           </property>
           <property>
             <name>hadoop.http.authentication.type</name>
             <value>simple</value>
           </property>
           <property>
             <name>hadoop.security.authorization</name>
             <value>false</value>
           </property>
           <property>
             <name>hbase.security.authentication</name>
             <value>Simple</value>
           </property>
           <property>
             <name>hbase.security.authorization</name>
             <value>false</value>
           </property>
           <property>
             <name>ipc.client.fallback-to-simple-auth-allowed</name>
             <value>true</value>
           </property>
           <property>
             <name>dfs.replication</name>
              <value>1</value>
           </property>
        </configuration>
EOF

sed -i "s#__<FS_DEFAULT_NAME>__#$HADOOP_FS_DEFAULT_NAME#" "${HADOOP_CONF_DIR}/core-site.xml" \
&& echo "[core-site.xml] fs.default.name in core-site.xml" \
|| echo "[core-site.xml-ERROR] Something went wrong when HADOOP_FS_DEFAULT_NAME was configured in core-site.xml"


  if [[ $? == 0 ]]; then
    echo "[HADOOP-CONF] HADOOP $CORE_SITE not secured configured succesfully"
    echo "" >> ${VARIABLES}
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${VARIABLES}
    echo "" >> ${SYSTEM_VARIABLES}
    echo "export HADOOP_CONF_DIR=${HADOOP_CONF_DIR}" >> ${SYSTEM_VARIABLES}
    CORE_SITE="${HADOOP_CONF_DIR}/core-site.xml"
    CORE_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/core-site.xml"
    HDFS_SITE="${HADOOP_CONF_DIR}/hdfs-site.xml"
    HDFS_SITE_CLASSPATH="${SPARTA_CLASSPATH_DIR}/hdfs-site.xml"
    cp "${CORE_SITE}" "${CORE_SITE_CLASSPATH}"
    cp "${HDFS_SITE}" "${HDFS_SITE_CLASSPATH}"
  else
    echo "[HADOOP-CONF] HADOOP $CORE_SITE not secured was NOT configured"
    exit 1
  fi
}
