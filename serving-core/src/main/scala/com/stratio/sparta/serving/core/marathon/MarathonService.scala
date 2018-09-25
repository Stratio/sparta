/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon

import java.util.{Calendar, UUID}

import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.actor.EnvironmentCleanerActor
import com.stratio.sparta.serving.core.actor.EnvironmentCleanerActor.TriggerCleaning
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant.EnvironmentCleanerActorName
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant.SubmitMesosConstraintConf
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.helpers.{InfoHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.marathon.OauthTokenUtils._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowExecution}
import com.stratio.sparta.serving.core.services.SparkSubmitService
import com.stratio.tikitakka.common.message._
import com.stratio.tikitakka.common.model._
import com.stratio.tikitakka.core.UpAndDownActor
import com.stratio.tikitakka.updown.UpAndDownComponent
import com.typesafe.config.Config
import org.json4s.jackson.Serialization._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Properties, Try}

//scalastyle:off
case class MarathonService(context: ActorContext, execution: Option[WorkflowExecution]) extends SpartaSerializer {

  def this(context: ActorContext, execution: WorkflowExecution) =
    this(context, Option(execution))

  def this(context: ActorContext) = this(context, None)

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

  /* Lazy variables */
  lazy val calicoEnabled: Boolean = {
    val calicoEnabled = Properties.envOrNone(CalicoEnableEnv)
    val calicoNetwork = Properties.envOrNone(CalicoNetworkEnv).notBlank
    if (calicoEnabled.isDefined && calicoEnabled.get.equals("true") && calicoNetwork.isDefined) true else false
  }
  lazy val useDynamicAuthentication = Try(scala.util.Properties.envOrElse(DynamicAuthEnv, "false").toBoolean)
    .getOrElse(false)
  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get
  lazy val upAndDownComponent: UpAndDownComponent = SpartaMarathonComponent.apply
  lazy val upAndDownActor: ActorRef = actorSystem.actorOf(Props(new UpAndDownActor(upAndDownComponent)),
    s"${AkkaConstant.UpDownMarathonActor}-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}")
  lazy val cleanerActor: ActorRef = actorSystem.actorOf(Props(new EnvironmentCleanerActor()),
    s"$EnvironmentCleanerActorName-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}")


  /* PUBLIC METHODS */

  def launch(): Unit = {
    require(execution.isDefined, "It is mandatory to specify a execution")
    require(execution.get.marathonExecution.isDefined, "It is mandatory that the execution contains marathon settings")
    val workflow = execution.get.getWorkflowToExecute
    val createApp = addRequirements(getMarathonAppFromFile, workflow, execution.get)
    log.info(s"Submitting Marathon application: ${write(createApp)}")
    for {
      response <- (upAndDownActor ? UpServiceRequest(createApp, Try(getToken).toOption)).mapTo[UpAndDownMessage]
    } response match {
      case response: UpServiceFails =>
        val information = s"Workflow App ${response.appInfo.id} cannot be deployed: ${response.msg}"
        log.error(information)
        throw new Exception(information)
      case response: UpServiceResponse =>
        log.info(s"Workflow App correctly launched to Marathon API with id: ${response.appInfo.id}")
    }
  }

  def kill(containerId: String): Unit = {
    for {
      response <- (upAndDownActor ? DownServiceRequest(ContainerId(containerId), Try(getToken).toOption))
        .mapTo[UpAndDownMessage]
    } response match {
      case response: DownServiceFails =>
        val information = s"Workflow App ${response.appInfo.id} cannot be killed: ${response.msg}"
        log.error(information)
        cleanerActor ! TriggerCleaning
        throw new Exception(information)
      case response: DownServiceResponse =>
        log.info(s"Workflow App correctly killed with Marathon API and id: ${response.appInfo.id}")
        cleanerActor ! TriggerCleaning
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

  private def forcePullImage: Boolean =
    Try(marathonConfig.getString("docker.forcePullImage").toBoolean).getOrElse(DefaultForcePullImage)

  private def privileged: Boolean =
    Try(marathonConfig.getString("docker.privileged").toBoolean).getOrElse(DefaultPrivileged)

  private def getMarathonAppFromFile: CreateApp = {
    val templateFile = Try(marathonConfig.getString("template.file")).toOption.getOrElse(DefaultMarathonTemplateFile)
    val fileContent = Source.fromFile(templateFile).mkString
    Json.parse(fileContent).as[CreateApp]
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

  //scalastyle:off
  private def addRequirements(app: CreateApp, workflowModel: Workflow, execution: WorkflowExecution): CreateApp = {
    val submitExecution = execution.sparkSubmitExecution.get
    val newCpus = submitExecution.sparkConfigurations.get("spark.driver.cores")
      .map(_.toDouble).getOrElse(app.cpus)
    val newMem = submitExecution.sparkConfigurations.get("spark.driver.memory")
      .map(transformMemoryToInt).getOrElse(app.mem)
    val envFromSubmit = submitExecution.sparkConfigurations.flatMap { case (key, value) =>
      if (key.startsWith("spark.mesos.driverEnv.")) {
        Option((key.split("spark.mesos.driverEnv.").tail.head, JsString(value)))
      } else None
    }
    val (dynamicAuthEnv, newSecrets) = {
      val appRoleName = Properties.envOrNone(AppRoleNameEnv)
      if (useDynamicAuthentication && appRoleName.isDefined) {
        log.info("Adding dynamic authentication parameters to marathon application")
        (Map(AppRoleEnv -> JsObject(Map("secret" -> JsString("role")))), Map("role" -> Map("source" -> appRoleName.get)))
      } else (Map.empty[String, JsString], Map.empty[String, Map[String, String]])
    }
    val newEnv = Option(
      envProperties(workflowModel, execution, newMem) ++ envFromSubmit ++ dynamicAuthEnv ++ vaultProperties ++
        gosecPluginProperties ++ xdProperties ++ hadoopProperties ++ logLevelProperties ++ securityProperties ++
        calicoProperties ++ extraProperties ++ postgresProperties ++ zookeeperProperties ++ spartaConfigProperties ++
        intelligenceProperties
    )
    val javaCertificatesVolume = {
      if (!Try(marathonConfig.getString("docker.includeCertVolumes").toBoolean).getOrElse(DefaultIncludeCertVolumes) ||
        (Properties.envOrNone(VaultEnableEnv).isDefined && Properties.envOrNone(VaultHostsEnv).isDefined &&
          Properties.envOrNone(VaultTokenEnv).isDefined))
        Seq.empty[Volume]
      else Seq(
        Volume(ContainerCertificatePath, HostCertificatePath, "RO"),
        Volume(ContainerJavaCertificatePath, HostJavaCertificatePath, "RO")
      )
    }
    val commonVolumes: Seq[Volume] = {
      if (Try(marathonConfig.getString("docker.includeCommonVolumes").toBoolean).getOrElse(DefaultIncludeCommonVolumes))
        Seq(Volume(ResolvConfigFile, ResolvConfigFile, "RO"), Volume(Krb5ConfFile, Krb5ConfFile, "RO"))
      else Seq.empty[Volume]
    }

    val newPortMappings = {
      if (calicoEnabled)
        Option(
          Seq(PortMapping(DefaultSparkUIPort, DefaultSparkUIPort, Option(0), protocol = Option("tcp"))) ++
            app.container.docker.portMappings.getOrElse(Seq.empty)
        )
      else app.container.docker.portMappings
    }
    val networkType = if (calicoEnabled) Option("USER") else app.container.docker.network
    val newDockerContainerInfo = Properties.envOrNone(MesosNativeJavaLibraryEnv) match {
      case Some(_) =>
        ContainerInfo(app.container.docker.copy(
          image = spartaDockerImage,
          volumes = Option(app.container.docker.volumes.getOrElse(Seq.empty) ++ javaCertificatesVolume ++ commonVolumes),
          forcePullImage = Option(forcePullImage),
          network = networkType,
          portMappings = newPortMappings,
          privileged = Option(privileged)
        ))
      case None =>
        ContainerInfo(app.container.docker.copy(
          volumes = Option(app.container.docker.volumes.getOrElse(Seq.empty) ++ Seq(
            Volume(HostMesosNativeLibPath, mesosphereLibPath, "RO"),
            Volume(HostMesosNativePackagesPath, mesospherePackagesPath, "RO")
          ) ++ javaCertificatesVolume ++ commonVolumes),
          image = spartaDockerImage,
          forcePullImage = Option(forcePullImage),
          network = networkType,
          portMappings = newPortMappings,
          privileged = Option(privileged)
        ))
    }
    val newHealthChecks = Option(Seq(HealthCheck(
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
      mem = newMem + getSOMemory,
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

  private def gosecPluginProperties: Map[String, JsString] = {
    val invalid = Seq(
      "SPARTA_PLUGIN_LOCAL_HOSTNAME",
      "SPARTA_PLUGIN_LDAP_CREDENTIALS",
      "SPARTA_PLUGIN_LDAP_PRINCIPAL"
    )
    sys.env
      .filterKeys(key => key.startsWith("SPARTA_PLUGIN") && !invalid.contains(key))
      .mapValues(value => JsString(value))
  }

  private def extraProperties: Map[String, JsString] =
    sys.env.filterKeys(key => key.startsWith("SPARTA_EXTRA")).map { case (key, value) =>
      key.replaceAll("SPARTA_EXTRA_", "") -> JsString(value)
    }

  private def vaultProperties: Map[String, JsString] =
    sys.env.filterKeys(key => key.startsWith("VAULT") && key != VaultTokenEnv).mapValues(value => JsString(value))

  private def calicoProperties: Map[String, JsString] =
    sys.env.filterKeys(key => key.startsWith("CALICO")).mapValues(value => JsString(value))

  private def logLevelProperties: Map[String, JsString] =
    sys.env.filterKeys { key =>
      key.contains("LOG_LEVEL") || key.startsWith("LOG")
    }.mapValues(value => JsString(value))

  private def securityProperties: Map[String, JsString] =
    sys.env.filterKeys(key => key.startsWith("SECURITY") && key.contains("ENABLE")).mapValues(value => JsString(value))

  private def hadoopProperties: Map[String, JsString] =
    sys.env.filterKeys { key =>
      key.startsWith("HADOOP") || key.startsWith("CORE_SITE") || key.startsWith("HDFS_SITE") ||
        key.startsWith("HDFS")
    }.mapValues(value => JsString(value))

  private def xdProperties: Map[String, JsString] =
    sys.env.filterKeys { key =>
      key.startsWith("CROSSDATA_CORE_CATALOG") ||
        key.startsWith("CROSSDATA_STORAGE") ||
        key.startsWith("CROSSDATA_SECURITY")
    }.mapValues(value => JsString(value))

  private def postgresProperties: Map[String, JsString] =
    sys.env.filterKeys { key =>
      key.startsWith("SPARTA_POSTGRES")
    }.mapValues(value => JsString(value))

  private def zookeeperProperties: Map[String, JsString] =
    sys.env.filterKeys { key =>
      key.startsWith("SPARTA_ZOOKEEPER")
    }.mapValues(value => JsString(value))

  private def spartaConfigProperties: Map[String, JsString] =
    sys.env.filterKeys { key =>
      key.contains("SPARTA_TIMEOUT_API_CALLS")
    }.mapValues(value => JsString(value))

  private def intelligenceProperties: Map[String, JsString] =
    sys.env.filterKeys { key =>
      key.startsWith("INTELLIGENCE")
    }.mapValues(value => JsString(value))

  private def envProperties(workflow: Workflow, execution: WorkflowExecution, memory: Int): Map[String, JsString] = {
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
      AppHeapSizeEnv -> Option(s"-Xmx${memory}m"),
      SparkHomeEnv -> Properties.envOrNone(SparkHomeEnv),
      DatastoreCaNameEnv -> Properties.envOrSome(DatastoreCaNameEnv, Option("ca")),
      SpartaSecretFolderEnv -> Properties.envOrNone(SpartaSecretFolderEnv),
      DcosServiceName -> Properties.envOrNone(DcosServiceName),
      GosecAuthEnableEnv -> Properties.envOrNone(GosecAuthEnableEnv),
      UserNameEnv -> execution.genericDataExecution.userId,
      MarathonAppConstraints -> submitExecution.sparkConfigurations.get(SubmitMesosConstraintConf),
      SpartaZookeeperPathEnv -> Option(BaseZkPath)
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> JsString(value))) }.flatten.toMap
  }

}
