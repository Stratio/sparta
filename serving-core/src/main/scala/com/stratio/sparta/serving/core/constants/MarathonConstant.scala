/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.constants

object MarathonConstant {

  /* Constant variables */

  val AppMainClass = "com.stratio.sparta.driver.MarathonDriver"
  val DefaultMarathonTemplateFile = "/etc/sds/sparta/marathon-app-template.json"
  val MarathonApp = "marathon"
  val HostMesosNativeLibPath = "/opt/mesosphere/lib"
  val HostMesosNativePackagesPath = "/opt/mesosphere/packages"
  val HostMesosLib = s"$HostMesosNativeLibPath"
  val HostMesosNativeLib = s"$HostMesosNativeLibPath/libmesos.so"
  val DefaultMemory = 1024
  val Krb5ConfFile = "/etc/krb5.conf"
  val ResolvConfigFile = "/etc/resolv.conf"
  val ContainerCertificatePath = "/etc/ssl/certs/java/cacerts"
  val ContainerJavaCertificatePath = "/etc/pki/ca-trust/extracted/java/cacerts"
  val HostCertificatePath = "/etc/pki/ca-trust/extracted/java/cacerts"
  val HostJavaCertificatePath = "/usr/lib/jvm/jre1.8.0_112/lib/security/cacerts"
  val DefaultGracePeriodSeconds = 240
  val DefaultIntervalSeconds = 60
  val DefaultTimeoutSeconds = 30
  val DefaultMaxConsecutiveFailures = 3
  val DefaultForcePullImage = false
  val DefaultPrivileged = false
  val DefaultIncludeCommonVolumes = true
  val DefaultIncludeCertVolumes = true
  val DefaultSparkUIPort = 4040
  val DefaultSOMemSize = 512
  val MinSOMemSize = 256

  /* Environment variables to Marathon Application */

  val AppTypeEnv = "SPARTA_APP_TYPE"
  val MesosNativeJavaLibraryEnv = "MESOS_NATIVE_JAVA_LIBRARY"
  val LdLibraryEnv = "LD_LIBRARY_PATH"
  val AppMainEnv = "SPARTA_MARATHON_MAIN_CLASS"
  val AppJarEnv = "SPARTA_MARATHON_JAR"
  val VaultEnableEnv = "VAULT_ENABLE"
  val VaultHostsEnv = "VAULT_HOSTS"
  val VaultTokenEnv = "VAULT_TOKEN"
  val WorkflowIdEnv = "SPARTA_WORKFLOW_ID"
  val ZookeeperConfigEnv = "SPARTA_ZOOKEEPER_CONFIG"
  val DetailConfigEnv = "SPARTA_DETAIL_CONFIG"
  val PluginFiles = "SPARTA_PLUGIN_FILES"
  val AppHeapSizeEnv = "MARATHON_APP_HEAP_SIZE"
  val SpartaOSMemoryEnv = "SPARTA_MARATHON_OS_MEMORY"
  val SparkHomeEnv = "SPARK_HOME"
  val DcosServiceName = "MARATHON_APP_LABEL_DCOS_SERVICE_NAME"
  val HostnameConstraint = "MESOS_HOSTNAME_CONSTRAINT"
  val OperatorConstraint = "MESOS_OPERATOR_CONSTRAINT"
  val AttributeConstraint = "MESOS_ATTRIBUTE_CONSTRAINT"
  val MarathonAppConstraints = "MARATHONAPP_CONSTRAINTS"
  val SpartaSecretFolderEnv = "SPARTA_SECRET_FOLDER"
  val AppRoleEnv = "APPROLE"
  val AppRoleNameEnv = "APPROLENAME"
  val CalicoEnableEnv = "CALICO_ENABLED"
  val CalicoNetworkEnv = "CALICO_NETWORK"
  val SparkUIPort = "PORT_SPARKUI"
  val MesosRoleEnv = "SPARK_MESOS_ROLE"
  val TenantEnv = "TENANT_NAME"
  val DynamicAuthEnv = "USE_DYNAMIC_AUTHENTICATION"
  val SpartaZookeeperPathEnv = "SPARTA_ZOOKEEPER_PATH"
  val LoggerStdoutSizeEnv = "CONTAINER_LOGGER_MAX_STDOUT_SIZE"
  val LoggerStderrSizeEnv = "CONTAINER_LOGGER_MAX_STDERR_SIZE"
  val LoggerStdoutRotateEnv = "CONTAINER_LOGGER_LOGROTATE_STDOUT_OPTIONS"
  val LoggerStderrRotateEnv = "CONTAINER_LOGGER_LOGROTATE_STDERR_OPTIONS"
  val NginxMarathonLBHostEnv = "MARATHON_APP_LABEL_HAPROXY_1_VHOST"
  val GosecAuthEnableEnv = "ENABLE_GOSEC_AUTH"
  val UserNameEnv = "USER_NAME"

}
