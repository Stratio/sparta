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
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant.SubmitMesosConstraintConf
import com.stratio.sparta.serving.core.constants.{AppConstant, SparkConstant}
import com.stratio.sparta.serving.core.helpers.{InfoHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow._
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
  lazy val useDynamicAuthentication = Try(scala.util.Properties.envOrElse(DynamicAuthEnv, "false").toBoolean)
    .getOrElse(false)
  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get
  lazy val marathonUpAndDownComponent = MarathonUpAndDownComponent(marathonConfig)
  lazy val cleanerActor: ActorRef = context.actorOf(Props(new EnvironmentCleanerActor()),
    s"$EnvironmentCleanerActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}")


  /* PUBLIC METHODS */

  import MarathonService._

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

  private def getUserDefinedLabels(workflow: Workflow): Map[String, String] = workflow.settings.global.
    marathonDeploymentSettings.fold(Map.empty[String, String]) { marathonSettings =>
    marathonSettings.userLabels.flatMap { kvPair => Option(kvPair.key.toString -> kvPair.value.toString) }.toMap
  }

  private def getUserDefinedEnvVariables(workflow: Workflow): Map[String, String] = {
    workflow.settings.global.
      marathonDeploymentSettings.fold(Map.empty[String, String]) { marathonSettings =>
      marathonSettings.logLevel.notBlank.fold(Map.empty[String, String]) {
        logLevel =>
          val level = logLevel.toString.toUpperCase
          Seq(sparkLogLevel -> level, spartaLogLevel -> level, spartaRedirectorLogLevel -> level).toMap
      } ++
        marathonSettings.userEnvVariables.flatMap { kvPair => Option(kvPair.key.toString -> kvPair.value.toString) }.toMap
    }
  }

  private def getWorkflowInheritedVariables: Map[String, String] = {
    val prefix = Properties.envOrElse(workflowLabelsPrefix, "INHERITED_TO_WORKFLOW")
    val labelFromPrefix = sys.env.filterKeys { key =>
      key.startsWith(prefix)
    }
    val fixedLabels = Properties.envOrNone(fixedWorkflowLabels).notBlank match {
      case Some(labels) =>
        labels.split(",").flatMap { label =>
          val splittedLabel = label.split("=")

          splittedLabel.headOption.map(key => key -> splittedLabel.lastOption.getOrElse(""))
        }.toMap
      case None => Map.empty[String, String]
    }

    fixedLabels ++ labelFromPrefix
  }

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
    val (dynamicAuthEnv, newSecrets) = {
      val appRoleName = Properties.envOrNone(AppRoleNameEnv)
      if (useDynamicAuthentication && appRoleName.isDefined) {
        (Map(AppRoleEnv -> write(Map("secret" -> "role"))), Map("role" -> Map("source" -> appRoleName.get)))
      } else (Map.empty[String, String], Map.empty[String, Map[String, String]])
    }

    val newHealthChecks: Option[Seq[MarathonHealthCheck]] = getHealthChecks(workflowModel)
    val healthCheckEnvVariables: Map[String, String] =
      Map(SpartaMarathonTotalTimeBeforeKill -> calculateMaxTimeout(newHealthChecks).toString)

    val newEnv = Option(
      envProperties(workflowModel, execution, marathonAppHeapSize) ++ envVariablesFromSubmit(submitExecution) ++ dynamicAuthEnv ++ vaultProperties ++
        gosecPluginProperties ++ xdProperties ++ xdGosecProperties ++ hadoopProperties ++ logLevelProperties ++ securityProperties ++
        calicoProperties ++ extraProperties ++ spartaExtraProperties ++ postgresProperties ++ zookeeperProperties ++ spartaConfigProperties ++
        intelligenceProperties ++ marathonAppProperties ++ configProperties ++ actorsProperties ++ marathonProperties ++ mesosRoleFromSubmit(submitExecution) ++
        getUserDefinedEnvVariables(workflowModel) ++ healthCheckEnvVariables ++ appIdentity
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
      if (WorkflowHelper.isCalicoEnabled)
        Option(
          Seq(
            DockerPortMapping(DefaultSparkUIPort, DefaultSparkUIPort, Option(0), protocol = "tcp"),
            DockerPortMapping(DefaultMetricsMarathonDriverPort, DefaultMetricsMarathonDriverPort, Option(0), protocol = "tcp", name = Option("metrics")),
            DockerPortMapping(DefaultJmxMetricsMarathonDriverPort, DefaultJmxMetricsMarathonDriverPort, Option(0), protocol = "tcp", name = Option("jmx"))
          ) ++
            app.container.docker.portMappings.getOrElse(Seq.empty)
        )
      else app.container.docker.portMappings
    }
    val networkType = if (WorkflowHelper.isCalicoEnabled) "USER" else app.container.docker.network
    val newDocker = app.container.docker.copy(
      image = spartaDockerImage,
      forcePullImage = Option(workflowModel.settings.global.marathonDeploymentSettings.fold(DefaultForcePullImage) {
        marathonSettings => marathonSettings.forcePullImage.getOrElse(DefaultForcePullImage)
      }),
      network = networkType,
      portMappings = newPortMappings,
      privileged = Option(workflowModel.settings.global.marathonDeploymentSettings.fold(DefaultPrivileged) {
        marathonSettings => marathonSettings.privileged.getOrElse(DefaultPrivileged)
      })
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
    val inputConstraints = getConstraint(workflowModel)
    val newConstraint = if (inputConstraints.isEmpty) None else Option(Seq(inputConstraints))
    val newIpAddress = {
      if (WorkflowHelper.isCalicoEnabled)
        Option(IpAddress(networkName = Properties.envOrNone(CalicoNetworkEnv)))
      else None
    }
    val newPortDefinitions = if (WorkflowHelper.isCalicoEnabled) None else app.portDefinitions

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
      labels = app.labels ++ companyBillingLabels ++ getWorkflowInheritedVariables ++ getUserDefinedLabels(workflowModel),
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

  private def marathonProperties: Map[String, String] =
    sys.env.filterKeys { key =>
      key.startsWith("SPARTA_MARATHON")
    }

  private def spartaExtraProperties: Map[String, String] =
    sys.env.filterKeys(key => key.startsWith("SPARTA_EXTRA")).map { case (key, value) =>
      key.replaceAll("SPARTA_EXTRA_", "") -> value
    }

  private def extraProperties: Map[String, String] =
    sys.env.filterKeys(key => key.contains("EXTRA_PROPERTIES"))

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

  private def appIdentity: Map[String, String] = {
    val workflowsIdentity = Properties.envOrNone(GenericWorkflowIdentity).getOrElse(spartaTenant)
    Map(
      DcosServiceName -> workflowsIdentity,
      SpartaPostgresUser -> workflowsIdentity,
      SystemHadoopUserName -> workflowsIdentity
    )
  }

  private def companyBillingLabels: Map[String, String] = {
    Properties.envOrNone(DcosServiceCompanyLabelPrefix).fold(Map.empty[String, String]) { companyPrefix =>
      sys.env.filterKeys { key =>
        key.contains(MarathonLabelPrefixEnv) && key.contains(companyPrefix)
      }.map { case (key, value) =>
        key.replaceAll(MarathonLabelPrefixEnv, "") -> value
      }
    }
  }

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
      GenericWorkflowIdentity -> Properties.envOrNone(GenericWorkflowIdentity),
      GosecAuthEnableEnv -> Properties.envOrNone(GosecAuthEnableEnv),
      UserNameEnv -> execution.genericDataExecution.userId,
      MarathonAppConstraints -> submitExecution.sparkConfigurations.get(SubmitMesosConstraintConf),
      SpartaZookeeperPathEnv -> Option(BaseZkPath)
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> value)) }.flatten.toMap
  }
}

object MarathonService {

  protected[core] def getHealthChecks(workflowModel: Workflow): Option[Seq[MarathonHealthCheck]] = {
    val workflowHealthcheckSettings =
      HealthChecks.fromObject(workflowModel.settings.global.marathonDeploymentSettings)

    Option(Seq(MarathonHealthCheck(
      protocol = "HTTP",
      path = Option("/environment"),
      portIndex = Option(0),
      gracePeriodSeconds = workflowHealthcheckSettings.gracePeriodSeconds,
      intervalSeconds = workflowHealthcheckSettings.intervalSeconds,
      timeoutSeconds = workflowHealthcheckSettings.timeoutSeconds,
      maxConsecutiveFailures = workflowHealthcheckSettings.maxConsecutiveFailures,
      ignoreHttp1xx = Option(false)
    )))
  }


  protected[core] def calculateMaxTimeout(healthChecks: Option[Seq[MarathonHealthCheck]]): Int =
    healthChecks.fold(AppConstant.DefaultAwaitWorkflowChangeStatusSeconds) {
      healthCk => {
        val applicationHck = healthCk.head
        import applicationHck._
        gracePeriodSeconds + (intervalSeconds + timeoutSeconds) * maxConsecutiveFailures
      }
    }

  case class HealthChecks(gracePeriodSeconds: Int = DefaultGracePeriodSeconds,
                          intervalSeconds: Int = DefaultIntervalSeconds,
                          timeoutSeconds: Int = DefaultTimeoutSeconds,
                          maxConsecutiveFailures: Int = DefaultMaxConsecutiveFailures)

  object HealthChecks {

    private def toIntOrElse(integerString: Option[JsoneyString], defaultValue: Int): Int =
      Try(integerString.map(_.toString.toInt).get).toOption.getOrElse(defaultValue)

    protected[core] def fromObject(settings: Option[MarathonDeploymentSettings]): HealthChecks =
      settings match {
        case None => HealthChecks()
        case Some(marathonSettings) =>
          HealthChecks(
            gracePeriodSeconds = toIntOrElse(marathonSettings.gracePeriodSeconds, DefaultGracePeriodSeconds),
            intervalSeconds = toIntOrElse(marathonSettings.intervalSeconds, DefaultIntervalSeconds),
            timeoutSeconds = toIntOrElse(marathonSettings.timeoutSeconds, DefaultTimeoutSeconds),
            maxConsecutiveFailures = toIntOrElse(marathonSettings.maxConsecutiveFailures, DefaultMaxConsecutiveFailures)
          )
      }
  }

}