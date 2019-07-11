/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.constants

object MarathonConstant {

  /* Constant variables */

  val AppMainClass = "com.stratio.sparta.driver.MarathonDriver"
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
  val DefaultIntervalSeconds = 20
  val DefaultTimeoutSeconds = 20
  val DefaultMaxConsecutiveFailures = 3
  val DefaultForcePullImage = true
  val DefaultPrivileged = false
  val DefaultIncludeCommonVolumes = true
  val DefaultIncludeCertVolumes = true
  val DefaultSparkUIPort = 4040
  val DefaultMetricsMarathonDriverPort = 6080
  val DefaultJmxMetricsMarathonDriverPort = 5080
  val DefaultSOMemSize = 1024
  val MinSOMemSize = 512
  val DefaultFileEncodingSystemProperty = "-Dfile.encoding=UTF-8"
  val DefaultRetryAttempts = 3
  val DefaultRetrySleep = 1000
  val DefaultMaxTimeOutInMarathonRequests = 10000

  /* Environment variables to Marathon Application */

  val AppTypeEnv = "SPARTA_APP_TYPE"
  val MesosNativeJavaLibraryEnv = "MESOS_NATIVE_JAVA_LIBRARY"
  val LdLibraryEnv = "LD_LIBRARY_PATH"
  val AppMainEnv = "SPARTA_MARATHON_MAIN_CLASS"
  val AppJarEnv = "SPARTA_MARATHON_JAR"
  val VaultEnableEnv = "VAULT_ENABLE"
  val VaultHostsEnv = "VAULT_HOSTS"
  val VaultTokenEnv = "VAULT_TOKEN"
  val ExecutionIdEnv = "SPARTA_EXECUTION_ID"
  val ZookeeperConfigEnv = "SPARTA_ZOOKEEPER_CONFIG"
  val DetailConfigEnv = "SPARTA_DETAIL_CONFIG"
  val PluginFiles = "SPARTA_PLUGIN_FILES"
  val AppHeapSizeEnv = "MARATHON_APP_HEAP_SIZE"
  val SpartaFileEncoding = "SPARTA_FILE_ENCODING"
  val SpartaOSMemoryEnv = "SPARTA_MARATHON_OS_MEMORY"
  val SpartaMarathonGracePeriodsSecondsEnv = "SPARTA_MARATHON_GRACEPERIODS_SECONDS"
  val SpartaMarathonIntervalSecondsEnv = "SPARTA_MARATHON_INTERVAL_SECONDS"
  val SpartaMarathonMaxFailuresEnv = "SPARTA_MARATHON_MAX_FAILURES"
  val SpartaMarathonTotalTimeBeforeKill = "SPARTA_MARATHON_TOTAL_TIME_BEFORE_KILL"
  val SparkHomeEnv = "SPARK_HOME"
  val DcosServiceName = "MARATHON_APP_LABEL_DCOS_SERVICE_NAME"
  val TenantIdentity = "TENANT_IDENTITY"
  val TenantName = "TENANT_NAME"
  val ServerTenantName = "SERVER_TENANT_NAME"
  val SpartaServerServiceIdWithPath = "SERVICE_ID_WITH_PATH"
  val SpartaServerMarathonAppId = "MARATHON_APP_ID"
  val DcosServiceBaseApplicationPath = "ROOT_SERVICE_PATH"
  val DcosServiceCompanyLabelPrefix = "MARATHON_COMPANY_LABEL_PREFIX"
  val HostnameConstraint = "MESOS_HOSTNAME_CONSTRAINT"
  val OperatorConstraint = "MESOS_OPERATOR_CONSTRAINT"
  val AttributeConstraint = "MESOS_ATTRIBUTE_CONSTRAINT"
  val MarathonAppConstraints = "MARATHONAPP_CONSTRAINTS"
  val SpartaSecretFolderEnv = "SPARTA_SECRET_FOLDER"
  val NginxIgnoreInvalidHeadersEnv = "SPARTA_NGINX_IGNORE_INVALID_HEADERS"
  val AppRoleEnv = "APPROLE"
  val AppRoleNameEnv = "APPROLENAME"
  val CalicoEnableEnv = "CALICO_ENABLED"
  val CalicoNetworkEnv = "CALICO_NETWORK"
  val MesosRoleEnv = "SPARK_MESOS_ROLE"
  val WorkflowIdentity = "WORKFLOW_IDENTITY"
  val WorkflowIdVaultPath= "GENERIC_WORKFLOW_ID_VAULT_PATH"
  val VaultPasswordsPath = "SPARTA_SECURITY_VAULT_PASSWORDS_PATH"
  val DynamicAuthEnv = "USE_DYNAMIC_AUTHENTICATION"
  val SpartaZookeeperPathEnv = "SPARTA_ZOOKEEPER_PATH"
  val NginxMarathonLBUserHostEnv = "USER_HAPROXY_VHOST"
  val NginxMarathonLBUserPathEnv = "USER_HAPROXY_PATH"
  val NginxMarathonLBHostEnv = "MARATHON_APP_LABEL_HAPROXY_1_VHOST"
  val NginxMarathonLBPathEnv = "MARATHON_APP_LABEL_HAPROXY_1_PATH"
  val NginxMarathonLBProxyPassPathEnv = "HAPROXY_1_HTTP_BACKEND_PROXYPASS_PATH"
  val NginxMarathonLBFrontendAclWithPathEnv = "HAPROXY_1_HTTPS_FRONTEND_ACL_WITH_PATH"
  val NginxMarathonLBRemovePathLocationEnv = "HAPROXY_1_REMOVE_PATH_LOCATION"
  val ServerMarathonLBHostEnv = "MARATHON_APP_LABEL_HAPROXY_0_VHOST"
  val ServerMarathonLBPathEnv = "MARATHON_APP_LABEL_HAPROXY_0_PATH"
  val GosecAuthEnableEnv = "ENABLE_GOSEC_AUTH"
  val UserNameEnv = "USER_NAME"
  val DatastoreCaNameEnv = "DATASTORE_TRUSTSTORE_CA_NAME"
  val SpartaTLSEnableEnv = "SECURITY_TLS_ENABLE"
  val sparkLogLevel = "SPARK_LOG_LEVEL"
  val spartaRedirectorLogLevel = "SPARTA_REDIRECTOR_LOG_LEVEL"
  val spartaLogLevel = "SPARTA_LOG_LEVEL"
  val workflowLabelsPrefix = "WORKFLOW_LABELS_PREFIX"
  val fixedWorkflowLabels = "FIXED_WORKFLOW_LABELS"
  val MesosTaskId = "MESOS_TASK_ID"
  val GenericWorkflowIdentity = "GENERIC_WORKFLOW_IDENTITY"
  val SpartaPostgresUser = "SPARTA_POSTGRES_USER"
  val TrimMarathonUri = "MARATHON_SSO_TRIM_URI"
  val PrometheusMetricsPortEnv = "SPARTA_METRICS_PORT"
  val JmxMetricsPortEnv = "SPARTA_JMX_METRICS_PORT"
  val HostInUseMetricsEnv = "HOST_IN_USE"
  val MarathonLabelPrefixEnv = "MARATHON_APP_LABEL_"
  val ProductLabelEnv = "PRODUCT"
  val ApplicationLabelEnv = "APPLICATION"
  val SpartaPluginInstance = "SPARTA_PLUGIN_INSTANCE"
  val SpartaWorkflowsPluginInstance = "SPARTA_WORKFLOWS_PLUGIN_INSTANCE"

}
