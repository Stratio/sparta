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

import akka.actor.ActorSystem
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.env.EnvironmentVariable
import com.stratio.sparta.serving.core.models.workflow.Group
import com.stratio.sparta.serving.core.utils.ZookeeperUtils

import scala.util.Properties

/**
 * Global constants of the application.
 */
object AppConstant extends ZookeeperUtils {

  val version = "2.0.0-SNAPSHOT"
  val ConfigAppName = "sparta"
  val ConfigApi = "api"
  val ConfigHdfs = "hdfs"
  val ConfigDetail = "config"
  val ConfigOauth2 = "oauth2"
  val ConfigSpray = "spray.can.server"
  val ConfigZookeeper = "zookeeper"
  val ConfigSpark = "spark"
  val DefaultOauth2CookieName = "user"
  val ConfigLocal = "local"
  val ConfigMesos = "mesos"
  val ConfigMarathon = "marathon"
  val DefaultRememberPartitioner = true
  val DriverPackageLocation = "driverPackageLocation"
  val BackupsLocation = "backupsLocation"
  val DefaultDriverPackageLocation = "/opt/sds/sparta/driver"
  val DefaultBackupsLocation = "/opt/sds/sparta/backups"
  val DriverURI = "driverURI"
  val DefaultMarathonDriverURI = "/opt/sds/sparta/driver/sparta-driver.jar"
  val DefaultDriverLocation = "provided"
  val DriverLocation = "driverLocation"
  val PluginsLocation = "pluginsLocation"
  val DefaultPluginsLocation = "plugins"
  val ConfigSecurity = "security"
  val AwaitWorkflowChangeStatus = "awaitWorkflowChangeStatus"
  val DefaultAwaitWorkflowChangeStatus = "360s"
  val PreStopMarathonDelay = "preStopMarathonDelay"
  val DefaultPreStopMarathonDelay = "10s"
  val PreStopMarathonInterval = "preStopMarathonInterval"
  val DefaultPreStopMarathonInterval = "5s"
  val DefaultkillUrl = "http://127.0.0.1:7077/v1/submissions/kill"
  val DefaultGroup = Group(Option("940800b2-6d81-44a8-84d9-26913a2faea4"), "/home")
  val DefaultApiTimeout = 20
  val DefaultSerializationTimeout = 5

  //Hdfs Options
  val HadoopUserName = "hadoopUserName"
  val HdfsMaster = "hdfsMaster"
  val HdfsPort = "hdfsPort"
  val KeytabPath = "keytabPath"
  val PrincipalName = "principalName"
  val ReloadKeyTabTime = "reloadKeyTabTime"
  val ReloadKeyTab = "reloadKeyTab"
  val DefaultReloadKeyTab = false
  val DefaultReloadKeyTabTime = "23h"
  val SystemHadoopConfDir = "HADOOP_CONF_DIR"
  val CoreSite = "core-site.xml"
  val HDFSSite = "hdfs-site.xml"
  val SystemHadoopUserName = "HADOOP_USER_NAME"
  val SystemPrincipalName = "SPARTA_PRINCIPAL_NAME"
  val SystemKeyTabPath = "SPARTA_KEYTAB_PATH"
  val SystemHostName = "HOSTNAME"
  val DefaultFSProperty = "fs.defaultFS"

  //Zookeeper
  val ZKConnection = "connectionString"
  val DefaultZKConnection = "127.0.0.1:2181"
  val ZKConnectionTimeout = "connectionTimeout"
  val DefaultZKConnectionTimeout = 19000
  val ZKSessionTimeout = "sessionTimeout"
  val DefaultZKSessionTimeout = 60000
  val ZKRetryAttemps = "retryAttempts"
  val DefaultZKRetryAttemps = 5
  val ZKRetryInterval = "retryInterval"
  val DefaultZKRetryInterval = 10000
  val DefaultZKPath = "/stratio/sparta/sparta"

  //Zookeeper paths
  val instanceName = Properties.envOrNone("MARATHON_APP_LABEL_DCOS_SERVICE_NAME")
  lazy val BaseZkPath: String = (retrievePathFromEnv, instanceName, retrieveFromConf) match {
    case (Some(path), _, _ ) if checkIfValidPath(path) => path
    case (_, Some(instance), _)=> s"/stratio/sparta/$instance"
    case (_, _, Some(confPath)) if checkIfValidPath(confPath) => confPath
    case _ => DefaultZKPath
  }

  lazy val WorkflowsZkPath = s"$BaseZkPath/workflows"
  lazy val WorkflowStatusesZkPath = s"$BaseZkPath/workflowStatuses"
  lazy val WorkflowExecutionsZkPath = s"$BaseZkPath/workflowExecutions"
  lazy val TemplatesZkPath = s"$BaseZkPath/templates"
  lazy val ErrorsZkPath = s"$BaseZkPath/error"
  lazy val EnvironmentZkPath = s"$BaseZkPath/environment"
  lazy val GroupZkPath = s"$BaseZkPath/group"

  //Scheduler system to schedule threads executions
  val SchedulerSystem = ActorSystem("SchedulerSystem", SpartaConfig.daemonicAkkaConfig)
  val CustomTypeKey = "customClassType"

  //Environment
  val DefaultEnvironment = Seq(
    EnvironmentVariable("DEFAULT_OUTPUT_FIELD", "raw"),
    EnvironmentVariable("DEFAULT_DELIMITER", ","),
    EnvironmentVariable("CROSSDATA_ZOOKEEPER_CONNECTION", "localhost:2181"),
    EnvironmentVariable("CROSSDATA_ZOOKEEPER_PATH", "/crossdata/offsets"),
    EnvironmentVariable("KAFKA_BROKER_HOST", "localhost"),
    EnvironmentVariable("KAFKA_BROKER_PORT", "9092"),
    EnvironmentVariable("KAFKA_GROUP_ID", "sparta"),
    EnvironmentVariable("WEBSOCKET_URL", "ws://stream.meetup.com/2/rsvps"),
    EnvironmentVariable("CASSANDRA_HOST", "localhost"),
    EnvironmentVariable("CASSANDRA_PORT", "9042"),
    EnvironmentVariable("CASSANDRA_KEYSPACE", "sparta"),
    EnvironmentVariable("CASSANDRA_CLUSTER", "sparta"),
    EnvironmentVariable("ES_HOST", "localhost"),
    EnvironmentVariable("ES_PORT", "9200"),
    EnvironmentVariable("ES_CLUSTER", "elasticsearch"),
    EnvironmentVariable("ES_INDEX_MAPPING", "sparta"),
    EnvironmentVariable("JDBC_URL", "jdbc:postgresql://dbserver:port/database?user=postgres"),
    EnvironmentVariable("POSTGRES_URL", "jdbc:postgresql://dbserver:port/database?user=postgres"),
    EnvironmentVariable("MONGODB_HOST", "localhost"),
    EnvironmentVariable("MONGODB_PORT", "27017"),
    EnvironmentVariable("MONGODB_DB", "sparta"),
    EnvironmentVariable("REDIS_HOST", "localhost"),
    EnvironmentVariable("REDIS_PORT", "6379"),
    EnvironmentVariable("SPARTA_CHECKPOINT_PATH","sparta/checkpoint"),
    EnvironmentVariable("SPARK_STREAMING_WINDOW","2s"),
    EnvironmentVariable("SPARK_MASTER","mesos://leader.mesos:5050"),
    EnvironmentVariable("DRIVER_JAVA_OPTIONS","-XX:+UseConcMarkSweepGC -Dlog4j.configurationFile=file:///etc/sds/sparta/log4j2.xml"),
    EnvironmentVariable("EXECUTOR_EXTRA_JAVA_OPTIONS","-XX:+UseConcMarkSweepGC"),
    EnvironmentVariable("SPARK_LOCAL_PATH", "/opt/spark/dist"),
    EnvironmentVariable("SPARK_CORES_MAX","2"),
    EnvironmentVariable("SPARK_EXECUTOR_MEMORY","2G"),
    EnvironmentVariable("SPARK_EXECUTOR_CORES","1"),
    EnvironmentVariable("SPARK_DRIVER_CORES","1"),
    EnvironmentVariable("SPARK_DRIVER_MEMORY","2G"),
    EnvironmentVariable("SPARK_LOCALITY_WAIT","100"),
    EnvironmentVariable("SPARK_TASK_MAX_FAILURES","8"),
    EnvironmentVariable("SPARK_MEMORY_FRACTION","0.6"),
    EnvironmentVariable("SPARK_BLOCK_INTERVAL","100ms")
  )
}
