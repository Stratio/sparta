/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.constants

import akka.actor.ActorSystem
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.DataType
import com.stratio.sparta.serving.core.models.parameters.ParameterVariable
import com.stratio.sparta.serving.core.models.workflow.{Group, WorkflowRelationSettings}
import com.stratio.sparta.serving.core.utils.ZookeeperUtils

import scala.util.{Properties, Try}

/**
 * Global constants of the application.
 */
object AppConstant extends ZookeeperUtils {

  val ConfigAppName = "sparta"
  val ConfigOauth2 = "oauth2"
  val ConfigSpray = "spray.can.server"
  val ConfigSpark = "spark"
  val ConfigCrossdata = "crossdata"
  val ConfigApi = "sparta.api"
  val ConfigHdfs = "sparta.hdfs"
  val ConfigDetail = "sparta.config"
  val ConfigSecurity = "sparta.security"
  val ConfigPostgres = "sparta.postgres"
  val ConfigZookeeper = "sparta.zookeeper"
  val ConfigMarathon = "sparta.marathon"
  val ConfigIntelligence = "sparta.intelligence"
  val ConfigIgnite = "sparta.ignite"
  val HdfsKey = "hdfs"
  val DefaultOauth2CookieName = "user"
  val DriverPackageLocation = "driverPackageLocation"
  val DefaultDriverPackageLocation = "/opt/sds/sparta/driver"
  val DriverURI = "driverURI"
  val DefaultMarathonDriverURI = "/opt/sds/sparta/driver/sparta-driver.jar"
  val DefaultDriverLocation = "provided"
  val DriverLocation = "driverLocation"
  val PluginsLocation = "pluginsLocation"
  val DefaultPluginsLocation = "plugins"
  val AwaitWorkflowChangeStatus = "awaitWorkflowChangeStatus"
  val ModelRepositoryUrlKey = "repository.url"
  val DefaultModelRepositoryUrl = "http://localhost:11000"
  val DefaultAwaitWorkflowChangeStatus = "360s"
  val DefaultkillUrl = "http://127.0.0.1:7077/v1/submissions/kill"
  val DefaultGroup = Group(Option("940800b2-6d81-44a8-84d9-26913a2faea4"), "/home")
  val DefaultApiTimeout = 20
  val DefaultVersion = "2.4.0"
  lazy val version = Try(SpartaConfig.getDetailConfig().get.getString("version"))
    .toOption.notBlank.getOrElse(DefaultVersion)
  val CassiopeaVersion = ""
  val AndromedaVersion = "2.3.0"

  //Debug Options
  val DebugSparkWindow = 120000
  val maxDebugTimeout = 10000
  val maxDebugWriteErrorTimeout = 5000

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
  val DefaultZKPath = "/stratio/sparta/sparta"

  val PostgresDaos="com.stratio.sparta.serving.core.services.dao"

  //Zookeeper paths
  val instanceName = Properties.envOrNone(MarathonConstant.DcosServiceName)
  lazy val BaseZkPath: String = (retrievePathFromEnv, instanceName, retrieveFromConf) match {
    case (Some(path), _, _ ) if checkIfValidPath(path) => path
    case (_, Some(instance), _)=> s"/stratio/sparta/$instance"
    case (_, _, Some(confPath)) if checkIfValidPath(confPath) => confPath
    case _ => DefaultZKPath
  }
  lazy val ExecutionsStatusChangesZkPath = s"$BaseZkPath/executionStatusChanges"
  lazy val IgniteDiscoveryZkPath = s"$BaseZkPath/$retrieveIgnitePathFromEnv"

  //Migration
  lazy val WorkflowsZkPath = s"$BaseZkPath/workflows"
  lazy val WorkflowsOldZkPath = s"$BaseZkPath-old/workflows"
  lazy val TemplatesZkPath = s"$BaseZkPath/templates"
  lazy val TemplatesOldZkPath = s"$BaseZkPath-old/templates"
  lazy val EnvironmentZkPath = s"$BaseZkPath/environment"
  lazy val EnvironmentOldZkPath = s"$BaseZkPath-old/environment"
  lazy val GroupZkPath = s"$BaseZkPath/group"
  lazy val GroupOldZkPath = s"$BaseZkPath-old/group"


  //Ignite
  val IgniteEnabled = "enabled"
  val IgniteInstanceName = "instance.name"
  val IgniteCacheName = "cache.name"
  val IgniteCommunicationSpiAddress = "communication.spiAddress"
  val IgniteCommunicationSpiPort = "communication.spiPort"
  val IgniteCommunicationSpiPortRange = "communication.spiPortRange"
  val IgniteSecurityEnabled = "security.enabled"
  val IgniteClusterEnabled = "cluster.enabled"
  val IgnitePersistenceEnabled = "persistence.enabled"
  val IgnitePersistenceWalPath = "persistence.walPath"
  val IgnitePersistencePath = "persistence.persistencePath"
  val IgniteMemoryInitialSize ="memory.initialSizeMB"
  val IgniteMemoryMaxSize ="memory.maxSizeMB"

  //Marathon
  val marathonInstanceName = AppConstant.instanceName.fold("sparta-server") { x => x }

  lazy val ClusterSeedNodesZkPath = s"$BaseZkPath/seedNodes"

  //Scheduler system to schedule threads executions
  val SchedulerSystem = ActorSystem("SchedulerSystem", SpartaConfig.daemonicAkkaConfig)
  val CustomTypeKey = "customClassType"

  //Parameters
  val parametersTwoBracketsPattern = "\\{\\{[\\w\\.\\-\\_]*\\}\\}".r

  //Environment Parameters
  val EnvironmentParameterListName = "Environment"
  val EnvironmentParameterListId = Option("f16e9034-ab81-11e8-98d0-529269fb1459")
  val DefaultEnvironmentParameters = Seq(
    new ParameterVariable("CROSSDATA_ZOOKEEPER_CONNECTION", "localhost:2181"),
    new ParameterVariable("CROSSDATA_ZOOKEEPER_PATH", "/crossdata/offsets"),
    new ParameterVariable("KAFKA_BROKER_HOST", "localhost"),
    new ParameterVariable("KAFKA_BROKER_PORT", "9092"),
    new ParameterVariable("CASSANDRA_HOST", "localhost"),
    new ParameterVariable("ES_HOST", "localhost"),
    new ParameterVariable("ES_PORT", "9200"),
    new ParameterVariable("JDBC_URL", "jdbc:postgresql://dbserver:port/database?user=postgres"),
    new ParameterVariable("JDBC_DRIVER", "org.postgresql.Driver"),
    new ParameterVariable("POSTGRES_URL", "jdbc:postgresql://dbserver:port/database?user=postgres"),
    new ParameterVariable("MONGODB_HOST", "localhost"),
    new ParameterVariable("MONGODB_PORT", "27017")
  )

  //Example Custom Group parameters
  val CustomExampleParameterList = "Default"
  val CustomExampleParameterListId = Option("1b8d86a8-c7d5-11e8-a8d5-f2801f1b9fd1")
  val DefaultCustomExampleParameters = Seq(
    new ParameterVariable("CASSANDRA_KEYSPACE", "sparta"),
    new ParameterVariable("CASSANDRA_CLUSTER", "sparta"),
    new ParameterVariable("KAFKA_GROUP_ID", "sparta"),
    new ParameterVariable("KAFKA_MAX_POLL_TIMEOUT", "512"),
    new ParameterVariable("KAFKA_MAX_RATE_PER_PARTITION", "0"),
    new ParameterVariable("ES_INDEX_MAPPING", "sparta"),
    new ParameterVariable("MONGODB_DB", "sparta")
  )

  lazy val spartaFileEncoding: String =
    Properties.envOrElse(MarathonConstant.SpartaFileEncoding, MarathonConstant.DefaultFileEncodingSystemProperty)

  //Global Parameters
  val DefaultGlobalParameters = Seq(
    new ParameterVariable("DEFAULT_OUTPUT_FIELD", "raw"),
    new ParameterVariable("DEFAULT_DELIMITER", ","),
    new ParameterVariable("SPARK_EXECUTOR_BASE_IMAGE", "qa.stratio.com/stratio/spark-stratio-driver:2.2.0-2.0.0-ae1b428"),
    new ParameterVariable("SPARK_DRIVER_JAVA_OPTIONS", s"$spartaFileEncoding -Dconfig.file=/etc/sds/sparta/spark/reference.conf -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC -Dlog4j.configurationFile=file:///etc/sds/sparta/log4j2.xml -Djava.util.logging.config.file=file:///etc/sds/sparta/log4j2.xml"),
    new ParameterVariable("SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS", s"$spartaFileEncoding  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC"),
    new ParameterVariable("SPARK_STREAMING_CHECKPOINT_PATH", "sparta/checkpoint"),
    new ParameterVariable("SPARK_STREAMING_WINDOW", "2s"),
    new ParameterVariable("SPARK_STREAMING_BLOCK_INTERVAL", "100ms"),
    new ParameterVariable("SPARK_LOCAL_PATH", "/opt/spark/dist"),
    new ParameterVariable("SPARK_CORES_MAX", "2"),
    new ParameterVariable("SPARK_EXECUTOR_MEMORY", "2G"),
    new ParameterVariable("SPARK_EXECUTOR_CORES", "1"),
    new ParameterVariable("SPARK_DRIVER_CORES", "1"),
    new ParameterVariable("SPARK_DRIVER_MEMORY", "2G"),
    new ParameterVariable("SPARK_LOCALITY_WAIT", "100"),
    new ParameterVariable("SPARK_TASK_MAX_FAILURES", "8"),
    new ParameterVariable("SPARK_MEMORY_FRACTION", "0.6")
  )
}
