/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.constants

import java.util

import akka.actor.ActorSystem
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.utils.RegexUtils._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.EntityAuthorization
import com.stratio.sparta.serving.core.models.enumerators.DataType
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterVariable}
import com.stratio.sparta.serving.core.models.workflow.{Group, WorkflowRelationSettings}
import com.stratio.sparta.serving.core.utils.ZookeeperUtils
import com.typesafe.config.Config
import org.joda.time.DateTime

import scala.util.{Properties, Try}

/**
  * Global constants of the application.
  */
object AppConstant extends ZookeeperUtils {

  val ConfigAppName = "sparta"
  val ConfigOauth2 = "oauth2"
  val ConfigAuthViaHeaders = "sparta.authWithHeaders"
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
  val ConfigGovernance = "governance"
  val ConfigS3 = "spark.hadoop.fs.s3a"
  val ConfigSftp = "spark.hadoop.fs.sftp"
  val ConfigBootstrap = "sparta.bootstrap"
  val ConfigDebug = "sparta.debug"
  val ConfigValidator = "sparta.validator"
  val ConfigCatalog = "sparta.catalog"
  val ConfigJwt = "jwt"
  val HdfsKey = "hdfs"
  val DefaultOauth2CookieName = "user"
  val DriverPackageLocation = "driverPackageLocation"
  val DefaultDriverPackageLocation = "/opt/sds/sparta/driver"
  val DriverURI = "driverURI"
  val DefaultMarathonDriverURI = "/opt/sds/sparta/driver/sparta-driver.jar"
  val DefaultDriverLocation = "provided"
  val DriverLocation = "driverLocation"
  val PluginsLocation = "pluginsLocation"
  val MockDataLocation = "mockDataLocation"
  val DefaultPluginsLocation = "plugins"
  val DefaultMockDataLocation = "mockData"
  val AwaitWorkflowChangeStatus = "awaitWorkflowChangeStatus"
  val SchedulerStopMaxCount = "scheduler.stop.maxCount"
  val DefaultSchedulerStopMaxCount = 3
  val ModelRepositoryUrlKey = "repository.url"
  val DefaultModelRepositoryUrl = "http://localhost:11000"
  val DefaultAwaitWorkflowChangeStatus = "360s"
  val DefaultAwaitWorkflowChangeStatusSeconds = 360
  val DefaultkillUrl = "http://127.0.0.1:7077/v1/submissions/kill"
  val DefaultGroup = Group(Option("940800b2-6d81-44a8-84d9-26913a2faea4"), "/home")
  val DefaultSystemGroup = Group(Option("66adb16e-fe6c-4464-91d3-403fe49f9f3c"), ".system")
  val DefaultPlannedQRWorkflowId = "f4d043c1-a015-4b04-b3ff-3c7d8f29e137"
  val DefaultApiTimeout = 20
  val DefaultVersion = "2.11.0"
  lazy val version = Try(SpartaConfig.getDetailConfig().get.getString("version"))
    .toOption.notBlank.getOrElse(DefaultVersion)

  //Debug Options
  val DebugSparkWindow = 1000
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
  val DefaultReloadKeyTab = true
  val DefaultReloadKeyTabTime = "23h"
  val SystemHadoopConfDir = "HADOOP_CONF_DIR"
  val CoreSite = "core-site.xml"
  val HDFSSite = "hdfs-site.xml"
  val SystemHadoopUserName = "HADOOP_USER_NAME"
  val SystemPrincipalName = "SPARTA_PRINCIPAL_NAME"
  val SystemPrincipalNameWorkflow = "SPARTA_PRINCIPAL_NAME_WORKFLOW"
  val SystemKeyTabPath = "SPARTA_KEYTAB_PATH"
  val SystemKeyTabPathWorkflow = "SPARTA_KEYTAB_PATH_WORKFLOW"
  val SystemHostName = "HOSTNAME"
  val DefaultFSProperty = "fs.defaultFS"

  val EosTenantEnv = "EOS_TENANT"

  val EosTenant: Option[String] = Properties.envOrNone(EosTenantEnv).notBlank
  val EosTenantHeader: String = EosTenant.getOrElse("NONE")
  val EosTenantNone = "NONE"
  val EosTenantOrNone: String = EosTenant.getOrElse(EosTenantNone)

  //Postgres
  val PostgresDaos = "com.stratio.sparta.serving.core.services.dao"

  //Service variables to identify
  val SpartaServiceName = "SPARTA_SERVICE_NAME"

  val instanceServiceName = Properties.envOrNone(SpartaServiceName)
  val tenantIdentity = Properties.envOrNone(EosTenantEnv)

  val instanceNameWithDefault = instanceServiceName.getOrElse("sparta-server")
  val tenantIdInstanceNameWithDefault = Seq(tenantIdentity, Option(instanceServiceName.getOrElse("sparta-server"))).flatten.mkString("-")

  val virtualHost = Properties.envOrNone(MarathonConstant.ServerMarathonLBHostEnv).notBlank
  val virtualPath = Properties.envOrNone(MarathonConstant.ServerMarathonLBPathEnv).notBlank

  //Tenant variable to use as identity
  val spartaTenant = Properties.envOrElse(MarathonConstant.TenantName, "sparta")

  //Multi-tenant sid
  lazy val spartaServerMarathonAppId = Properties.envOrElse(MarathonConstant.SpartaServerMarathonAppId, s"/sparta/$instanceNameWithDefault/$instanceNameWithDefault")

  //Security ON/OFF
  val securityTLSEnable = Try(Properties.envOrElse(MarathonConstant.SpartaTLSEnableEnv, "false").toBoolean).getOrElse(false)

  //Zookeeper
  val DefaultZKPath = "/stratio/sparta/sparta"
  lazy val BaseZkPath: String = (retrievePathFromEnv, instanceServiceName, retrieveFromConf) match {
    case (Some(path), _, _ ) if checkIfValidPath(path) => path
    case (_, Some(instance), _)=> s"/stratio/sparta/$instance"
    case (_, _, Some(confPath)) if checkIfValidPath(confPath) => confPath
    case _ => DefaultZKPath
  }
  lazy val ExecutionsStatusChangesZkPath = s"$BaseZkPath/executionStatusChanges"
  lazy val RunWorkflowZkPath = s"$BaseZkPath/runWorkflow"
  lazy val IgniteDiscoveryZkPath = s"$BaseZkPath/$retrieveIgnitePathFromEnv"

  //Migration
  lazy val WorkflowsZkPath = s"$BaseZkPath/workflows"
  lazy val WorkflowsOldZkPath = s"$BaseZkPath-old/workflows"
  lazy val WorkflowsOldCassiopeiaZkPath = s"$BaseZkPath-old/workflowsCassiopeia"
  lazy val WorkflowsOldZkAndromedaPath = s"$BaseZkPath-old/workflowsAndromeda"
  lazy val TemplatesZkPath = s"$BaseZkPath/templates"
  lazy val TemplatesOldZkPath = s"$BaseZkPath-old/templates"
  lazy val TemplatesOldCassiopeiaZkPath = s"$BaseZkPath-old/templatesCassiopeia"
  lazy val TemplatesOldAndromedaZkPath = s"$BaseZkPath-old/templatesAndromeda"
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

  lazy val ClusterSeedNodesZkPath = s"$BaseZkPath/seedNodes"

  //Scheduler system to schedule threads executions
  val SchedulerSystem = ActorSystem("SchedulerSystem", SpartaConfig.daemonicAkkaConfig)
  val CustomTypeKey = "customClassType"

  //Parameters
  val parametersTwoBracketsPattern = "\\{\\{[\\w\\.\\-\\_]*\\}\\}".r
  val regexMatchingMoustacheVariable = "(?<=\\{{2,3})([\\s*\\w\\-.+\\s]*)(?=\\}{2,3})".r

  //Environment Parameters
  val EnvironmentParameterListName = "Environment"
  val EnvironmentParameterListId = Option("f16e9034-ab81-11e8-98d0-529269fb1459")
  val DefaultEnvironmentParameters = Seq(
    ParameterVariable.create("ARANGODB_HOST", "localhost"),
    ParameterVariable.create("ARANGODB_PORT", "8529"),
    ParameterVariable.create("CROSSDATA_ZOOKEEPER_CONNECTION", "localhost:2181"),
    ParameterVariable.create("CROSSDATA_ZOOKEEPER_PATH", "/crossdata/offsets"),
    ParameterVariable.create("KAFKA_BROKER_HOST", "localhost"),
    ParameterVariable.create("KAFKA_BROKER_PORT", "9092"),
    ParameterVariable.create("CASSANDRA_HOST", "localhost"),
    ParameterVariable.create("CASSANDRA_PORT", "9042"),
    ParameterVariable.create("ES_HOST", "localhost"),
    ParameterVariable.create("ES_PORT", "9200"),
    ParameterVariable.create("ES_INDEX_MAPPING", "sparta"),
    ParameterVariable.create("ES_CLUSTER", "elasticsearch"),
    ParameterVariable.create("JDBC_URL", "jdbc:postgresql://dbserver:port/database?user=postgres"),
    ParameterVariable.create("JDBC_DRIVER", "org.postgresql.Driver"),
    ParameterVariable.create("POSTGRES_URL", "jdbc:postgresql://dbserver:port/database?user=postgres"),
    ParameterVariable.create("IGNITE_URL", "jdbc:ignite:thin://localhost:10800"),
    ParameterVariable.create("MONGODB_DB", "sparta"),
    ParameterVariable.create("MONGODB_HOST", "localhost"),
    ParameterVariable.create("MONGODB_PORT", "27017"),
    ParameterVariable.create("CASSANDRA_KEYSPACE", "sparta"),
    ParameterVariable.create("CASSANDRA_CLUSTER", "sparta"),
    ParameterVariable.create("KAFKA_GROUP_ID", "sparta"),
    ParameterVariable.create("KAFKA_MAX_POLL_TIMEOUT", "512"),
    ParameterVariable.create("KAFKA_MAX_RATE_PER_PARTITION", "0"),
    ParameterVariable.create("KAFKA_BROKER_HOST", "localhost"),
    ParameterVariable.create("KAFKA_BROKER_PORT", "9092"),
    ParameterVariable.create("KAFKA_MAX_POLL_TIMEOUT", "1000"),
    ParameterVariable.create("KAFKA_AUTO_COMMIT_INTERVAL", "5000"),
    ParameterVariable.create("KAFKA_MAX_PARTITION_FETCH_BYTES", "10485760"),
    ParameterVariable.create("KAFKA_SESSION_TIMEOUT", "30000"),
    ParameterVariable.create("KAFKA_REQUEST_TIMEOUT", "40000"),
    ParameterVariable.create("KAFKA_HEARTBEAT_INTERVAL", "10000"),
    ParameterVariable.create("KAFKA_FETCH_MAX_WAIT", "500"),
    ParameterVariable.create("KAFKA_RETRY_BACKOFF", "1000"),
    ParameterVariable.create("WEBSOCKET_URL", "ws://stream.meetup.com/2/rsvps"),
    ParameterVariable.create("REDIS_HOST", "localhost"),
    ParameterVariable.create("REDIS_PORT", "6379"),
    ParameterVariable.create("POSTGRES_DRIVER", "org.postgresql.Driver")
  )
  val DefaultEnvironmentParametersMap = ParameterList.parametersToMap(DefaultEnvironmentParameters)

  //Example Custom Group parameters
  val CustomExampleParameterList = "Default"
  val CustomExampleParameterListId = Option("1b8d86a8-c7d5-11e8-a8d5-f2801f1b9fd1")
  val DefaultCustomExampleParameters = Seq.empty[ParameterVariable]
  val DefaultCustomExampleParametersMap = ParameterList.parametersToMap(DefaultCustomExampleParameters)

  // Custom Planned QR parameters
  val DefaultPlannedQRParameterList = "PlannedQRSettings"

  lazy val resourcesPlannedQRConfig= SpartaConfig.getDetailConfig().map(_.getConfig("resources.default"))

  lazy val getDefaultExecutorCores =
    Try(resourcesPlannedQRConfig.get.getString("executor.cores").toInt).toOption.getOrElse(1)

  lazy val getDefaultExecutorMemory =
    Try(resourcesPlannedQRConfig.get.getString("executor.memory")).toOption.getOrElse("2G")

  lazy val getDefaultDriverCores =
    Try(resourcesPlannedQRConfig.get.getString("driver.cores").toInt).toOption.getOrElse(1)

  lazy val getDefaultDriverMemory =
    Try(resourcesPlannedQRConfig.get.getString("driver.memory")).toOption.getOrElse("2G")

  def increaseMemPow(baseMem: String, powerFactor: Double): String = {
    val (size, unit) = sizeAndUnit(baseMem)
    s"${math.pow(size.toDouble,powerFactor)}$unit"
  }

  def calculateSumExecutorsCores(coresPerExecutor: Int, powerFactor: Double, baseNumExecutors: Int = 2) : Int = {
    val numbExecutors = math.pow(baseNumExecutors.toDouble, powerFactor).toInt
    numbExecutors * coresPerExecutor
  }

  lazy val DefaultPlannedQRParameterListParameters = Seq(
    ParameterVariable.create("SPARK_CORES_MAX", getDefaultExecutorCores * 1),
    ParameterVariable.create("SPARK_EXECUTOR_MEMORY", getDefaultExecutorMemory),
    ParameterVariable.create("SPARK_EXECUTOR_CORES", getDefaultExecutorCores),
    ParameterVariable.create("SPARK_DRIVER_CORES", getDefaultDriverCores),
    ParameterVariable.create("SPARK_DRIVER_MEMORY", getDefaultDriverMemory),
    ParameterVariable.create("SPARK_LOCALITY_WAIT", "100"),
    ParameterVariable.create("SPARK_TASK_MAX_FAILURES", "8"),
    ParameterVariable.create("SPARK_MEMORY_FRACTION", "0.6")
  )

  // 1 executor, each with 2GB and 1 core, Driver with 2 GB and 1 cores
  val DefaultPlannedQRParameterListParameters_S = Seq(
    ParameterVariable.create("SPARK_CORES_MAX",
      calculateSumExecutorsCores(coresPerExecutor = getDefaultExecutorCores, powerFactor = 0.0)),
    ParameterVariable.create("SPARK_EXECUTOR_MEMORY", getDefaultExecutorMemory),
    ParameterVariable.create("SPARK_EXECUTOR_CORES", getDefaultExecutorCores),
    ParameterVariable.create("SPARK_DRIVER_CORES", getDefaultDriverCores),
    ParameterVariable.create("SPARK_DRIVER_MEMORY", getDefaultDriverMemory)
  )

  // 2 executors, each with 4GB and 2 cores, Driver with 4 GB and 2 cores
  val DefaultPlannedQRParameterListParameters_M = Seq(
    ParameterVariable.create("SPARK_CORES_MAX", calculateSumExecutorsCores(coresPerExecutor = 2, powerFactor = 1.0)),
    ParameterVariable.create("SPARK_EXECUTOR_MEMORY", increaseMemPow(getDefaultExecutorMemory, powerFactor = 2.0)),
    ParameterVariable.create("SPARK_EXECUTOR_CORES", 2),
    ParameterVariable.create("SPARK_DRIVER_CORES", 2),
    ParameterVariable.create("SPARK_DRIVER_MEMORY",  increaseMemPow(getDefaultDriverMemory, powerFactor = 2.0))
  )

  // 4 executors, each with 8GB and 2 cores, Driver with 8 GB and 2 cores
  val DefaultPlannedQRParameterListParameters_L = Seq(
    ParameterVariable.create("SPARK_CORES_MAX", calculateSumExecutorsCores(coresPerExecutor = 2, powerFactor = 2.0)),
    ParameterVariable.create("SPARK_EXECUTOR_MEMORY", increaseMemPow(getDefaultExecutorMemory, powerFactor = 3.0)),
    ParameterVariable.create("SPARK_EXECUTOR_CORES", 2),
    ParameterVariable.create("SPARK_DRIVER_CORES", 2),
    ParameterVariable.create("SPARK_DRIVER_MEMORY", increaseMemPow(getDefaultDriverMemory, powerFactor = 3.0))
  )

  // 8 executors, each with 16GB and 2 cores, Driver with 16 GB and 2 cores
  val DefaultPlannedQRParameterListParameters_XL = Seq(
    ParameterVariable.create("SPARK_CORES_MAX", calculateSumExecutorsCores(coresPerExecutor = 2, powerFactor = 3.0)),
    ParameterVariable.create("SPARK_EXECUTOR_MEMORY", increaseMemPow(getDefaultExecutorMemory, powerFactor = 4.0)),
    ParameterVariable.create("SPARK_EXECUTOR_CORES", 2),
    ParameterVariable.create("SPARK_DRIVER_CORES", 2),
    ParameterVariable.create("SPARK_DRIVER_MEMORY", increaseMemPow(getDefaultDriverMemory, powerFactor = 4.0))
  )

  lazy val customParameterList = ParameterList(
    id = CustomExampleParameterListId,
    name = CustomExampleParameterList,
    parameters = DefaultCustomExampleParameters
  )

  lazy val plannedQRParameterList: ParameterList =
    ParameterList(name = DefaultPlannedQRParameterList,
      id = Option("ea093cf2-363c-46cc-b229-33a0ebb88080"),
      parameters = DefaultPlannedQRParameterListParameters,
      parent = Option(DefaultPlannedQRParameterList),
      versionSparta = Option(DefaultVersion)
    )

  val smallSize = "S"
  val mediumSize = "M"
  val largeSize = "L"
  val extraLargeSize = "XL"

  val sizeResourcesPlannedQR = Set(smallSize, mediumSize, largeSize, extraLargeSize)

  lazy val plannedQRParameterListSmall: ParameterList =
    ParameterList(name = smallSize,
      id = Option("8b98ff82-0b28-4339-b654-a9e238181487"),
      parameters = DefaultPlannedQRParameterListParameters_S,
      parent = Option(DefaultPlannedQRParameterList),
      versionSparta = Option(DefaultVersion)
  )

  lazy val plannedQRParameterListMedium: ParameterList =
    ParameterList(name = mediumSize,
      id = Option("d21fb6fb-122c-40b7-bf7e-ca2bb96adf18"),
      parameters = DefaultPlannedQRParameterListParameters_M,
      parent = Option(DefaultPlannedQRParameterList),
      versionSparta = Option(DefaultVersion)
    )

  lazy val plannedQRParameterListLarge: ParameterList =
    ParameterList(name = largeSize,
      id = Option("bfc7d0d2-a4e6-42ee-914e-36edd7ccfd7d"),
      parameters = DefaultPlannedQRParameterListParameters_L,
      parent = Option(DefaultPlannedQRParameterList),
      versionSparta = Option(DefaultVersion)
    )

  lazy val plannedQRParameterListExtraLarge: ParameterList =
    ParameterList(name = extraLargeSize,
      id = Option("4e470a92-1869-42b6-8d39-fbba576e3119"),
      parameters = DefaultPlannedQRParameterListParameters_XL,
      parent = Option(DefaultPlannedQRParameterList),
      versionSparta = Option(DefaultVersion)
    )


  lazy val spartaFileEncoding: String =
    Properties.envOrElse(MarathonConstant.SpartaFileEncoding, MarathonConstant.DefaultFileEncodingSystemProperty)

  //Global Parameters
  val DefaultGlobalParameters = Seq(
    ParameterVariable.create("DEFAULT_OUTPUT_FIELD", "raw"),
    ParameterVariable.create("DEFAULT_DELIMITER", ","),
    ParameterVariable.create("SPARK_DRIVER_JAVA_OPTIONS", s"$spartaFileEncoding -Dconfig.file=/etc/sds/sparta/spark/reference.conf -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC -Dlog4j.configurationFile=file:///etc/sds/sparta/log4j2.xml -Djava.util.logging.config.file=file:///etc/sds/sparta/log4j2.xml"),
    ParameterVariable.create("SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS", s"$spartaFileEncoding  -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:+UseConcMarkSweepGC"),
    ParameterVariable.create("SPARK_STREAMING_CHECKPOINT_PATH", "sparta/checkpoint"),
    ParameterVariable.create("SPARK_STREAMING_WINDOW", "2s"),
    ParameterVariable.create("SPARK_STREAMING_BLOCK_INTERVAL", "100ms"),
    ParameterVariable.create("SPARK_LOCAL_PATH", "/opt/spark/dist"),
    ParameterVariable.create("SPARK_CORES_MAX", "2"),
    ParameterVariable.create("SPARK_EXECUTOR_MEMORY", "2G"),
    ParameterVariable.create("SPARK_EXECUTOR_CORES", "1"),
    ParameterVariable.create("SPARK_DRIVER_CORES", "1"),
    ParameterVariable.create("SPARK_DRIVER_MEMORY", "2G"),
    ParameterVariable.create("SPARK_LOCALITY_WAIT", "100"),
    ParameterVariable.create("SPARK_TASK_MAX_FAILURES", "8"),
    ParameterVariable.create("SPARK_MEMORY_FRACTION", "0.6")
  )
  val DefaultGlobalParametersMap = ParameterList.parametersToMap(DefaultGlobalParameters)
}