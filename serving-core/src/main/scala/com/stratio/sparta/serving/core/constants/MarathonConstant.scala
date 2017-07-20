/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.stratio.sparta.serving.core.constants

object MarathonConstant {

  /* Environment variables to Marathon Application */

  val AppTypeEnv = "SPARTA_APP_TYPE"
  val MesosNativeJavaLibraryEnv = "MESOS_NATIVE_JAVA_LIBRARY"
  val LdLibraryEnv = "LD_LIBRARY_PATH"
  val AppMainEnv = "SPARTA_MARATHON_MAIN_CLASS"
  val AppJarEnv = "SPARTA_MARATHON_JAR"
  val VaultEnableEnv = "VAULT_ENABLE"
  val VaultHostsEnv = "VAULT_HOSTS"
  val VaultPortEnv = "VAULT_PORT"
  val VaultTokenEnv = "VAULT_TOKEN"
  val WorkflowIdEnv = "SPARTA_WORKFLOW_ID"
  val ZookeeperConfigEnv = "SPARTA_ZOOKEEPER_CONFIG"
  val DetailConfigEnv = "SPARTA_DETAIL_CONFIG"
  val AppHeapSizeEnv = "MARATHON_APP_HEAP_SIZE"
  val AppHeapMinimunSizeEnv = "MARATHON_APP_HEAP_MINIMUM_SIZE"
  val SparkHomeEnv = "SPARK_HOME"
  val HadoopUserNameEnv = "HADOOP_USER_NAME"
  val HdfsConfFromUriEnv = "HADOOP_CONF_FROM_URI"
  val CoreSiteFromUriEnv = "CORE_SITE_FROM_URI"
  val HdfsConfFromDfsEnv = "HADOOP_CONF_FROM_DFS"
  val HdfsConfFromDfsNotSecuredEnv = "HADOOP_CONF_FROM_DFS_NOT_SECURED"
  val DefaultFsEnv = "HADOOP_FS_DEFAULT_NAME"
  val DefaultHdfsConfUriEnv = "HADOOP_CONF_URI"
  val HadoopConfDirEnv = "HADOOP_CONF_DIR"
  val ServiceLogLevelEnv = "SERVICE_LOG_LEVEL"
  val SpartaLogLevelEnv = "SPARTA_LOG_LEVEL"
  val SparkLogLevelEnv = "SPARK_LOG_LEVEL"
  val ZookeeperLogLevelEnv = "ZOOKEEPER_LOG_LEVEL"
  val HadoopLogLevelEnv = "HADOOP_LOG_LEVEL"
  val ParquetLogLevelEnv = "PARQUET_LOG_LEVEL"
  val AvroLogLevelEnv = "AVRO_LOG_LEVEL"
  val NettyLogLevelEnv = "NETTY_LOG_LEVEL"
  val DcosServiceName = "MARATHON_APP_LABEL_DCOS_SERVICE_NAME"
  val Constraints = "MESOS_CONSTRAINTS"
  val HdfsRpcProtectionEnv = "HADOOP_RPC_PROTECTION"
  val HdfsSecurityAuthEnv = "HADOOP_SECURITY_AUTH"
  val HdfsEncryptDataEnv = "HADOOP_DFS_ENCRYPT_DATA_TRANSFER"
  val HdfsTokenUseIpEnv = "HADOOP_SECURITY_TOKEN_USE_IP"
  val HdfsKerberosPrincipalEnv = "HADOOP_NAMENODE_KRB_PRINCIPAL"
  val HdfsKerberosPrincipalPatternEnv = "HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN"
  val HdfsEncryptDataTransferEnv = "HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES"
  val HdfsEncryptDataBitLengthEnv = "HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH"
  val SparkUserEnv = "SPARK_USER"
  val SecurityTlsEnv = "SECURITY_TLS_ENABLE"
  val SecurityTrustoreEnv = "SECURITY_TRUSTSTORE_ENABLE"
  val SecurityKerberosEnv = "SECURITY_KRB_ENABLE"
  val SecurityOauth2Env = "SECURITY_OAUTH2_ENABLE"
  val SecurityMesosEnv = "SECURITY_MESOS_ENABLE"
  val CrossdataCoreCatalogClass = "CROSSDATA_CORE_CATALOG_CLASS"
  val CrossdataCoreCatalogPrefix = "CROSSDATA_CORE_CATALOG_PREFIX"
  val CrossdataCoreCatalogZookeeperConnectionString = "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING"
  val CrossdataCoreCatalogZookeeperConnectionTimeout = "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONTIMEOUT"
  val CrossdataCoreCatalogZookeeperSessionTimeout = "CROSSDATA_CORE_CATALOG_ZOOKEEPER_SESSIONTIMEOUT"
  val CrossdataCoreCatalogZookeeperRetryAttempts = "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYATTEMPTS"
  val CrossdataCoreCatalogZookeeperRetryInterval = "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYINTERVAL"
  val AppRoleEnv = "APPROLE"
  val AppRoleNameEnv = "APPROLENAME"
  val CalicoEnableEnv = "CALICO_ENABLED"
  val CalicoNetworkEnv = "CALICO_NETWORK"
  val SparkUIPort = "PORT_SPARKUI"
  val MesosPrincipalEnv = "SPARK_MESOS_PRINCIPAL"
  val MesosSecretEnv = "SPARK_MESOS_SECRET"
  val MesosRoleEnv = "SPARK_MESOS_ROLE"
  val DynamicAuthEnv = "USE_DYNAMIC_AUTHENTICATION"
}