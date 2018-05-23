/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.constants

import akka.actor.ActorSystem
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.DataType
import com.stratio.sparta.serving.core.models.env.EnvironmentVariable
import com.stratio.sparta.serving.core.models.workflow.{Group, WorkflowRelationSettings}
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
  val ConfigCrossdata = "crossdata"
  val DefaultOauth2CookieName = "user"
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
  val DefaultSerializationTimeout = 5000
  val DefaultEnvSleep = 5000L
  val DefaultRecoverySleep = 5000L
  val SparkLocalMaster = "local[1]"

  //Debug Options
  val DebugSparkWindow = 100

  //Workflow
  val defaultWorkflowRelationSettings = WorkflowRelationSettings(DataType.ValidData)

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
  lazy val DebugWorkflowZkPath = s"$BaseZkPath/debug"
  lazy val DebugStepDataZkPath = s"$BaseZkPath/debugStepData"
  lazy val DebugStepErrorZkPath = s"$BaseZkPath/debugStepError"

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
    EnvironmentVariable("KAFKA_MAX_POLL_TIMEOUT", "512"),
    EnvironmentVariable("KAFKA_MAX_RATE_PER_PARTITION", "0"),
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
    EnvironmentVariable("JDBC_DRIVER", "org.postgresql.Driver"),
    EnvironmentVariable("POSTGRES_URL", "jdbc:postgresql://dbserver:port/database?user=postgres"),
    EnvironmentVariable("MONGODB_HOST", "localhost"),
    EnvironmentVariable("MONGODB_PORT", "27017"),
    EnvironmentVariable("MONGODB_DB", "sparta"),
    EnvironmentVariable("REDIS_HOST", "localhost"),
    EnvironmentVariable("REDIS_PORT", "6379"),
    EnvironmentVariable("SPARK_STREAMING_CHECKPOINT_PATH","sparta/checkpoint"),
    EnvironmentVariable("SPARK_STREAMING_WINDOW","2s"),
    EnvironmentVariable("SPARK_STREAMING_BLOCK_INTERVAL","100ms"),
    EnvironmentVariable("SPARK_MASTER","mesos://leader.mesos:5050"),
    EnvironmentVariable("SPARK_DRIVER_JAVA_OPTIONS","-Dconfig.file=/etc/sds/sparta/spark/reference.conf -XX:+UseConcMarkSweepGC -Dlog4j.configurationFile=file:///etc/sds/sparta/log4j2.xml"),
    EnvironmentVariable("SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS","-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC"),
    EnvironmentVariable("SPARK_LOCAL_PATH", "/opt/spark/dist"),
    EnvironmentVariable("SPARK_CORES_MAX","2"),
    EnvironmentVariable("SPARK_EXECUTOR_MEMORY","2G"),
    EnvironmentVariable("SPARK_EXECUTOR_CORES","1"),
    EnvironmentVariable("SPARK_DRIVER_CORES","1"),
    EnvironmentVariable("SPARK_DRIVER_MEMORY","2G"),
    EnvironmentVariable("SPARK_LOCALITY_WAIT","100"),
    EnvironmentVariable("SPARK_TASK_MAX_FAILURES","8"),
    EnvironmentVariable("SPARK_MEMORY_FRACTION","0.6"),
    EnvironmentVariable("SPARK_EXECUTOR_BASE_IMAGE","qa.stratio.com/stratio/stratio-spark:2.2.0.5")
  )
}
