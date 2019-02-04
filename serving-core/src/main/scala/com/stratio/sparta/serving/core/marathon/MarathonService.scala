/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon

import java.util.{Calendar, UUID}

import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.actor.EnvironmentCleanerActor
import com.stratio.sparta.serving.core.actor.EnvironmentCleanerActor.TriggerCleaning
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant.EnvironmentCleanerActorName
import com.stratio.sparta.serving.core.constants.{AppConstant, SparkConstant}
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant.SubmitMesosConstraintConf
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{SparkSubmitExecution, Workflow, WorkflowExecution}
import com.stratio.sparta.serving.core.services.SparkSubmitService
import com.stratio.sparta.serving.core.utils.MarathonOauthTokenUtils._
import com.typesafe.config.Config
import org.json4s.jackson.Serialization._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Properties, Try}

//scalastyle:off
case class MarathonService(context: ActorContext) extends SpartaSerializer {

  /* Implicit variables */

  implicit val actorSystem: ActorSystem = context.system
  val timeoutConfig = Try(SpartaConfig.getDetailConfig().get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1
  implicit val timeout: Timeout = Timeout(timeoutConfig.seconds)
  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

  /* Constant variables */

  val appInfo = InfoHelper.getAppInfo
  val versionParsed = if (appInfo.pomVersion != "${project.version}") appInfo.pomVersion else version
  val DefaultSpartaDockerImage = s"qa.stratio.com/stratio/sparta:$versionParsed"
  val DefaultMaxTimeOutInMarathonRequests = 5000

  /* Lazy variables */
  lazy val calicoEnabled: Boolean = {
    val calicoEnabled = Properties.envOrNone(CalicoEnableEnv)
    val calicoNetwork = Properties.envOrNone(CalicoNetworkEnv).notBlank
    if (calicoEnabled.isDefined && calicoEnabled.get.equals("true") && calicoNetwork.isDefined) true else false
  }
  lazy val useDynamicAuthentication = Try(scala.util.Properties.envOrElse(DynamicAuthEnv, "false").toBoolean)
    .getOrElse(false)
  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get
  lazy val marathonUpAndDownComponent = MarathonUpAndDownComponent(marathonConfig)
  lazy val cleanerActor: ActorRef = context.actorOf(Props(new EnvironmentCleanerActor()),
    s"$EnvironmentCleanerActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}")


  /* PUBLIC METHODS */

  def launch(execution: WorkflowExecution): Unit = {

    require(execution.marathonExecution.isDefined, "It is mandatory that the execution contains marathon settings")

    val workflow = execution.getWorkflowToExecute
    val marathonApplication = createMarathonApplication(workflow, execution)
    val launchRequest = marathonUpAndDownComponent.upApplication(marathonApplication, Try(getToken).toOption)

    Await.result(launchRequest, DefaultMaxTimeOutInMarathonRequests seconds) match {
      case response: (String, String) =>
        log.info(s"Workflow App ${marathonApplication.id} launched with status code ${response._1} and response ${response._2}")
      case _ =>
        log.info(s"Workflow App ${marathonApplication.id} launched but the response is not serializable")
    }
  }

  def kill(containerId: String): Unit = {

    val killRequest = marathonUpAndDownComponent.killDeploymentsAndDownApplication(containerId, Try(getToken).toOption)

    Await.result(killRequest, DefaultMaxTimeOutInMarathonRequests seconds) match {
      case response: Seq[(String, String)] =>
        cleanerActor ! TriggerCleaning
        log.info(s"Workflow App $containerId correctly killed with responses: ${response.mkString(",")}")
      case _ =>
        cleanerActor ! TriggerCleaning
        log.info(s"Workflow App $containerId killed but the response is not serializable")
    }
  }

  /* PRIVATE METHODS */

  private def marathonJar: Option[String] =
    Try(marathonConfig.getString("jar")).toOption.orElse(Option(AppConstant.DefaultMarathonDriverURI))

  private def ldNativeLibrary: Option[String] = Properties.envOrNone(MesosNativeJavaLibraryEnv) match {
    case Some(_) => None
    case None => Option(HostMesosLib)
  }

  private def getVaultToken: Option[String] =
    if (!useDynamicAuthentication) Properties.envOrNone(VaultTokenEnv)
    else None

  private def mesosphereLibPath: String =
    Try(marathonConfig.getString("mesosphere.lib")).toOption.getOrElse(HostMesosNativeLibPath)

  private def mesospherePackagesPath: String =
    Try(marathonConfig.getString("mesosphere.packages")).toOption.getOrElse(HostMesosNativePackagesPath)

  private def spartaDockerImage: String =
    Try(marathonConfig.getString("docker.image")).toOption.getOrElse(DefaultSpartaDockerImage)

  private def gracePeriodSeconds: Int =
    Try(marathonConfig.getInt("gracePeriodSeconds")).toOption.getOrElse(DefaultGracePeriodSeconds)

  private def intervalSeconds: Int =
    Try(marathonConfig.getInt("intervalSeconds")).toOption.getOrElse(DefaultIntervalSeconds)

  private def timeoutSeconds: Int =
    Try(marathonConfig.getInt("timeoutSeconds")).toOption.getOrElse(DefaultTimeoutSeconds)

  private def maxConsecutiveFailures: Int =
    Try(marathonConfig.getInt("maxConsecutiveFailures")).toOption.getOrElse(DefaultMaxConsecutiveFailures)

  private def forcePullImageConf: Boolean =
    Try(marathonConfig.getString("docker.forcePullImage").toBoolean).getOrElse(DefaultForcePullImage)

  private def privileged: Boolean =
    Try(marathonConfig.getString("docker.privileged").toBoolean).getOrElse(DefaultPrivileged)

  private def transformMemoryToInt(memory: String): Int = Try(memory match {
    case mem if mem.contains("G") => mem.replace("G", "").toInt * 1024
    case mem if mem.contains("g") => mem.replace("g", "").toInt * 1024
    case mem if mem.contains("m") => mem.replace("m", "").toInt
    case mem if mem.contains("M") => mem.replace("M", "").toInt
    case _ => memory.toInt
  }).getOrElse(DefaultMemory)

  private def getSOMemory: Int =
    Properties.envOrNone(SpartaOSMemoryEnv) match {
      case Some(x) =>
        Try(x.toInt).filter(y => y >= MinSOMemSize).getOrElse(DefaultSOMemSize)
      case None => DefaultSOMemSize
    }

  private def getConstraint(workflowModel: Workflow): Seq[String] = {
    val envConstraints = Seq(
      Properties.envOrNone(HostnameConstraint).notBlank,
      Properties.envOrNone(OperatorConstraint).notBlank,
      Properties.envOrNone(AttributeConstraint).notBlank
    ).flatten

    (workflowModel.settings.global.mesosConstraint.notBlank, envConstraints) match {
      case (Some(workflowConstraint), _) =>
        val constraints = workflowConstraint.split(":")
        Seq(
          Option(constraints.head),
          Option(workflowModel.settings.global.mesosConstraintOperator.notBlankWithDefault("CLUSTER")), {
            if (constraints.size == 2 || constraints.size == 3) Option(constraints.last) else None
          }
        ).flatten
      case (None, constraints) if constraints.size == 3 =>
        constraints
      case _ =>
        Seq.empty[String]
    }
  }

  private def mesosRoleFromSubmit(sparkSubmitExecution: SparkSubmitExecution): Map[String, String] = {
    sparkSubmitExecution.sparkConfigurations.flatMap { case (key, value) =>
      if (key == SparkConstant.SubmitMesosRoleConf) {
        Option(MesosRoleEnv, value)
      } else None
    }
  }

  private def envVariablesFromSubmit(sparkSubmitExecution: SparkSubmitExecution): Map[String, String] = {
    sparkSubmitExecution.sparkConfigurations.flatMap { case (key, value) =>
      if (key.startsWith("spark.mesos.driverEnv.")) {
        Option((key.split("spark.mesos.driverEnv.").tail.head, value))
      } else None
    }
  }

  //scalastyle:off
  private def createMarathonApplication(workflowModel: Workflow, execution: WorkflowExecution): MarathonApplication = {
    val app = MarathonApplication.defaultMarathonApplication
    val submitExecution = execution.sparkSubmitExecution.get
    val newCpus = submitExecution.sparkConfigurations.get("spark.driver.cores").map(_.toDouble).getOrElse(app.cpus)
    val newMem = submitExecution.sparkConfigurations.get("spark.driver.memory").map(transformMemoryToInt).getOrElse(app.mem) + getSOMemory
    val marathonAppHeapSize = Try(getSOMemory / 2).getOrElse(512)
    val envFromSubmit = submitExecution.sparkConfigurations.flatMap { case (key, value) =>
      if (key.startsWith("spark.mesos.driverEnv.")) {
        Option((key.split("spark.mesos.driverEnv.").tail.head, value))
      } else None
    }
    val (dynamicAuthEnv, newSecrets) = {
      val appRoleName = Properties.envOrNone(AppRoleNameEnv)
      if (useDynamicAuthentication && appRoleName.isDefined) {
        (Map(AppRoleEnv -> write(Map("secret" -> "role"))), Map("role" -> Map("source" -> appRoleName.get)))
      } else (Map.empty[String, String], Map.empty[String, Map[String, String]])
    }
    val newEnv = Option(
      envProperties(workflowModel, execution, marathonAppHeapSize) ++ envFromSubmit ++ dynamicAuthEnv ++ vaultProperties ++
        gosecPluginProperties ++ xdProperties ++ xdGosecProperties ++ hadoopProperties ++ logLevelProperties ++ securityProperties ++
        calicoProperties ++ extraProperties ++ spartaExtraProperties ++ postgresProperties ++ zookeeperProperties ++ spartaConfigProperties ++
        intelligenceProperties ++ marathonAppProperties ++ configProperties ++ actorsProperties ++ marathonProperties ++ mesosRoleFromSubmit(submitExecution)
    )
    val javaCertificatesVolume = {
      if (!Try(marathonConfig.getString("docker.includeCertVolumes").toBoolean).getOrElse(DefaultIncludeCertVolumes) ||
        (Properties.envOrNone(VaultEnableEnv).isDefined && Properties.envOrNone(VaultHostsEnv).isDefined &&
          Properties.envOrNone(VaultTokenEnv).isDefined))
        Seq.empty[MarathonVolume]
      else Seq(
        MarathonVolume(ContainerCertificatePath, HostCertificatePath, "RO"),
        MarathonVolume(ContainerJavaCertificatePath, HostJavaCertificatePath, "RO")
      )
    }
    val commonVolumes: Seq[MarathonVolume] = {
      if (Try(marathonConfig.getString("docker.includeCommonVolumes").toBoolean).getOrElse(DefaultIncludeCommonVolumes))
        Seq(MarathonVolume(ResolvConfigFile, ResolvConfigFile, "RO"), MarathonVolume(Krb5ConfFile, Krb5ConfFile, "RO"))
      else Seq.empty[MarathonVolume]
    }

    val newPortMappings = {
      if (calicoEnabled)
        Option(
          Seq(
            DockerPortMapping(DefaultSparkUIPort, DefaultSparkUIPort, Option(0), protocol = "tcp")
          ) ++
            app.container.docker.portMappings.getOrElse(Seq.empty)
        )
      else app.container.docker.portMappings
    }
    val networkType = if (calicoEnabled) "USER" else app.container.docker.network
    val newDocker = app.container.docker.copy(
      image = spartaDockerImage,
      forcePullImage = Option(forcePullImageConf),
      network = networkType,
      portMappings = newPortMappings,
      privileged = Option(privileged)
    )
    val newContainerVolumes = Properties.envOrNone(MesosNativeJavaLibraryEnv) match {
      case Some(_) =>
        Option(app.container.volumes.getOrElse(Seq.empty) ++ javaCertificatesVolume ++ commonVolumes)
      case None =>
        Option(
          app.container.volumes.getOrElse(Seq.empty) ++
            Seq(
              MarathonVolume(HostMesosNativeLibPath, mesosphereLibPath, "RO"),
              MarathonVolume(HostMesosNativePackagesPath, mesospherePackagesPath, "RO")
            ) ++ javaCertificatesVolume ++ commonVolumes
        )

    }
    val newDockerContainerInfo = MarathonContainer(docker = newDocker, volumes = newContainerVolumes)
    val newHealthChecks = Option(Seq(MarathonHealthCheck(
      protocol = "HTTP",
      path = Option("/environment"),
      portIndex = Option(0),
      gracePeriodSeconds = gracePeriodSeconds,
      intervalSeconds = intervalSeconds,
      timeoutSeconds = timeoutSeconds,
      maxConsecutiveFailures = maxConsecutiveFailures,
      ignoreHttp1xx = Option(false)
    )))
    val inputConstraints = getConstraint(workflowModel)
    val newConstraint = if (inputConstraints.isEmpty) None else Option(Seq(inputConstraints))
    val newIpAddress = {
      if (calicoEnabled)
        Option(IpAddress(networkName = Properties.envOrNone(CalicoNetworkEnv)))
      else None
    }
    val newPortDefinitions = if (calicoEnabled) None else app.portDefinitions


    app.copy(
      id = execution.marathonExecution.get.marathonId,
      cpus = newCpus,
      mem = newMem,
      env = newEnv,
      container = newDockerContainerInfo,
      healthChecks = newHealthChecks,
      secrets = newSecrets,
      portDefinitions = newPortDefinitions,
      ipAddress = newIpAddress,
      constraints = newConstraint
    )
  }

  //scalastyle:on

  private def gosecPluginProperties: Map[String, String] = {
    val invalid = Seq(
      "SPARTA_PLUGIN_LOCAL_HOSTNAME",
      "SPARTA_PLUGIN_LDAP_CREDENTIALS",
      "SPARTA_PLUGIN_LDAP_PRINCIPAL"
    )

    sys.env.filterKeys(key => key.startsWith("SPARTA_PLUGIN") && !invalid.contains(key))
  }

  private def extraProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("EXTRA_PROPERTIES")
    }

  private def marathonProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("SPARTA_MARATHON")
    }

  private def spartaExtraProperties: Map[String, String] =
    sys.env.filterKeys(key => key.startsWith("SPARTA_EXTRA")).map { case (key, value) =>
      key.replaceAll("SPARTA_EXTRA_", "") -> value
    }

  private def vaultProperties: Map[String, String] =
    sys.env.filterKeys(key => key.startsWith("VAULT") && key != VaultTokenEnv)

  private def calicoProperties: Map[String, String] =
    sys.env.filterKeys(key => key.startsWith("CALICO"))

  private def logLevelProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.contains("LOG_LEVEL") || key.startsWith("LOG")
    }

  private def securityProperties: Map[String, String] =
    sys.env.filterKeys(key => key.startsWith("SECURITY") && key.contains("ENABLE"))

  private def hadoopProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("HADOOP") || key.startsWith("CORE_SITE") || key.startsWith("HDFS_SITE") ||
        key.startsWith("HDFS")
    }

  private def xdProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("CROSSDATA_CORE_CATALOG") ||
        key.startsWith("CROSSDATA_STORAGE") ||
        key.startsWith("CROSSDATA_SECURITY")
    }

  private def xdGosecProperties: Map[String, String] = {
    sys.env.filterKeys { key =>
      key.startsWith("CROSSDATA_SECURITY_MANAGER_ENABLED") ||
        key.startsWith("GOSEC_CROSSDATA_VERSION") ||
        key.startsWith("CROSSDATA_PLUGIN_SERVICE_NAME") ||
        key.startsWith("DYPLON_SYSTEM_TENANT") ||
        key.startsWith("DYPLON_TENANT_NAME")
    }
  }

  private def postgresProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("SPARTA_POSTGRES")
    }

  private def configProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("SPARTA_CONFIG")
    }

  private def actorsProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("SPARTA_ACTORS")
    }

  private def zookeeperProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("SPARTA_ZOOKEEPER")
    }

  private def spartaConfigProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.contains("SPARTA_TIMEOUT_API_CALLS")
    }

  private def intelligenceProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.contains("INTELLIGENCE")
    }

  private def marathonAppProperties: Map[String, String] =
    sys.env.filterKeys(key => key.startsWith("MARATHON_APP"))


  private def envProperties(workflow: Workflow, execution: WorkflowExecution, marathonAppHeapSize: Int): Map[String, String] = {
    val submitExecution = execution.sparkSubmitExecution.get

    Map(
      AppMainEnv -> Option(AppMainClass),
      AppTypeEnv -> Option(MarathonApp),
      MesosNativeJavaLibraryEnv -> Properties.envOrNone(MesosNativeJavaLibraryEnv).orElse(Option(HostMesosNativeLib)),
      LdLibraryEnv -> ldNativeLibrary,
      AppJarEnv -> marathonJar,
      VaultTokenEnv -> getVaultToken,
      PluginFiles -> Option(submitExecution.pluginFiles.mkString(",")),
      ExecutionIdEnv -> submitExecution.driverArguments.get(SparkSubmitService.ExecutionIdKey),
      DynamicAuthEnv -> Properties.envOrNone(DynamicAuthEnv),
      SpartaFileEncoding -> Some(Properties.envOrElse(SpartaFileEncoding, DefaultFileEncodingSystemProperty)),
      AppHeapSizeEnv -> Option(s"-Xmx${marathonAppHeapSize}m"),
      SparkHomeEnv -> Properties.envOrNone(SparkHomeEnv),
      DatastoreCaNameEnv -> Properties.envOrSome(DatastoreCaNameEnv, Option("ca")),
      SpartaSecretFolderEnv -> Properties.envOrNone(SpartaSecretFolderEnv),
      DcosServiceName -> Properties.envOrNone(DcosServiceName),
      GosecAuthEnableEnv -> Properties.envOrNone(GosecAuthEnableEnv),
      UserNameEnv -> execution.genericDataExecution.userId,
      MarathonAppConstraints -> submitExecution.sparkConfigurations.get(SubmitMesosConstraintConf),
      SpartaZookeeperPathEnv -> Option(BaseZkPath)
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap
  }

}
