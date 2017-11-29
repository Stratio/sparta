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

package com.stratio.sparta.serving.core.utils

import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.utils.NginxUtils.{AppParameters, NginxMetaConfig}
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
        val result = testNginx.updatedNginxConf(listWorkflowsFake, "sparta-server")

        result should not be empty
      }
    }
  }


  val jsonApp="""{
                |  "id": "/sparta/sparta-server/workflows",
                |  "dependencies": [],
                |  "version": "2017-09-20T07:35:44.922Z",
                |  "apps": [{
                |    "id": "/sparta/sparta-server/workflows/kafka-print-10partitions",
                |    "cmd": null,
                |    "args": null,
                |    "user": null,
                |    "env": {
                |      "VAULT_HOST": "vault.service.paas.labs.stratio.com",
                |      "ZOOKEEPER_LOG_LEVEL": "OFF",
                |      "SPARTA_MARATHON_JAR": "/opt/sds/sparta/driver/sparta-driver.jar",
                |      "SPARK_SECURITY_KAFKA_VAULT_CERT_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
                |      "SECURITY_MESOS_ENABLE": "true",
                |      "SECURITY_MARATHON_ENABLE": "true",
                |      "HADOOP_CONF_FROM_DFS": "true",
                |      "CROSSDATA_CORE_CATALOG_CLASS": "org.apache.spark.sql.crossdata.catalyst.catalog.persistent.zookeeper.ZookeeperCatalog",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONTIMEOUT": "15000",
                |      "HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH": "128",
                |      "CORE_SITE_FROM_URI": "false",
                |      "SPARTA_ZOOKEEPER_CONFIG": "eyJ6b29rZWVwZXIiOnsiY29ubmVjdGlvblN0cmluZyI6InprLTAwMDEtem9va2VlcGVyc3RhYmxlLnNlcnZpY2UucGFhcy5sYWJzLnN0cmF0aW8uY29tOjIxODEsemstMDAwMi16b29rZWVwZXJzdGFibGUuc2VydmljZS5wYWFzLmxhYnMuc3RyYXRpby5jb206MjE4MSx6ay0wMDAzLXpvb2tlZXBlcnN0YWJsZS5zZXJ2aWNlLnBhYXMubGFicy5zdHJhdGlvLmNvbToyMTgxIiwiY29ubmVjdGlvblRpbWVvdXQiOjE1MDAwLCJyZXRyeUF0dGVtcHRzIjo1LCJyZXRyeUludGVydmFsIjoxMDAwMCwic2Vzc2lvblRpbWVvdXQiOjYwMDAwLCJzdG9yYWdlUGF0aCI6Ii9zdHJhdGlvL3NwYXJ0YS9zcGFydGEtc2VydmVyIn19",
                |      "SPARK_SECURITY_KAFKA_VAULT_KEY_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
                |      "HADOOP_SECURITY_AUTH": "kerberos",
                |      "SERVICE_LOG_LEVEL": "ERROR",
                |      "HADOOP_FS_DEFAULT_NAME": "hdfs://192.168.0.41:8020",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYINTERVAL": "10000",
                |      "SECURITY_TRUSTSTORE_ENABLE": "true",
                |      "SECURITY_KRB_ENABLE": "true",
                |      "HADOOP_SECURITY_TOKEN_USE_IP": "false",
                |      "MESOS_NATIVE_JAVA_LIBRARY": "/usr/lib/libmesos.so",
                |      "PARQUET_LOG_LEVEL": "ERROR",
                |      "SPARTA_APP_TYPE": "marathon",
                |      "MARATHON_APP_LABEL_DCOS_SERVICE_NAME": "sparta-server",
                |      "CROSSDATA_CORE_CATALOG_PREFIX": "crossdataCluster",
                |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER": "true",
                |      "SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PATH": "/v1/ca-trust/certificates/ca",
                |      "SPARK_SECURITY_KAFKA_VAULT_CERT_PATH": "/v1/userland/certificates/sparta-server",
                |      "SPARK_HOME": "/opt/spark/dist",
                |      "NETTY_LOG_LEVEL": "ERROR",
                |      "AVRO_LOG_LEVEL": "ERROR",
                |      "HADOOP_LOG_LEVEL": "ERROR",
                |      "SPARTA_MARATHON_MAIN_CLASS": "com.stratio.sparta.driver.MarathonDriver",
                |      "SECURITY_TLS_ENABLE": "true",
                |      "HADOOP_NAMENODE_KRB_PRINCIPAL": "hdfs/192.168.0.41@PERFORMANCE.STRATIO.COM",
                |      "CALICO_NETWORK": "stratio",
                |      "SECURITY_OAUTH2_ENABLE": "false",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING": "zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181",
                |      "VAULT_PROTOCOL": "https",
                |      "VAULT_ENABLE": "true",
                |      "SPARTA_DETAIL_CONFIG": "eyJjb25maWciOnsiYXdhaXRQb2xpY3lDaGFuZ2VTdGF0dXMiOiIzNjBzIiwiYmFja3Vwc0xvY2F0aW9uIjoiL29wdC9zZHMvc3BhcnRhL2JhY2t1cHMiLCJjcm9zc2RhdGEiOnsicmVmZXJlbmNlIjoiL2V0Yy9zZHMvc3BhcnRhL3JlZmVyZW5jZS5jb25mIn0sImRyaXZlclBhY2thZ2VMb2NhdGlvbiI6Ii9vcHQvc2RzL3NwYXJ0YS9kcml2ZXIiLCJmcm9udGVuZCI6eyJ0aW1lb3V0IjoxMDAwMH0sInBsdWdpblBhY2thZ2VMb2NhdGlvbiI6Ii9vcHQvc2RzL3NwYXJ0YS9wbHVnaW5zIn19",
                |      "HADOOP_CONF_FROM_URI": "false",
                |      "VAULT_PORT": "8200",
                |      "SPARTA_ZOOKEEPER_PATH": "/stratio/sparta/sparta-server",
                |      "SPARTA_LOG_LEVEL": "DEBUG",
                |      "HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN": "hdfs/*@PERFORMANCE.STRATIO.COM",
                |      "HADOOP_CONF_DIR": "/etc/hadoop",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYATTEMPTS": "5",
                |      "CALICO_ENABLED": "true",
                |      "VAULT_HOSTS": "vault.service.paas.labs.stratio.com",
                |      "HADOOP_CONF_FROM_DFS_NOT_SECURED": "false",
                |      "SPARTA_WORKFLOW_ID": "e1b67a79-e96d-4329-b48d-455bca5eea43",
                |      "USE_DYNAMIC_AUTHENTICATION": "true",
                |      "SPARK_SECURITY_KAFKA_ENABLE": "true",
                |      "HADOOP_RPC_PROTECTION": "authentication",
                |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES": "AES/CTR/NoPadding",
                |      "SPARK_LOG_LEVEL": "INFO",
                |      "MARATHON_APP_HEAP_SIZE": "-Xmx4096m",
                |      "SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
                |      "APPROLE": {
                |        "secret": "role"
                |      }
                |    },
                |    "instances": 1,
                |    "cpus": 2,
                |    "mem": 4096,
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
                |        "image": "qa.stratio.com/stratio/sparta:1.7.4",
                |        "network": "USER",
                |        "portMappings": [{
                |          "containerPort": 4040,
                |          "hostPort": 4040,
                |          "servicePort": 10109,
                |          "protocol": "tcp",
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
                |      "path": "/environment",
                |      "protocol": "HTTP",
                |      "portIndex": 0,
                |      "gracePeriodSeconds": 180,
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
                |    "version": "2017-09-20T07:35:44.922Z",
                |    "residency": null,
                |    "secrets": {
                |      "role": {
                |        "source": "open"
                |      }
                |    },
                |    "taskKillGracePeriodSeconds": null,
                |    "versionInfo": {
                |      "lastScalingAt": "2017-09-20T07:35:44.922Z",
                |      "lastConfigChangeAt": "2017-09-20T07:35:44.922Z"
                |    }
                |  }, {
                |    "id": "/sparta/sparta-server/workflows/kafka-print-5partition",
                |    "cmd": null,
                |    "args": null,
                |    "user": null,
                |    "env": {
                |      "VAULT_HOST": "vault.service.paas.labs.stratio.com",
                |      "ZOOKEEPER_LOG_LEVEL": "OFF",
                |      "SPARTA_MARATHON_JAR": "/opt/sds/sparta/driver/sparta-driver.jar",
                |      "SPARK_SECURITY_KAFKA_VAULT_CERT_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
                |      "SECURITY_MESOS_ENABLE": "true",
                |      "SECURITY_MARATHON_ENABLE": "true",
                |      "HADOOP_CONF_FROM_DFS": "true",
                |      "CROSSDATA_CORE_CATALOG_CLASS": "org.apache.spark.sql.crossdata.catalyst.catalog.persistent.zookeeper.ZookeeperCatalog",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONTIMEOUT": "15000",
                |      "HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH": "128",
                |      "CORE_SITE_FROM_URI": "false",
                |      "SPARTA_ZOOKEEPER_CONFIG": "eyJ6b29rZWVwZXIiOnsiY29ubmVjdGlvblN0cmluZyI6InprLTAwMDEtem9va2VlcGVyc3RhYmxlLnNlcnZpY2UucGFhcy5sYWJzLnN0cmF0aW8uY29tOjIxODEsemstMDAwMi16b29rZWVwZXJzdGFibGUuc2VydmljZS5wYWFzLmxhYnMuc3RyYXRpby5jb206MjE4MSx6ay0wMDAzLXpvb2tlZXBlcnN0YWJsZS5zZXJ2aWNlLnBhYXMubGFicy5zdHJhdGlvLmNvbToyMTgxIiwiY29ubmVjdGlvblRpbWVvdXQiOjE1MDAwLCJyZXRyeUF0dGVtcHRzIjo1LCJyZXRyeUludGVydmFsIjoxMDAwMCwic2Vzc2lvblRpbWVvdXQiOjYwMDAwLCJzdG9yYWdlUGF0aCI6Ii9zdHJhdGlvL3NwYXJ0YS9zcGFydGEtc2VydmVyIn19",
                |      "SPARK_SECURITY_KAFKA_VAULT_KEY_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
                |      "HADOOP_SECURITY_AUTH": "kerberos",
                |      "APP_NAME": "sparta-server",
                |      "SERVICE_LOG_LEVEL": "ERROR",
                |      "HADOOP_FS_DEFAULT_NAME": "hdfs://192.168.0.41:8020",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYINTERVAL": "10000",
                |      "SECURITY_TRUSTSTORE_ENABLE": "true",
                |      "SECURITY_KRB_ENABLE": "true",
                |      "HADOOP_SECURITY_TOKEN_USE_IP": "false",
                |      "SPARK_DATASTORE_SSL_ENABLE": "true",
                |      "MESOS_NATIVE_JAVA_LIBRARY": "/usr/lib/libmesos.so",
                |      "PARQUET_LOG_LEVEL": "ERROR",
                |      "SPARTA_APP_TYPE": "marathon",
                |      "MARATHON_APP_LABEL_DCOS_SERVICE_NAME": "sparta-server",
                |      "CROSSDATA_CORE_CATALOG_PREFIX": "crossdataCluster",
                |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER": "true",
                |      "SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PATH": "/v1/ca-trust/certificates/ca",
                |      "SPARK_SECURITY_KAFKA_VAULT_CERT_PATH": "/v1/userland/certificates/sparta-server",
                |      "CA_NAME": "ca",
                |      "SPARK_HOME": "/opt/spark/dist",
                |      "NETTY_LOG_LEVEL": "ERROR",
                |      "AVRO_LOG_LEVEL": "ERROR",
                |      "HADOOP_LOG_LEVEL": "ERROR",
                |      "SPARTA_MARATHON_MAIN_CLASS": "com.stratio.sparta.driver.MarathonDriver",
                |      "SECURITY_TLS_ENABLE": "true",
                |      "HADOOP_NAMENODE_KRB_PRINCIPAL": "hdfs/192.168.0.41@PERFORMANCE.STRATIO.COM",
                |      "CALICO_NETWORK": "stratio",
                |      "SECURITY_OAUTH2_ENABLE": "false",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING": "zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181",
                |      "VAULT_PROTOCOL": "https",
                |      "VAULT_ENABLE": "true",
                |      "SPARTA_DETAIL_CONFIG": "eyJjb25maWciOnsiYXdhaXRQb2xpY3lDaGFuZ2VTdGF0dXMiOiIzNjBzIiwiYmFja3Vwc0xvY2F0aW9uIjoiL29wdC9zZHMvc3BhcnRhL2JhY2t1cHMiLCJjcm9zc2RhdGEiOnsicmVmZXJlbmNlIjoiL2V0Yy9zZHMvc3BhcnRhL3JlZmVyZW5jZS5jb25mIn0sImRyaXZlclBhY2thZ2VMb2NhdGlvbiI6Ii9vcHQvc2RzL3NwYXJ0YS9kcml2ZXIiLCJmcm9udGVuZCI6eyJ0aW1lb3V0IjoxMDAwMH0sInBsdWdpblBhY2thZ2VMb2NhdGlvbiI6Ii9vcHQvc2RzL3NwYXJ0YS9wbHVnaW5zIn19",
                |      "HADOOP_CONF_FROM_URI": "false",
                |      "VAULT_PORT": "8200",
                |      "SPARTA_ZOOKEEPER_PATH": "/stratio/sparta/sparta-server",
                |      "SPARTA_LOG_LEVEL": "DEBUG",
                |      "HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN": "hdfs/*@PERFORMANCE.STRATIO.COM",
                |      "HADOOP_CONF_DIR": "/etc/hadoop",
                |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYATTEMPTS": "5",
                |      "CALICO_ENABLED": "true",
                |      "VAULT_HOSTS": "vault.service.paas.labs.stratio.com",
                |      "HADOOP_CONF_FROM_DFS_NOT_SECURED": "false",
                |      "SPARTA_WORKFLOW_ID": "5cd822e5-3988-4381-bd6b-1688d13d34f0",
                |      "USE_DYNAMIC_AUTHENTICATION": "true",
                |      "SPARK_SECURITY_KAFKA_ENABLE": "true",
                |      "HADOOP_RPC_PROTECTION": "authentication",
                |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES": "AES/CTR/NoPadding",
                |      "SPARK_LOG_LEVEL": "INFO",
                |      "MARATHON_APP_HEAP_SIZE": "-Xmx4096m",
                |      "SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
                |      "APPROLE": {
                |        "secret": "role"
                |      }
                |    },
                |    "instances": 1,
                |    "cpus": 2,
                |    "mem": 4096,
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
                |        "image": "qa.stratio.com/stratio/sparta:1.7.4",
                |        "network": "USER",
                |        "portMappings": [{
                |          "containerPort": 4040,
                |          "hostPort": 4040,
                |          "servicePort": 10116,
                |          "protocol": "tcp",
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
                |      "path": "/environment",
                |      "protocol": "HTTP",
                |      "portIndex": 0,
                |      "gracePeriodSeconds": 180,
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
                |    "version": "2017-09-19T10:40:35.571Z",
                |    "residency": null,
                |    "secrets": {
                |      "role": {
                |        "source": "open"
                |      }
                |    },
                |    "taskKillGracePeriodSeconds": null,
                |    "versionInfo": {
                |      "lastScalingAt": "2017-09-19T10:40:35.571Z",
                |      "lastConfigChangeAt": "2017-09-19T10:40:35.571Z"
                |    }
                |  }],
                |  "groups": []
                |}
                |""".stripMargin


  val appId= """{
               |  "app": {
               |    "id": "/sparta/sparta-server/workflows/kafka-print-10partitions",
               |    "cmd": null,
               |    "args": null,
               |    "user": null,
               |    "env": {
               |      "VAULT_HOST": "vault.service.paas.labs.stratio.com",
               |      "ZOOKEEPER_LOG_LEVEL": "OFF",
               |      "SPARTA_MARATHON_JAR": "/opt/sds/sparta/driver/sparta-driver.jar",
               |      "SPARK_SECURITY_KAFKA_VAULT_CERT_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
               |      "SECURITY_MESOS_ENABLE": "true",
               |      "SECURITY_MARATHON_ENABLE": "true",
               |      "HADOOP_CONF_FROM_DFS": "true",
               |      "CROSSDATA_CORE_CATALOG_CLASS": "org.apache.spark.sql.crossdata.catalyst.catalog.persistent.zookeeper.ZookeeperCatalog",
               |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONTIMEOUT": "15000",
               |      "HADOOP_DFS_ENCRYPT_DATA_CIPHER_KEY_BITLENGTH": "128",
               |      "CORE_SITE_FROM_URI": "false",
               |      "SPARTA_ZOOKEEPER_CONFIG": "eyJ6b29rZWVwZXIiOnsiY29ubmVjdGlvblN0cmluZyI6InprLTAwMDEtem9va2VlcGVyc3RhYmxlLnNlcnZpY2UucGFhcy5sYWJzLnN0cmF0aW8uY29tOjIxODEsemstMDAwMi16b29rZWVwZXJzdGFibGUuc2VydmljZS5wYWFzLmxhYnMuc3RyYXRpby5jb206MjE4MSx6ay0wMDAzLXpvb2tlZXBlcnN0YWJsZS5zZXJ2aWNlLnBhYXMubGFicy5zdHJhdGlvLmNvbToyMTgxIiwiY29ubmVjdGlvblRpbWVvdXQiOjE1MDAwLCJyZXRyeUF0dGVtcHRzIjo1LCJyZXRyeUludGVydmFsIjoxMDAwMCwic2Vzc2lvblRpbWVvdXQiOjYwMDAwLCJzdG9yYWdlUGF0aCI6Ii9zdHJhdGlvL3NwYXJ0YS9zcGFydGEtc2VydmVyIn19",
               |      "SPARK_SECURITY_KAFKA_VAULT_KEY_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
               |      "HADOOP_SECURITY_AUTH": "kerberos",
               |      "SERVICE_LOG_LEVEL": "ERROR",
               |      "HADOOP_FS_DEFAULT_NAME": "hdfs://192.168.0.41:8020",
               |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYINTERVAL": "10000",
               |      "SECURITY_TRUSTSTORE_ENABLE": "true",
               |      "SECURITY_KRB_ENABLE": "true",
               |      "HADOOP_SECURITY_TOKEN_USE_IP": "false",
               |      "MESOS_NATIVE_JAVA_LIBRARY": "/usr/lib/libmesos.so",
               |      "PARQUET_LOG_LEVEL": "ERROR",
               |      "SPARTA_APP_TYPE": "marathon",
               |      "MARATHON_APP_LABEL_DCOS_SERVICE_NAME": "sparta-server",
               |      "CROSSDATA_CORE_CATALOG_PREFIX": "crossdataCluster",
               |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER": "true",
               |      "SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PATH": "/v1/ca-trust/certificates/ca",
               |      "SPARK_SECURITY_KAFKA_VAULT_CERT_PATH": "/v1/userland/certificates/sparta-server",
               |      "SPARK_HOME": "/opt/spark/dist",
               |      "NETTY_LOG_LEVEL": "ERROR",
               |      "AVRO_LOG_LEVEL": "ERROR",
               |      "HADOOP_LOG_LEVEL": "ERROR",
               |      "SPARTA_MARATHON_MAIN_CLASS": "com.stratio.sparta.driver.MarathonDriver",
               |      "SECURITY_TLS_ENABLE": "true",
               |      "HADOOP_NAMENODE_KRB_PRINCIPAL": "hdfs/192.168.0.41@PERFORMANCE.STRATIO.COM",
               |      "CALICO_NETWORK": "stratio",
               |      "SECURITY_OAUTH2_ENABLE": "false",
               |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_CONNECTIONSTRING": "zk-0001-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0002-zookeeperstable.service.paas.labs.stratio.com:2181,zk-0003-zookeeperstable.service.paas.labs.stratio.com:2181",
               |      "VAULT_PROTOCOL": "https",
               |      "VAULT_ENABLE": "true",
               |      "SPARTA_DETAIL_CONFIG": "eyJjb25maWciOnsiYXdhaXRQb2xpY3lDaGFuZ2VTdGF0dXMiOiIzNjBzIiwiYmFja3Vwc0xvY2F0aW9uIjoiL29wdC9zZHMvc3BhcnRhL2JhY2t1cHMiLCJjcm9zc2RhdGEiOnsicmVmZXJlbmNlIjoiL2V0Yy9zZHMvc3BhcnRhL3JlZmVyZW5jZS5jb25mIn0sImRyaXZlclBhY2thZ2VMb2NhdGlvbiI6Ii9vcHQvc2RzL3NwYXJ0YS9kcml2ZXIiLCJmcm9udGVuZCI6eyJ0aW1lb3V0IjoxMDAwMH0sInBsdWdpblBhY2thZ2VMb2NhdGlvbiI6Ii9vcHQvc2RzL3NwYXJ0YS9wbHVnaW5zIn19",
               |      "HADOOP_CONF_FROM_URI": "false",
               |      "VAULT_PORT": "8200",
               |      "SPARTA_ZOOKEEPER_PATH": "/stratio/sparta/sparta-server",
               |      "SPARTA_LOG_LEVEL": "DEBUG",
               |      "HADOOP_NAMENODE_KRB_PRINCIPAL_PATTERN": "hdfs/*@PERFORMANCE.STRATIO.COM",
               |      "HADOOP_CONF_DIR": "/etc/hadoop",
               |      "CROSSDATA_CORE_CATALOG_ZOOKEEPER_RETRYATTEMPTS": "5",
               |      "CALICO_ENABLED": "true",
               |      "VAULT_HOSTS": "vault.service.paas.labs.stratio.com",
               |      "HADOOP_CONF_FROM_DFS_NOT_SECURED": "false",
               |      "SPARTA_WORKFLOW_ID": "e1b67a79-e96d-4329-b48d-455bca5eea43",
               |      "USE_DYNAMIC_AUTHENTICATION": "true",
               |      "SPARK_SECURITY_KAFKA_ENABLE": "true",
               |      "HADOOP_RPC_PROTECTION": "authentication",
               |      "HADOOP_DFS_ENCRYPT_DATA_TRANSFER_CIPHER_SUITES": "AES/CTR/NoPadding",
               |      "SPARK_LOG_LEVEL": "INFO",
               |      "MARATHON_APP_HEAP_SIZE": "-Xmx4096m",
               |      "SPARK_SECURITY_KAFKA_VAULT_TRUSTSTORE_PASS_PATH": "/v1/userland/passwords/sparta-server/keystore",
               |      "APPROLE": {
               |        "secret": "role"
               |      }
               |    },
               |    "instances": 1,
               |    "cpus": 2,
               |    "mem": 4096,
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
               |        "image": "qa.stratio.com/stratio/sparta:1.7.4",
               |        "network": "USER",
               |        "portMappings": [{
               |          "containerPort": 4040,
               |          "hostPort": 4040,
               |          "servicePort": 10109,
               |          "protocol": "tcp",
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
               |      "path": "/environment",
               |      "protocol": "HTTP",
               |      "portIndex": 0,
               |      "gracePeriodSeconds": 180,
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
               |    "version": "2017-09-20T07:35:44.922Z",
               |    "residency": null,
               |    "secrets": {
               |      "role": {
               |        "source": "open"
               |      }
               |    },
               |    "taskKillGracePeriodSeconds": null,
               |    "versionInfo": {
               |      "lastScalingAt": "2017-09-20T07:35:44.922Z",
               |      "lastConfigChangeAt": "2017-09-20T07:35:44.922Z"
               |    },
               |    "tasksStaged": 0,
               |    "tasksRunning": 1,
               |    "tasksHealthy": 1,
               |    "tasksUnhealthy": 0,
               |    "deployments": [],
               |    "tasks": [{
               |      "id": "sparta_sparta-server_workflows_kafka-print-10partitions.50111b86-9dd6-11e7-84fe-86ecbfdb9791",
               |      "slaveId": "87cd0d16-d4de-47d3-80d5-6915e74c497b-S14",
               |      "host": "192.168.0.218",
               |      "state": "TASK_RUNNING",
               |      "startedAt": "2017-09-20T07:35:46.372Z",
               |      "stagedAt": "2017-09-20T07:35:45.044Z",
               |      "ports": [4040],
               |      "version": "2017-09-20T07:35:44.922Z",
               |      "ipAddresses": [{
               |        "ipAddress": "172.25.79.178",
               |        "protocol": "IPv4"
               |      }],
               |      "appId": "/sparta/sparta-server/workflows/kafka-print-10partitions",
               |      "healthCheckResults": [{
               |        "alive": true,
               |        "consecutiveFailures": 0,
               |        "firstSuccess": "2017-09-20T07:36:50.076Z",
               |        "lastFailure": null,
               |        "lastSuccess": "2017-09-20T09:10:51.889Z",
               |        "lastFailureCause": null,
               |        "taskId": "sparta_sparta-server_workflows_kafka-print-10partitions.50111b86-9dd6-11e7-84fe-86ecbfdb9791"
               |      }]
               |    }]
               |  }
               |}""".stripMargin


  "retrieveAppId" should{
    "retrieve two appID" when {
      "it parses the testJson" in {
        testNginx.extractAppsId(jsonApp) shouldBe Some {
          Seq(
            "/sparta/sparta-server/workflows/kafka-print-10partitions",
            "/sparta/sparta-server/workflows/kafka-print-5partition"
          )
        }
      }
    }
  }

  "retrieveIPandPort" should {
    "retrieve a tuple (String, Int)" when {
      "it parses the testJson" in {
        testNginx.extractAppParameters(appId) shouldBe Some(
          AppParameters(
            "/sparta/sparta-server/workflows/kafka-print-10partitions",
            "172.25.79.178",
            4040
          )
        )
      }
    }
  }



}