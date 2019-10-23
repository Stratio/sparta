/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.utils.MarathonAPIUtils._
import com.stratio.sparta.serving.core.utils.NginxUtils.NginxMetaConfig
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class NginxUtilsTest extends BaseUtilsTest {
  nuTest =>

  val materializer = ActorMaterializer()

  val nginxMetaConfig = NginxMetaConfig(
    instanceName = "sparta-server"
  )

  val testNginx = new NginxUtils(system, materializer, nginxMetaConfig)

  "NginxUtils.updatedNginxConf" when {
    "Given a seq of tuples (String,Int)" should {
      "create a string with all nginx configuration" in {
        val listWorkflowsFake = Seq(
          AppParameters("/sparta-server/kafka-sec", "123.123.123.1", 4040),
          AppParameters("/sparta-server/spark-history-server", "123.123.123.1", 18080),
          AppParameters("/sparta-server/sparta-api", "123.123.123.1", 9090),
          AppParameters("/sparta-server/postgres", "123.123.123.1", 4040)
        )
        val result = testNginx.updatedNginxConf(listWorkflowsFake, "sparta-server", "sparta-server")

        result should not be empty
      }
    }
  }


  val jsonApp="""{
                |  "id": "/sparta/sparta-server/workflows",
                |  "dependencies": [],
                |  "version": "2018-01-04T12:21:46.218Z",
                |  "apps": [{
                |    "id": "/sparta/sparta-server/workflows/testinput-kafka2",
                |    "cmd": null,
                |    "args": null,
                |    "user": null,
                |    "env": {
                |      "VAULT_HOST": "vault.service.paas.labs.stratio.com",
                |      "SPARK_SECURITY_DATASTORE_VAULT_CERT_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
                |      "ZOOKEEPER_LOG_LEVEL": "OFF",
                |      "SPARTA_MARATHON_JAR": "/opt/sds/sparta/driver/sparta-driver.jar",
                |      "SECURITY_MESOS_ENABLE": "true",
                |      "HADOOP_CONF_FROM_DFS": "true",
                |      "CROSSDATA_CORE_CATALOG_CLASS": "org.apache.spark.sql.crossdata.catalyst.catalog.persistent.zookeeper.ZookeeperCatalog",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONTIMEOUT": "15000",
                |      "HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH": "128",
                |      "CORE_SITE_FROM_URI": "false",
                |      "SPARTA_ZOOKEEPER_CONFIG": "eyJ6b29rZWVwZXIiOnsiY29ubmVjdGlvblN0cmluZyI6InprLTAwMDEtemt1c2VybGFuZC5zZXJ2aWNlLnBhYXMubGFicy5zdHJhdGlvLmNvbToyMTgxLHprLTAwMDItemt1c2VybGFuZC5zZXJ2aWNlLnBhYXMubGFicy5zdHJhdGlvLmNvbToyMTgxLHprLTAwMDMtemt1c2VybGFuZC5zZXJ2aWNlLnBhYXMubGFicy5zdHJhdGlvLmNvbToyMTgxIiwiY29ubmVjdGlvblRpbWVvdXQiOjE1MDAwLCJyZXRyeUF0dGVtcHRzIjo1LCJyZXRyeUludGVydmFsIjoxMDAwMCwic2Vzc2lvblRpbWVvdXQiOjYwMDAwLCJzdG9yYWdlUGF0aCI6Ii9zdHJhdGlvL3NwYXJ0YS9zcGFydGEifX0=",
                |      "CROSSDATA_LOG_LEVEL": "ERROR",
                |      "HADOOP_SECURITY_AUTH": "kerberos",
                |      "APP_NAME": "sparta-server",
                |      "SERVICE_LOG_LEVEL": "ERROR",
                |      "HADOOP_FS_DEFAULT_NAME": "10.200.0.74",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYINTERVAL": "10000",
                |      "SECURITY_TRUSTSTORE_ENABLE": "true",
                |      "SECURITY_KRB_ENABLE": "true",
                |      "SPARK_SECURITY_DATASTORE_VAULT_TRUSTSTORE_PASS_PATH": "/v1/ca-trust/passwords/default/keystore",
                |      "HADOOP_SECURITY_TOKEN_USE_IP": "false",
                |      "MESOS_NATIVE_JAVA_LIBRARY": "/usr/lib/libmesos.so",
                |      "PARQUET_LOG_LEVEL": "ERROR",
                |      "SPARTA_APP_TYPE": "marathon",
                |      "MARATHON_APP_LABEL_DCOS_SERVICE_NAME": "sparta-server",
                |      "CROSSDATA_CORE_CATALOG_PREFIX": "spartaCluster",
                |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER": "true",
                |      "CA_NAME": "ca",
                |      "SPARK_HOME": "/opt/spark/dist",
                |      "AVRO_LOG_LEVEL": "ERROR",
                |      "CONTAINER_LOGGER_MAX_STDOUT_SIZE": "120MB",
                |      "HADOOP_LOG_LEVEL": "ERROR",
                |      "SPARTA_MARATHON_MAIN_CLASS": "com.stratio.sparta.driver.MarathonDriver",
                |      "SECURITY_TLS_ENABLE": "true",
                |      "HADOOP_NAMENODE_KRB_PRINCIPAL": "hdfs/10.200.0.74@DEMO.STRATIO.COM",
                |      "CALICO_NETWORK": "stratio",
                |      "CONTAINER_LOGGER_LOGROTATE_STDERR_OPTIONS": "rotate 10",
                |      "SECURITY_OAUTH2_ENABLE": "true",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING": "zk-0001-zkuserland.service.paas.labs.stratio.com:2181,zk-0002-zkuserland.service.paas.labs.stratio.com:2181,zk-0003-zkuserland.service.paas.labs.stratio.com:2181",
                |      "VAULT_PROTOCOL": "https",
                |      "VAULT_ENABLE": "true",
                |      "SPARTA_DETAIL_CONFIG": "eyJjb25maWciOnsiYXdhaXRXb3JrZmxvd0NoYW5nZVN0YXR1cyI6IjM2MHMiLCJiYWNrdXBzTG9jYXRpb24iOiIvb3B0L3Nkcy9zcGFydGEvYmFja3VwcyIsImNyb3NzZGF0YSI6eyJyZWZlcmVuY2UiOiIvZXRjL3Nkcy9zcGFydGEvcmVmZXJlbmNlLmNvbmYifSwiZHJpdmVyTG9jYXRpb24iOiIvb3B0L3Nkcy9zcGFydGEvZHJpdmVyL3NwYXJ0YS1kcml2ZXIuamFyIiwiZHJpdmVyUGFja2FnZUxvY2F0aW9uIjoiL29wdC9zZHMvc3BhcnRhL2RyaXZlciIsImZyb250ZW5kIjp7InRpbWVvdXQiOjIwMDAwfSwicGx1Z2luc0xvY2F0aW9uIjoicGx1Z2lucyJ9fQ==",
                |      "HADOOP_CONF_FROM_URI": "false",
                |      "VAULT_PORT": "8200",
                |      "SPARTA_ZOOKEEPER_PATH": "/stratio/sparta/sparta-server",
                |      "SPARK_SECURITY_DATASTORE_ENABLE": "true",
                |      "CONTAINER_LOGGER_MAX_STDERR_SIZE": "120MB",
                |      "SPARK_SECURITY_DATASTORE_VAULT_CERT_PATH": "/v1/userland/certificates/sparta-server",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_SESSIONTIMEOUT": "60000",
                |      "SPARTA_LOG_LEVEL": "INFO",
                |      "HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN": "hdfs/*@DEMO.STRATIO.COM",
                |      "HADOOP_CONF_DIR": "/etc/hadoop",
                |      "SPARK_SECURITY_DATASTORE_VAULT_KEY_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
                |      "HADOOP_CONF_URI": "hdfs://10.200.0.74:8020",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYATTEMPTS": "5",
                |      "CALICO_ENABLED": "true",
                |      "VAULT_HOSTS": "vault.service.paas.labs.stratio.com",
                |      "HADOOP_CONF_FROM_DFS_NOT_SECURED": "false",
                |      "CONTAINER_LOGGER_LOGROTATE_STDOUT_OPTIONS": "rotate 10",
                |      "SPARTA_EXECUTION_ID": "dcd33b29-5bd3-4f08-b468-2d8cdeac1ef9",
                |      "USE_DYNAMIC_AUTHENTICATION": "true",
                |      "HADOOP_RPC_PROTECTION": "authentication",
                |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES": "AES/CTR/NoPadding",
                |      "HADOOP_USER_NAME": "sparta-server",
                |      "SPARK_LOG_LEVEL": "ERROR",
                |      "MARATHON_APP_HEAP_SIZE": "-Xmx2048m",
                |      "APPROLE": {
                |        "secret": "role"
                |      }
                |    },
                |    "instances": 1,
                |    "cpus": 1,
                |    "mem": 2560,
                |    "disk": 0,
                |    "gpus": 0,
                |    "executor": "",
                |    "constraints": [],
                |    "uris": [],
                |    "fetch": [],
                |    "storeUrls": [],
                |    "backoffSeconds": 1,
                |    "backoffFactor": 1.15,
                |    "maxLaunchDelaySeconds": 3600,
                |    "container": {
                |      "type": "DOCKER",
                |      "volumes": [{
                |        "containerPath": "/etc/resolv.conf",
                |        "hostPath": "/etc/resolv.conf",
                |        "mode": "RO"
                |      }, {
                |        "containerPath": "/etc/krb5.conf",
                |        "hostPath": "/etc/krb5.conf",
                |        "mode": "RO"
                |      }],
                |      "docker": {
                |        "image": "qa.stratio.com:8443/stratio/sparta:1.9.1-RC1-SNAPSHOTPR311",
                |        "network": "USER",
                |        "portMappings": [{
                |          "containerPort": 4040,
                |          "hostPort": 4040,
                |          "servicePort": 10125,
                |          "protocol": "tcp",
                |          "labels": {}
                |        }],
                |        "privileged": false,
                |        "parameters": [],
                |        "forcePullImage": false
                |      }
                |    },
                |    "healthChecks": [{
                |      "path": "/environment",
                |      "protocol": "HTTP",
                |      "portIndex": 0,
                |      "gracePeriodSeconds": 240,
                |      "intervalSeconds": 60,
                |      "timeoutSeconds": 20,
                |      "maxConsecutiveFailures": 3,
                |      "ignoreHttp1xx": false
                |    }],
                |    "readinessChecks": [],
                |    "dependencies": [],
                |    "upgradeStrategy": {
                |      "minimumHealthCapacity": 1,
                |      "maximumOverCapacity": 1
                |    },
                |    "labels": {
                |      "DCOS_SERVICE_PORT_INDEX": "0",
                |      "DCOS_SERVICE_SCHEME": "http"
                |    },
                |    "acceptedResourceRoles": null,
                |    "ipAddress": {
                |      "groups": [],
                |      "labels": {},
                |      "discovery": {
                |        "ports": []
                |      },
                |      "networkName": "stratio"
                |    },
                |    "version": "2018-01-04T11:42:32.269Z",
                |    "residency": null,
                |    "secrets": {
                |      "role": {
                |        "source": "open"
                |      }
                |    },
                |    "taskKillGracePeriodSeconds": null,
                |    "versionInfo": {
                |      "lastScalingAt": "2018-01-04T11:42:32.269Z",
                |      "lastConfigChangeAt": "2018-01-04T11:42:32.269Z"
                |    }
                |  }],
                |  "groups": [{
                |    "id": "/sparta/sparta-server/workflows/group1",
                |    "dependencies": [],
                |    "version": "2018-01-04T12:21:46.218Z",
                |    "apps": [],
                |    "groups": [{
                |      "id": "/sparta/sparta-server/workflows/group1/nameworkflow",
                |      "dependencies": [],
                |      "version": "2018-01-04T12:21:46.218Z",
                |      "apps": [{
                |        "id": "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1",
                |        "cmd": null,
                |        "args": null,
                |        "user": null,
                |        "env": {
                |          "HDFS_KEYTAB_RELOAD_TIME": "23h",
                |          "CROSSDATA_SERVER_CONFIG_SPARK_EXECUTOR_CORES": "1",
                |          "SPARTA_TIMEOUT_CROSSDATA_QUERIES": "19",
                |          "SPARTA_PLUGIN_ZOOKEEPER_WATCHERS": "true",
                |          "SPARTA_MARATHON_FORCE_PULL_IMAGE": "false",
                |          "ZOOKEEPER_LOG_LEVEL": "OFF",
                |          "SPARTA_PLUGIN_LDAP_HOST": "idp.integration.labs.stratio.com",
                |          "OAUTH2_SSL_AUTHORIZE": "https://megadev.labs.stratio.com:9005/sso/oauth2.0/authorize",
                |          "OAUTH2_URL_PROFILE": "https://megadev.labs.stratio.com:9005/sso/oauth2.0/profile",
                |          "CROSSDATA_SERVER_CONFIG_SPARK_IMAGE": "qa.stratio.com/stratio/stratio-spark:2.2.0.5",
                |          "SPARTA_HEAP_MINIMUM_SIZE": "-Xms1024m",
                |          "SECURITY_MESOS_ENABLE": "true",
                |          "SPARTA_MARATHON_INTERVAL_SECONDS": "60",
                |          "HADOOP_CONF_FROM_DFS": "true",
                |          "SPARTA_MARATHON_GRACEPERIODS_SECONDS": "240",
                |          "CROSSDATA_CORE_CATALOG_CLASS": "org.apache.spark.sql.crossdata.catalyst.catalog.persistent.zookeeper.ZookeeperCatalog",
                |          "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONTIMEOUT": "15000",
                |          "SPARTA_ZOOKEEPER_CONNECTION_STRING": "zk-0001-zkuserland.service.paas.labs.stratio.com:2181,zk-0002-zkuserland.service.paas.labs.stratio.com:2181,zk-0003-zkuserland.service.paas.labs.stratio.com:2181",
                |          "HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH": "128",
                |          "SPARTA_PLUGIN_LDAP_GROUP_DOMAIN": "ou=Groups,dc=stratio,dc=com",
                |          "CORE_SITE_FROM_URI": "false",
                |          "CROSSDATA_LOG_LEVEL": "ERROR",
                |          "OAUTH2_URL_LOGOUT": "https://megadev.labs.stratio.com:9005/sso/logout",
                |          "HDFS_KEYTAB_RELOAD": "false",
                |          "HADOOP_SECURITY_AUTH": "kerberos",
                |          "CROSSDATA_HDFS_DELEGATION_TOKEN_DISABLE_CACHE": "true",
                |          "SPARTA_PLUGIN_ZK_CONNECT": "gosec1.node.paas.labs.stratio.com:2181,gosec2.node.paas.labs.stratio.com:2181,gosec3.node.paas.labs.stratio.com:2181",
                |          "SERVICE_LOG_LEVEL": "ERROR",
                |          "HADOOP_FS_DEFAULT_NAME": "10.200.0.74",
                |          "CROSSDATA_SERVER_SPARK_UI_ENABLED": "true",
                |          "MARATHON_SSO_URI": "https://megadev.labs.stratio.com:9005/sso",
                |          "SPARTA_DOCKER_IMAGE": "qa.stratio.com:8443/stratio/sparta:2.0.0-SNAPSHOTPR308",
                |          "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYINTERVAL": "10000",
                |          "SECURITY_TRUSTSTORE_ENABLE": "true",
                |          "SPARTA_MARATHON_PRIVILEGED": "false",
                |          "SPARTA_PLUGIN_LDAP_PORT": "636",
                |          "SPARTA_MARATHON_MAX_FAILURES": "3",
                |          "SECURITY_KRB_ENABLE": "true",
                |          "HADOOP_SECURITY_TOKEN_USE_IP": "false",
                |          "ENABLE_GOSEC_AUTH": "true",
                |          "PARQUET_LOG_LEVEL": "ERROR",
                |          "CROSSDATA_SERVER_SPARK_DATASTORE_SSL_ENABLE": "true",
                |          "CROSSDATA_CORE_CATALOG_PREFIX": "spartaCluster",
                |          "SPARTA_MARATHON_MESOSPHERE_PACKAGES": "/opt/mesosphere/packages",
                |          "CROSSDATA_SERVER_SPARK_EXECUTOR_CA_NAME": "ca",
                |          "HADOOP_DFS_ENCRYPT_DATA_TRANSFER": "true",
                |          "SECURITY_MARATHON_ENABLED": "true",
                |          "FRONTEND_TIMEOUT": "20000",
                |          "CROSSDATA_SERVER_SPARK_EXECUTOR_APP_NAME": "sparta-server",
                |          "SPARTA_AWAIT_POLICY_CHANGE_STATUS": "360s",
                |          "MARATHON_SSO_CLIENT_ID": "adminrouter_paas-master-1.node.paas.labs.stratio.com",
                |          "SPARTA_PLUGIN_KAFKA_TOPIC": "audit",
                |          "SPARTA_PLUGIN_LDAP_USER_DOMAIN": "ou=People,dc=stratio,dc=com",
                |          "SPARTA_PLUGIN_SSO_PRINCIPAL": "gosec-sso",
                |          "CROSSDATA_SERVER_CONFIG_SPARK_EXECUTOR_MEMORY": "1G",
                |          "AVRO_LOG_LEVEL": "ERROR",
                |          "CROSSDATA_STORAGE_PATH": "/tmp",
                |          "SPARTA_MAX_PERM_SIZE": "",
                |          "CONTAINER_LOGGER_MAX_STDOUT_SIZE": "120MB",
                |          "HADOOP_LOG_LEVEL": "ERROR",
                |          "CROSSDATA_CORE_ENABLE_CATALOG": "true",
                |          "SECURITY_TLS_ENABLE": "true",
                |          "SPARTA_ZOOKEEPER_SESSION_TIMEOUT": "60000",
                |          "HADOOP_NAMENODE_KRB_PRINCIPAL": "hdfs/10.200.0.74@DEMO.STRATIO.COM",
                |          "SPARTA_MARATHON_MESOSPHERE_LIB": "/opt/mesosphere/lib",
                |          "APPROLENAME": "open",
                |          "CALICO_NETWORK": "stratio",
                |          "CONTAINER_LOGGER_LOGROTATE_STDERR_OPTIONS": "rotate 10",
                |          "SECURITY_OAUTH2_ENABLE": "true",
                |          "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING": "zk-0001-zkuserland.service.paas.labs.stratio.com:2181,zk-0002-zkuserland.service.paas.labs.stratio.com:2181,zk-0003-zkuserland.service.paas.labs.stratio.com:2181",
                |          "VAULT_ENABLE": "true",
                |          "SPARTA_PLUGIN_INSTANCE": "sparta-server",
                |          "MARATHON_SSO_REDIRECT_URI": "https://megadev.labs.stratio.com/acs/api/v1/auth/login",
                |          "HADOOP_CONF_FROM_URI": "false",
                |          "VAULT_PORT": "8200",
                |          "CROSSDATA_SERVER_CONFIG_SPARK_DRIVER_CORES": "1",
                |          "SPARTA_ZOOKEEPER_PATH": "",
                |          "CONTAINER_LOGGER_MAX_STDERR_SIZE": "120MB",
                |          "SPARTA_MARATHON_TIMEOUT_SECONDS": "20",
                |          "SPARTA_ZOOKEEPER_RETRY_INTERVAL": "10000",
                |          "SPARTA_HEAP_SIZE": "-Xmx2048m",
                |          "CROSSDATA_CORE_CATALOG_ZOOKEEPER_SESSIONTIMEOUT": "60000",
                |          "SPRAY_CAN_SERVER_SSL_ENCRYPTION": "true",
                |          "OAUTH2_COOKIE_NAME": "user",
                |          "SPARTA_TIMEOUT_API_CALLS": "19",
                |          "SPARTA_LOG_LEVEL": "INFO",
                |          "HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN": "hdfs/*@DEMO.STRATIO.COM",
                |          "HADOOP_CONF_URI": "hdfs://10.200.0.74:8020",
                |          "SPARTA_ZOOKEEPER_RETRY_ATEMPTS": "5",
                |          "SPARTA_MARATHON_OS_MEMORY": "1024",
                |          "MARATHON_TIKI_TAKKA_MARATHON_API_VERSION": "v2",
                |          "SPARTA_PLUGIN_KAFKA_BOOTSTRAP": "gosec1.node.paas.labs.stratio.com:9092,gosec2.node.paas.labs.stratio.com:9092,gosec3.node.paas.labs.stratio.com:9092",
                |          "OAUTH2_URL_ACCESS_TOKEN": "https://megadev.labs.stratio.com:9005/sso/oauth2.0/accessToken",
                |          "RUN_MODE": "production",
                |          "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYATTEMPTS": "5",
                |          "CALICO_ENABLED": "true",
                |          "VAULT_HOSTS": "vault.service.paas.labs.stratio.com",
                |          "SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT": "15000",
                |          "HADOOP_CONF_FROM_DFS_NOT_SECURED": "false",
                |          "CONTAINER_LOGGER_LOGROTATE_STDOUT_OPTIONS": "rotate 10",
                |          "CROSSDATA_SERVER_CONFIG_SPARK_DRIVER_MEMORY": "1G",
                |          "USE_DYNAMIC_AUTHENTICATION": "true",
                |          "OAUTH2_URL_CALLBACK": "https://sparta.megadev.labs.stratio.com/sparta-server/login",
                |          "CROSSDATA_SERVER_CONFIG_SPARK_MASTER": "local[*]",
                |          "HADOOP_RPC_PROTECTION": "authentication",
                |          "HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES": "AES/CTR/NoPadding",
                |          "HADOOP_USER_NAME": "sparta-server",
                |          "SPARK_LOG_LEVEL": "ERROR",
                |          "OAUTH2_URL_ON_LOGIN_GO_TO": "/sparta-server",
                |          "CROSSDATA_SERVER_CONFIG_SPARK_CORES_MAX": "1",
                |          "SPARTA_PLUGIN_MGMT_PRINCIPAL": "gosec-management",
                |          "MARATHON_TIKI_TAKKA_MARATHON_URI": "https://megadev.labs.stratio.com/service/marathon",
                |          "APPROLE": {
                |            "secret": "role"
                |          },
                |          "MAX_OPEN_FILES": "65535"
                |        },
                |        "instances": 1,
                |        "cpus": 2,
                |        "mem": 2048,
                |        "disk": 0,
                |        "gpus": 0,
                |        "executor": "",
                |        "constraints": [],
                |        "uris": [],
                |        "fetch": [],
                |        "storeUrls": [],
                |        "backoffSeconds": 1,
                |        "backoffFactor": 1.15,
                |        "maxLaunchDelaySeconds": 3600,
                |        "container": {
                |          "type": "DOCKER",
                |          "volumes": [],
                |          "docker": {
                |            "image": "qa.stratio.com:8443/stratio/sparta:2.0.0-SNAPSHOTPR308",
                |            "network": "USER",
                |            "portMappings": [{
                |              "containerPort": 9090,
                |              "servicePort": 10115,
                |              "protocol": "tcp",
                |              "name": "spartaapi",
                |              "labels": {}
                |            }, {
                |              "containerPort": 4040,
                |              "servicePort": 10119,
                |              "protocol": "tcp",
                |              "name": "sparkapi",
                |              "labels": {}
                |            }],
                |            "privileged": false,
                |            "parameters": [{
                |              "key": "volume",
                |              "value": "/etc/krb5.conf:/etc/krb5.conf:ro"
                |            }],
                |            "forcePullImage": false
                |          }
                |        },
                |        "healthChecks": [{
                |          "protocol": "TCP",
                |          "portIndex": 0,
                |          "gracePeriodSeconds": 300,
                |          "intervalSeconds": 15,
                |          "timeoutSeconds": 1,
                |          "maxConsecutiveFailures": 3,
                |          "ignoreHttp1xx": false
                |        }],
                |        "readinessChecks": [],
                |        "dependencies": [],
                |        "upgradeStrategy": {
                |          "minimumHealthCapacity": 1,
                |          "maximumOverCapacity": 1
                |        },
                |        "labels": {
                |          "DCOS_PACKAGE_RELEASE": "7",
                |          "HAPROXY_0_REDIRECT_TO_HTTPS": "true",
                |          "DCOS_SERVICE_SCHEME": "https",
                |          "HAPROXY_0_STICKY": "false",
                |          "DCOS_PACKAGE_SOURCE": "http://iodo.stratio.com:8080/repo",
                |          "HAPROXY_GROUP": "external",
                |          "DCOS_PACKAGE_METADATA": "eyJwYWNrYWdpbmdWZXJzaW9uIjoiMy4wIiwibmFtZSI6InNwYXJ0YSIsInZlcnNpb24iOiIxLjcuOCIsIm1haW50YWluZXIiOiJzcGFydGFAc3RyYXRpby5jb20iLCJkZXNjcmlwdGlvbiI6IlJlYWwgVGltZSBhbmFseXRpY3MgZW5naW5lIGJhc2VkIG9uIEFwYWNoZSBTcGFyayIsInRhZ3MiOlsic3BhcnRhIiwic3RyZWFtaW5nIiwic3BhcmsiLCJzcWwiLCJzdHJhdGlvIiwiYW5hbHl0aWNzIiwiZXRsIiwiYWdncmVnYXRpb24iLCJyZWFsLXRpbWUiLCJrYWZrYSIsImNhc3NhbmRyYSIsIm1vbmdvZGIiLCJlbGFzdGljc2VhcmNoIiwiZmx1bWUiLCJoZGZzIiwiaGFkb29wIiwiZGF0YXNvdXJjZXMiLCJzdHJlYW0iLCJkYXRhIiwib2xhcCIsImN1YmUiLCJsYW1iZGEiLCJ3b3JrZmxvdyIsImNlcCIsImV2ZW50Il0sInNlbGVjdGVkIjp0cnVlLCJzY20iOiJodHRwczovL2dpdGh1Yi5jb20vU3RyYXRpby9zcGFydGEtd29ya2Zsb3ciLCJmcmFtZXdvcmsiOmZhbHNlLCJwcmVJbnN0YWxsTm90ZXMiOiJQcmVwYXJpbmcgdG8gaW5zdGFsbCBTdHJhdGlvIFNwYXJ0YSIsInBvc3RJbnN0YWxsTm90ZXMiOiJTdHJhdGlvIFNwYXJ0YSBpbnN0YWxsZWQgc3VjY2Vzc2Z1bGx5IiwicG9zdFVuaW5zdGFsbE5vdGVzIjoiU3RyYXRpbyBTcGFydGEgdW5pbnN0YWxsZWQgc3VjY2Vzc2Z1bGx5IiwibGljZW5zZXMiOlt7Im5hbWUiOiJTcGFydGEiLCJ1cmwiOiJodHRwczovL2dpdGh1Yi5jb20vU3RyYXRpby9zcGFydGEtd29ya2Zsb3cvYmxvYi9tYXN0ZXIvTElDRU5TRS50eHQifV0sImltYWdlcyI6eyJpY29uLXNtYWxsIjoiaHR0cDovL2Fzc2V0cy5zdHJhdGlvLmNvbS9sb2dvcy9sb2dvX3N0cmF0aW9fdW5pdmVyc2UucG5nIiwiaWNvbi1tZWRpdW0iOiJodHRwOi8vYXNzZXRzLnN0cmF0aW8uY29tL2xvZ29zL2xvZ29fc3RyYXRpb191bml2ZXJzZS5wbmciLCJpY29uLWxhcmdlIjoiaHR0cDovL2Fzc2V0cy5zdHJhdGlvLmNvbS9sb2dvcy9sb2dvX3N0cmF0aW9fdW5pdmVyc2UucG5nIn19",
                |          "DCOS_PACKAGE_REGISTRY_VERSION": "3.0",
                |          "HAPROXY_0_BACKEND_SERVER_OPTIONS": "server {serverName} {host_ipv4}:{port}{cookieOptions}{healthCheckOptions}{otherOptions} ssl verify none\n",
                |          "DCOS_SERVICE_NAME": "sparta-server",
                |          "HAPROXY_1_BACKEND_SERVER_OPTIONS": "server {serverName} {host_ipv4}:{port}{cookieOptions}{healthCheckOptions}{otherOptions} ssl verify none",
                |          "DCOS_PACKAGE_FRAMEWORK_NAME": "sparta-server",
                |          "DCOS_SERVICE_PORT_INDEX": "0",
                |          "HAPROXY_1_REDIRECT_TO_HTTPS": "true",
                |          "HAPROXY_0_PATH": "/sparta-server",
                |          "HAPROXY_1_PATH": "/workflows-sparta-server",
                |          "DCOS_PACKAGE_VERSION": "1.7.8",
                |          "HAPROXY_1_STICKY": "false",
                |          "DCOS_PACKAGE_NAME": "sparta",
                |          "HAPROXY_1_VHOST": "sparta.megadev.labs.stratio.com",
                |          "DCOS_PACKAGE_IS_FRAMEWORK": "false",
                |          "HAPROXY_0_VHOST": "sparta.megadev.labs.stratio.com"
                |        },
                |        "acceptedResourceRoles": null,
                |        "ipAddress": {
                |          "groups": [],
                |          "labels": {},
                |          "discovery": {
                |            "ports": []
                |          },
                |          "networkName": "stratio"
                |        },
                |        "version": "2018-01-04T12:21:46.218Z",
                |        "residency": null,
                |        "secrets": {
                |          "role": {
                |            "source": "open"
                |          }
                |        },
                |        "taskKillGracePeriodSeconds": null,
                |        "versionInfo": {
                |          "lastScalingAt": "2018-01-04T12:21:46.218Z",
                |          "lastConfigChangeAt": "2018-01-04T12:21:46.218Z"
                |        }
                |      }],
                |      "groups": []
                |    }]
                |  }]
                |}""".stripMargin

  val appId=
    """{
      |  "app": {
      |    "id": "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1",
      |    "cmd": null,
      |    "args": null,
      |    "user": null,
      |    "env": {
      |      "HDFS_KEYTAB_RELOAD_TIME": "23h",
      |      "CROSSDATA_SERVER_CONFIG_SPARK_EXECUTOR_CORES": "1",
      |      "SPARTA_TIMEOUT_CROSSDATA_QUERIES": "19",
      |      "SPARTA_PLUGIN_ZOOKEEPER_WATCHERS": "true",
      |      "SPARTA_MARATHON_FORCE_PULL_IMAGE": "false",
      |      "ZOOKEEPER_LOG_LEVEL": "OFF",
      |      "SPARTA_PLUGIN_LDAP_HOST": "idp.integration.labs.stratio.com",
      |      "HDFS_SECURITY_ENABLED": "true",
      |      "OAUTH2_SSL_AUTHORIZE": "https://megadev.labs.stratio.com:9005/sso/oauth2.0/authorize",
      |      "OAUTH2_URL_PROFILE": "https://megadev.labs.stratio.com:9005/sso/oauth2.0/profile",
      |      "CROSSDATA_SERVER_CONFIG_SPARK_IMAGE": "qa.stratio.com/stratio/stratio-spark:2.2.0.5",
      |      "SPARTA_HEAP_MINIMUM_SIZE": "-Xms1024m",
      |      "SECURITY_MESOS_ENABLE": "true",
      |      "SPARTA_MARATHON_INTERVAL_SECONDS": "60",
      |      "HADOOP_CONF_FROM_DFS": "true",
      |      "SPARTA_MARATHON_GRACEPERIODS_SECONDS": "240",
      |      "CROSSDATA_CORE_CATALOG_CLASS": "org.apache.spark.sql.crossdata.catalyst.catalog.persistent.zookeeper.ZookeeperCatalog",
      |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONTIMEOUT": "15000",
      |      "SPARTA_ZOOKEEPER_CONNECTION_STRING": "zk-0001-zkuserland.service.paas.labs.stratio.com:2181,zk-0002-zkuserland.service.paas.labs.stratio.com:2181,zk-0003-zkuserland.service.paas.labs.stratio.com:2181",
      |      "HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH": "128",
      |      "SPARTA_PLUGIN_LDAP_GROUP_DOMAIN": "ou=Groups,dc=stratio,dc=com",
      |      "CORE_SITE_FROM_URI": "false",
      |      "CROSSDATA_LOG_LEVEL": "ERROR",
      |      "OAUTH2_URL_LOGOUT": "https://megadev.labs.stratio.com:9005/sso/logout",
      |      "HDFS_KEYTAB_RELOAD": "false",
      |      "HADOOP_SECURITY_AUTH": "kerberos",
      |      "CROSSDATA_HDFS_DELEGATION_TOKEN_DISABLE_CACHE": "true",
      |      "SPARTA_PLUGIN_ZK_CONNECT": "gosec1.node.paas.labs.stratio.com:2181,gosec2.node.paas.labs.stratio.com:2181,gosec3.node.paas.labs.stratio.com:2181",
      |      "SERVICE_LOG_LEVEL": "ERROR",
      |      "HADOOP_FS_DEFAULT_NAME": "10.200.0.74",
      |      "CROSSDATA_SERVER_SPARK_UI_ENABLED": "true",
      |      "MARATHON_SSO_URI": "https://megadev.labs.stratio.com:9005/sso",
      |      "SPARTA_DOCKER_IMAGE": "qa.stratio.com:8443/stratio/sparta:2.0.0-SNAPSHOTPR308",
      |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYINTERVAL": "10000",
      |      "SECURITY_TRUSTSTORE_ENABLE": "true",
      |      "SPARTA_MARATHON_PRIVILEGED": "false",
      |      "SPARTA_PLUGIN_LDAP_PORT": "636",
      |      "SPARTA_MARATHON_MAX_FAILURES": "3",
      |      "SECURITY_KRB_ENABLE": "true",
      |      "HADOOP_SECURITY_TOKEN_USE_IP": "false",
      |      "ENABLE_GOSEC_AUTH": "true",
      |      "PARQUET_LOG_LEVEL": "ERROR",
      |      "CROSSDATA_SERVER_SPARK_DATASTORE_SSL_ENABLE": "true",
      |      "CROSSDATA_CORE_CATALOG_PREFIX": "spartaCluster",
      |      "SPARTA_MARATHON_MESOSPHERE_PACKAGES": "/opt/mesosphere/packages",
      |      "CROSSDATA_SERVER_SPARK_EXECUTOR_CA_NAME": "ca",
      |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER": "true",
      |      "SECURITY_MARATHON_ENABLED": "true",
      |      "FRONTEND_TIMEOUT": "20000",
      |      "CROSSDATA_SERVER_SPARK_EXECUTOR_APP_NAME": "sparta-server",
      |      "SPARTA_AWAIT_POLICY_CHANGE_STATUS": "360s",
      |      "MARATHON_SSO_CLIENT_ID": "adminrouter_paas-master-1.node.paas.labs.stratio.com",
      |      "SPARTA_PLUGIN_KAFKA_TOPIC": "audit",
      |      "SPARTA_PLUGIN_LDAP_USER_DOMAIN": "ou=People,dc=stratio,dc=com",
      |      "SPARTA_PLUGIN_SSO_PRINCIPAL": "gosec-sso",
      |      "CROSSDATA_SERVER_CONFIG_SPARK_EXECUTOR_MEMORY": "1G",
      |      "AVRO_LOG_LEVEL": "ERROR",
      |      "CROSSDATA_STORAGE_PATH": "/tmp",
      |      "SPARTA_MAX_PERM_SIZE": "",
      |      "CONTAINER_LOGGER_MAX_STDOUT_SIZE": "120MB",
      |      "HADOOP_LOG_LEVEL": "ERROR",
      |      "CROSSDATA_CORE_ENABLE_CATALOG": "true",
      |      "SECURITY_TLS_ENABLE": "true",
      |      "SPARTA_ZOOKEEPER_SESSION_TIMEOUT": "60000",
      |      "HADOOP_NAMENODE_KRB_PRINCIPAL": "hdfs/10.200.0.74@DEMO.STRATIO.COM",
      |      "SPARTA_MARATHON_MESOSPHERE_LIB": "/opt/mesosphere/lib",
      |      "APPROLENAME": "open",
      |      "CALICO_NETWORK": "stratio",
      |      "CONTAINER_LOGGER_LOGROTATE_STDERR_OPTIONS": "rotate 10",
      |      "SECURITY_OAUTH2_ENABLE": "true",
      |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING": "zk-0001-zkuserland.service.paas.labs.stratio.com:2181,zk-0002-zkuserland.service.paas.labs.stratio.com:2181,zk-0003-zkuserland.service.paas.labs.stratio.com:2181",
      |      "VAULT_ENABLE": "true",
      |      "SPARTA_PLUGIN_INSTANCE": "sparta-server",
      |      "MARATHON_SSO_REDIRECT_URI": "https://megadev.labs.stratio.com/acs/api/v1/auth/login",
      |      "HADOOP_CONF_FROM_URI": "false",
      |      "VAULT_PORT": "8200",
      |      "CROSSDATA_SERVER_CONFIG_SPARK_DRIVER_CORES": "1",
      |      "SPARTA_ZOOKEEPER_PATH": "",
      |      "CONTAINER_LOGGER_MAX_STDERR_SIZE": "120MB",
      |      "SPARTA_MARATHON_TIMEOUT_SECONDS": "20",
      |      "SPARTA_ZOOKEEPER_RETRY_INTERVAL": "10000",
      |      "SPARTA_HEAP_SIZE": "-Xmx2048m",
      |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_SESSIONTIMEOUT": "60000",
      |      "SPRAY_CAN_SERVER_SSL_ENCRYPTION": "true",
      |      "OAUTH2_COOKIE_NAME": "user",
      |      "SPARTA_TIMEOUT_API_CALLS": "19",
      |      "SPARTA_LOG_LEVEL": "INFO",
      |      "HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN": "hdfs/*@DEMO.STRATIO.COM",
      |      "HADOOP_CONF_URI": "hdfs://10.200.0.74:8020",
      |      "SPARTA_ZOOKEEPER_RETRY_ATEMPTS": "5",
      |      "SPARTA_MARATHON_OS_MEMORY": "1024",
      |      "MARATHON_TIKI_TAKKA_MARATHON_API_VERSION": "v2",
      |      "SPARTA_PLUGIN_KAFKA_BOOTSTRAP": "gosec1.node.paas.labs.stratio.com:9092,gosec2.node.paas.labs.stratio.com:9092,gosec3.node.paas.labs.stratio.com:9092",
      |      "OAUTH2_URL_ACCESS_TOKEN": "https://megadev.labs.stratio.com:9005/sso/oauth2.0/accessToken",
      |      "RUN_MODE": "production",
      |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYATTEMPTS": "5",
      |      "CALICO_ENABLED": "true",
      |      "VAULT_HOSTS": "vault.service.paas.labs.stratio.com",
      |      "SPARTA_ZOOKEEPER_CONNECTION_TIMEOUT": "15000",
      |      "HADOOP_CONF_FROM_DFS_NOT_SECURED": "false",
      |      "CONTAINER_LOGGER_LOGROTATE_STDOUT_OPTIONS": "rotate 10",
      |      "CROSSDATA_SERVER_CONFIG_SPARK_DRIVER_MEMORY": "1G",
      |      "USE_DYNAMIC_AUTHENTICATION": "true",
      |      "OAUTH2_URL_CALLBACK": "https://sparta.megadev.labs.stratio.com/sparta-server/login",
      |      "CROSSDATA_SERVER_CONFIG_SPARK_MASTER": "local[*]",
      |      "HADOOP_RPC_PROTECTION": "authentication",
      |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES": "AES/CTR/NoPadding",
      |      "HADOOP_USER_NAME": "sparta-server",
      |      "SPARK_LOG_LEVEL": "ERROR",
      |      "OAUTH2_URL_ON_LOGIN_GO_TO": "/sparta-server",
      |      "CROSSDATA_SERVER_CONFIG_SPARK_CORES_MAX": "1",
      |      "SPARTA_PLUGIN_MGMT_PRINCIPAL": "gosec-management",
      |      "MARATHON_TIKI_TAKKA_MARATHON_URI": "https://megadev.labs.stratio.com/service/marathon",
      |      "APPROLE": {
      |        "secret": "role"
      |      },
      |      "MAX_OPEN_FILES": "65535"
      |    },
      |    "instances": 1,
      |    "cpus": 2,
      |    "mem": 2048,
      |    "disk": 0,
      |    "gpus": 0,
      |    "executor": "",
      |    "constraints": [],
      |    "uris": [],
      |    "fetch": [],
      |    "storeUrls": [],
      |    "backoffSeconds": 1,
      |    "backoffFactor": 1.15,
      |    "maxLaunchDelaySeconds": 3600,
      |    "container": {
      |      "type": "DOCKER",
      |      "volumes": [],
      |      "docker": {
      |        "image": "qa.stratio.com:8443/stratio/sparta:2.0.0-SNAPSHOTPR308",
      |        "network": "USER",
      |        "portMappings": [{
      |          "containerPort": 9090,
      |          "servicePort": 10115,
      |          "protocol": "tcp",
      |          "name": "spartaapi",
      |          "labels": {}
      |        }, {
      |          "containerPort": 4040,
      |          "servicePort": 10119,
      |          "protocol": "tcp",
      |          "name": "sparkapi",
      |          "labels": {}
      |        }],
      |        "privileged": false,
      |        "parameters": [{
      |          "key": "volume",
      |          "value": "/etc/krb5.conf:/etc/krb5.conf:ro"
      |        }],
      |        "forcePullImage": false
      |      }
      |    },
      |    "healthChecks": [{
      |      "protocol": "TCP",
      |      "portIndex": 0,
      |      "gracePeriodSeconds": 300,
      |      "intervalSeconds": 15,
      |      "timeoutSeconds": 1,
      |      "maxConsecutiveFailures": 3,
      |      "ignoreHttp1xx": false
      |    }],
      |    "readinessChecks": [],
      |    "dependencies": [],
      |    "upgradeStrategy": {
      |      "minimumHealthCapacity": 1,
      |      "maximumOverCapacity": 1
      |    },
      |    "labels": {
      |      "DCOS_PACKAGE_RELEASE": "7",
      |      "HAPROXY_0_REDIRECT_TO_HTTPS": "true",
      |      "DCOS_SERVICE_SCHEME": "https",
      |      "HAPROXY_0_STICKY": "false",
      |      "DCOS_PACKAGE_SOURCE": "http://iodo.stratio.com:8080/repo",
      |      "HAPROXY_GROUP": "external",
      |      "DCOS_PACKAGE_METADATA": "eyJwYWNrYWdpbmdWZXJzaW9uIjoiMy4wIiwibmFtZSI6InNwYXJ0YSIsInZlcnNpb24iOiIxLjcuOCIsIm1haW50YWluZXIiOiJzcGFydGFAc3RyYXRpby5jb20iLCJkZXNjcmlwdGlvbiI6IlJlYWwgVGltZSBhbmFseXRpY3MgZW5naW5lIGJhc2VkIG9uIEFwYWNoZSBTcGFyayIsInRhZ3MiOlsic3BhcnRhIiwic3RyZWFtaW5nIiwic3BhcmsiLCJzcWwiLCJzdHJhdGlvIiwiYW5hbHl0aWNzIiwiZXRsIiwiYWdncmVnYXRpb24iLCJyZWFsLXRpbWUiLCJrYWZrYSIsImNhc3NhbmRyYSIsIm1vbmdvZGIiLCJlbGFzdGljc2VhcmNoIiwiZmx1bWUiLCJoZGZzIiwiaGFkb29wIiwiZGF0YXNvdXJjZXMiLCJzdHJlYW0iLCJkYXRhIiwib2xhcCIsImN1YmUiLCJsYW1iZGEiLCJ3b3JrZmxvdyIsImNlcCIsImV2ZW50Il0sInNlbGVjdGVkIjp0cnVlLCJzY20iOiJodHRwczovL2dpdGh1Yi5jb20vU3RyYXRpby9zcGFydGEtd29ya2Zsb3ciLCJmcmFtZXdvcmsiOmZhbHNlLCJwcmVJbnN0YWxsTm90ZXMiOiJQcmVwYXJpbmcgdG8gaW5zdGFsbCBTdHJhdGlvIFNwYXJ0YSIsInBvc3RJbnN0YWxsTm90ZXMiOiJTdHJhdGlvIFNwYXJ0YSBpbnN0YWxsZWQgc3VjY2Vzc2Z1bGx5IiwicG9zdFVuaW5zdGFsbE5vdGVzIjoiU3RyYXRpbyBTcGFydGEgdW5pbnN0YWxsZWQgc3VjY2Vzc2Z1bGx5IiwibGljZW5zZXMiOlt7Im5hbWUiOiJTcGFydGEiLCJ1cmwiOiJodHRwczovL2dpdGh1Yi5jb20vU3RyYXRpby9zcGFydGEtd29ya2Zsb3cvYmxvYi9tYXN0ZXIvTElDRU5TRS50eHQifV0sImltYWdlcyI6eyJpY29uLXNtYWxsIjoiaHR0cDovL2Fzc2V0cy5zdHJhdGlvLmNvbS9sb2dvcy9sb2dvX3N0cmF0aW9fdW5pdmVyc2UucG5nIiwiaWNvbi1tZWRpdW0iOiJodHRwOi8vYXNzZXRzLnN0cmF0aW8uY29tL2xvZ29zL2xvZ29fc3RyYXRpb191bml2ZXJzZS5wbmciLCJpY29uLWxhcmdlIjoiaHR0cDovL2Fzc2V0cy5zdHJhdGlvLmNvbS9sb2dvcy9sb2dvX3N0cmF0aW9fdW5pdmVyc2UucG5nIn19",
      |      "DCOS_PACKAGE_REGISTRY_VERSION": "3.0",
      |      "HAPROXY_0_BACKEND_SERVER_OPTIONS": "server {serverName} {host_ipv4}:{port}{cookieOptions}{healthCheckOptions}{otherOptions} ssl verify none\n",
      |      "DCOS_SERVICE_NAME": "sparta-server",
      |      "HAPROXY_1_BACKEND_SERVER_OPTIONS": "server {serverName} {host_ipv4}:{port}{cookieOptions}{healthCheckOptions}{otherOptions} ssl verify none",
      |      "DCOS_PACKAGE_FRAMEWORK_NAME": "sparta-server",
      |      "DCOS_SERVICE_PORT_INDEX": "0",
      |      "HAPROXY_1_REDIRECT_TO_HTTPS": "true",
      |      "HAPROXY_0_PATH": "/sparta-server",
      |      "HAPROXY_1_PATH": "/workflows-sparta-server",
      |      "DCOS_PACKAGE_VERSION": "1.7.8",
      |      "HAPROXY_1_STICKY": "false",
      |      "DCOS_PACKAGE_NAME": "sparta",
      |      "HAPROXY_1_VHOST": "sparta.megadev.labs.stratio.com",
      |      "DCOS_PACKAGE_IS_FRAMEWORK": "false",
      |      "HAPROXY_0_VHOST": "sparta.megadev.labs.stratio.com"
      |    },
      |    "acceptedResourceRoles": null,
      |    "ipAddress": {
      |      "groups": [],
      |      "labels": {},
      |      "discovery": {
      |        "ports": []
      |      },
      |      "networkName": "stratio"
      |    },
      |    "version": "2018-01-04T12:21:46.218Z",
      |    "residency": null,
      |    "secrets": {
      |      "role": {
      |        "source": "open"
      |      }
      |    },
      |    "taskKillGracePeriodSeconds": null,
      |    "versionInfo": {
      |      "lastScalingAt": "2018-01-04T12:21:46.218Z",
      |      "lastConfigChangeAt": "2018-01-04T12:21:46.218Z"
      |    },
      |    "tasksStaged": 0,
      |    "tasksRunning": 1,
      |    "tasksHealthy": 1,
      |    "tasksUnhealthy": 0,
      |    "deployments": [],
      |    "tasks": [{
      |      "id": "sparta_sparta-server_workflows_group1_nameworkflow_nameworkflow-v1.d4f8efb4-f149-11e7-a3fc-70b3d5800001",
      |      "slaveId": "c83bbabd-8467-4927-84dc-abcdae274d60-S10",
      |      "host": "10.200.0.55",
      |      "state": "TASK_RUNNING",
      |      "startedAt": "2018-01-04T12:24:51.098Z",
      |      "stagedAt": "2018-01-04T12:21:46.736Z",
      |      "ports": [0,1,2,4040],
      |      "version": "2018-01-04T12:21:46.218Z",
      |      "ipAddresses": [{
      |        "ipAddress": "172.25.159.108",
      |        "protocol": "IPv4"
      |      }],
      |      "appId": "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1",
      |      "healthCheckResults": [{
      |        "alive": true,
      |        "consecutiveFailures": 0,
      |        "firstSuccess": "2018-01-04T12:26:21.714Z",
      |        "lastFailure": null,
      |        "lastSuccess": "2018-01-04T12:31:37.227Z",
      |        "lastFailureCause": null,
      |        "taskId": "sparta_sparta-server_workflows_group1_nameworkflow_nameworkflow-v1.d4f8efb4-f149-11e7-a3fc-70b3d5800001"
      |      }]
      |    }]
      |  }
      |}
    """.stripMargin


  "retrieveAppId" should{
    "retrieve two appID" when {
      "it parses the testJson" in {
        val actualResult = testNginx.extractAppsId(jsonApp).get
        val expectedResult =
          Seq("/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1",
          "/sparta/sparta-server/workflows/testinput-kafka2")
        assert(actualResult.forall(item => expectedResult.contains(item)))
      }
    }
  }

  "retrieveAppId" should{
    "retrieve an empty list" when {
      "it parses an empty JSON" in {
        val actualResult = testNginx.extractAppsId("")
        val expectedResult = None
        assertResult(expectedResult)(actualResult)
      }
    }
  }

  "retrieveAppId" should{
    "retrieve a list with one element" when {
      "it parses a deeply nested JSON" in {
        val fakeJSON =
          """{
            |"apps":[],
            |"groups":[{
            |"groups":[{
            |"groups":[{
            |"groups":[{
            |"groups":[{
            |"apps": [
            |    {"id": "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1"},
            |    {"id": "/sparta/sparta-server/workflows/testinput-kafka2"}
            |    ]
            |}]
            |}]
            |}]
            |}]
            |}]
            |}
          """.stripMargin

        val actualResult = testNginx.extractAppsId(fakeJSON).get
        val expectedResult =
          Seq("/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1",
          "/sparta/sparta-server/workflows/testinput-kafka2")
        assert(actualResult.forall(item => expectedResult.contains(item)))
      }
    }
  }

  "retrieveAppId" should{
    "retrieve a list with two elements" when {
      "there are two versions of the same app" in {
        val fakeJSON =
          """{
            |"apps":[],
            |"groups":[{
            |"groups":[{
            |"groups":[{
            |"groups":[{
            |"groups":[{
            |"apps": [
            |    {"id": "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1"},
            |    {"id": "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v0"},
            |    {"id": []}
            |    ]
            |}]
            |}]
            |}]
            |}]
            |}]
            |}
          """.stripMargin

        val actualResult = testNginx.extractAppsId(fakeJSON).get
        val expectedResult = Seq("/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1",
          "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v0")
        assert(actualResult.forall(item => expectedResult.contains(item)))
      }
    }
  }

  "retrieveAppId" should{
    "retrieve an empty list" when {
      "it parses a JSON with empty apps" in {
        val fakeJSON =
          """{
            |"apps":[{"id": []}]
            |"groups":[{
            |"groups":[{
            |"groups":[{
            |"apps":[{"id": []}]
            |}]
            |}]
            |}]
            |}
          """.stripMargin

        val actualResult = testNginx.extractAppsId(fakeJSON)
        val expectedResult = None
        assertResult(expectedResult)(actualResult)
      }
    }
  }



  "retrieveIPandPort" should {
    "retrieve a tuple (String, Int)" when {
      "it parses the testJson" in {
        testNginx.extractAppParameters(appId) shouldBe Some(
          AppParameters(
            "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1",
            "172.25.159.108",
            4040
          )
        )
      }
    }
  }

  "retrieveIPandPorts" should {
    "retrieve a tuple (String, Int)" when {
      "it parses the testJson" in {
        testNginx.extractAppParameters(appId) shouldBe Some(
          AppParameters(
            "/sparta/sparta-server/workflows/group1/nameworkflow/nameworkflow-v1",
            "172.25.159.108",
            4040
          )
        )
      }
    }
  }



}