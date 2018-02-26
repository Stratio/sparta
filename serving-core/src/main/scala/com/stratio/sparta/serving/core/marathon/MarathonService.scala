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

package com.stratio.sparta.serving.core.marathon

import java.util.{Calendar, UUID}

import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant.SubmitMesosConstraintConf
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.helpers.{InfoHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowExecution}
import com.stratio.tikitakka.common.message._
import com.stratio.tikitakka.common.model._
import com.stratio.tikitakka.core.UpAndDownActor
import com.stratio.tikitakka.updown.UpAndDownComponent
import com.typesafe.config.Config
import org.apache.curator.framework.CuratorFramework
import play.api.libs.json._
import OauthTokenUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Properties, Try}

//scalastyle:off
class MarathonService(context: ActorContext,
                      val curatorFramework: CuratorFramework,
                      workflowModel: Option[Workflow],
                      sparkSubmitRequest: Option[WorkflowExecution]) {

  def this(context: ActorContext,
           curatorFramework: CuratorFramework,
           workflowModel: Workflow,
           sparkSubmitRequest: WorkflowExecution) =
    this(context, curatorFramework, Option(workflowModel), Option(sparkSubmitRequest))

  def this(context: ActorContext, curatorFramework: CuratorFramework) = this(context, curatorFramework, None, None)

  /* Implicit variables */

  implicit val actorSystem: ActorSystem = context.system
  val timeoutConfig = Try(SpartaConfig.getDetailConfig.get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1
  implicit val timeout: Timeout = Timeout(timeoutConfig.seconds)
  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

  /* Constant variables */

  val AppMainClass = "com.stratio.sparta.driver.MarathonDriver"
  val DefaultMarathonTemplateFile = "/etc/sds/sparta/marathon-app-template.json"
  val MarathonApp = "marathon"
  val appInfo = InfoHelper.getAppInfo
  val versionParsed = if (appInfo.pomVersion != "${project.version}") appInfo.pomVersion else version
  val DefaultSpartaDockerImage = s"qa.stratio.com/stratio/sparta:$versionParsed"
  val HostMesosNativeLibPath = "/opt/mesosphere/lib"
  val HostMesosNativePackagesPath = "/opt/mesosphere/packages"
  val HostMesosLib = s"$HostMesosNativeLibPath"
  val HostMesosNativeLib = s"$HostMesosNativeLibPath/libmesos.so"
  val ServiceName = workflowModel.fold("") { workflow => WorkflowHelper.getMarathonId(workflow) }
  val DefaultMemory = 1024
  val Krb5ConfFile = "/etc/krb5.conf"
  val ResolvConfigFile = "/etc/resolv.conf"
  val ContainerCertificatePath = "/etc/ssl/certs/java/cacerts"
  val ContainerJavaCertificatePath = "/etc/pki/ca-trust/extracted/java/cacerts"
  val HostCertificatePath = "/etc/pki/ca-trust/extracted/java/cacerts"
  val HostJavaCertificatePath = "/usr/lib/jvm/jre1.8.0_112/lib/security/cacerts"
  val DefaultGracePeriodSeconds = 240
  val DefaultIntervalSeconds = 60
  val DefaultTimeoutSeconds = 30
  val DefaultMaxConsecutiveFailures = 3
  val DefaultForcePullImage = false
  val DefaultPrivileged = false
  val DefaultSparkUIPort = 4040
  val DefaultSOMemSize = 512
  val MinSOMemSize = 256

  lazy val calicoEnabled: Boolean = {
    val calicoEnabled = Properties.envOrNone(CalicoEnableEnv)
    val calicoNetwork = Properties.envOrNone(CalicoNetworkEnv).notBlank
    if (calicoEnabled.isDefined && calicoEnabled.get.equals("true") && calicoNetwork.isDefined) true else false
  }

  lazy val useDynamicAuthentication = Try {
    scala.util.Properties.envOrElse(DynamicAuthEnv, "false").toBoolean
  }.getOrElse(false)

  val portSpark = DefaultSparkUIPort

  /* Lazy variables */

  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig.get
  lazy val upAndDownComponent: UpAndDownComponent = SpartaMarathonComponent.apply
  lazy val upAndDownActor: ActorRef = actorSystem.actorOf(Props(new UpAndDownActor(upAndDownComponent)),
    s"${AkkaConstant.UpDownMarathonActor}-${Calendar.getInstance().getTimeInMillis}-${UUID.randomUUID.toString}")

  /* PUBLIC METHODS */

  def launch(): Unit = {
    assert(workflowModel.isDefined && sparkSubmitRequest.isDefined, "It is mandatory to specify a workflow and a request")
    val createApp = addRequirements(getMarathonAppFromFile, workflowModel.get, sparkSubmitRequest.get)
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
      response <- (upAndDownActor ? DownServiceRequest(ContainerId(containerId), Try(getToken).toOption)).mapTo[UpAndDownMessage]
    } response match {
      case response: DownServiceFails =>
        val information = s"Workflow App ${response.appInfo.id} cannot be killed: ${response.msg}"
        log.error(information)
        throw new Exception(information)
      case response: DownServiceResponse =>
        log.info(s"Workflow App correctly killed with Marathon API and id: ${response.appInfo.id}")
    }
  }

  /* PRIVATE METHODS */

  private def marathonJar: Option[String] =
    Try(marathonConfig.getString("jar")).toOption.orElse(Option(AppConstant.DefaultMarathonDriverURI))

  private def mesosNativeLibrary: Option[String] = Properties.envOrNone(MesosNativeJavaLibraryEnv)

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

  private def addRequirements(app: CreateApp, workflowModel: Workflow, submitRequest: WorkflowExecution): CreateApp = {
    val newCpus = submitRequest.sparkSubmitExecution.sparkConfigurations.get("spark.driver.cores")
      .map(_.toDouble).getOrElse(app.cpus)
    val newMem = submitRequest.sparkSubmitExecution.sparkConfigurations.get("spark.driver.memory")
      .map(transformMemoryToInt).getOrElse(app.mem)
    val envFromSubmit = submitRequest.sparkSubmitExecution.sparkConfigurations.flatMap { case (key, value) =>
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
    val newEnv = Option(envProperties(workflowModel, submitRequest, newMem) ++ envFromSubmit ++ dynamicAuthEnv)
    val javaCertificatesVolume = {
      if (Properties.envOrNone(VaultEnableEnv).isDefined &&
        Properties.envOrNone(VaultHostsEnv).isDefined &&
        Properties.envOrNone(VaultTokenEnv).isDefined)
        Seq.empty[Volume]
      else Seq(
        Volume(ContainerCertificatePath, HostCertificatePath, "RO"),
        Volume(ContainerJavaCertificatePath, HostJavaCertificatePath, "RO")
      )
    }
    val commonVolumes: Seq[Volume] = Seq(
      Volume(ResolvConfigFile, ResolvConfigFile, "RO"),
      Volume(Krb5ConfFile, Krb5ConfFile, "RO")
    )

    val newPortMappings = if (calicoEnabled) Option(Seq(PortMapping(portSpark, portSpark, Option(0),
      protocol = Option("tcp"))))
    else None
    val networkType = if (calicoEnabled) Option("USER") else app.container.docker.network
    val newDockerContainerInfo = Properties.envOrNone(MesosNativeJavaLibraryEnv) match {
      case Some(_) =>
        ContainerInfo(app.container.docker.copy(
          image = spartaDockerImage,
          volumes = Option(javaCertificatesVolume ++ commonVolumes),
          forcePullImage = Option(forcePullImage),
          network = networkType,
          portMappings = newPortMappings,
          privileged = Option(privileged)
        ))
      case None =>
        ContainerInfo(app.container.docker.copy(
          volumes = Option(Seq(
            Volume(HostMesosNativeLibPath, mesosphereLibPath, "RO"),
            Volume(HostMesosNativePackagesPath, mesospherePackagesPath, "RO")) ++ javaCertificatesVolume ++ commonVolumes),
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
    val newIpAddress = if (calicoEnabled) Option(IpAddress(networkName = Properties.envOrNone(CalicoNetworkEnv)))
    else None
    val newPortDefinitions = if (calicoEnabled) None else app.portDefinitions

    app.copy(
      id = ServiceName,
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

  private def envProperties(workflowModel: Workflow,
                            submitRequest: WorkflowExecution,
                            memory: Int): Map[String, JsString] =
    Map(
      AppMainEnv -> Option(AppMainClass),
      AppTypeEnv -> Option(MarathonApp),
      MesosNativeJavaLibraryEnv -> mesosNativeLibrary.orElse(Option(HostMesosNativeLib)),
      LdLibraryEnv -> ldNativeLibrary,
      AppJarEnv -> marathonJar,
      ZookeeperConfigEnv -> submitRequest.sparkSubmitExecution.driverArguments.get("zookeeperConfig"),
      DetailConfigEnv -> submitRequest.sparkSubmitExecution.driverArguments.get("detailConfig"),
      PluginFiles -> Option(submitRequest.sparkSubmitExecution.pluginFiles.mkString(",")),
      WorkflowIdEnv -> submitRequest.sparkSubmitExecution.driverArguments.get("workflowId"),
      VaultEnableEnv -> Properties.envOrNone(VaultEnableEnv),
      VaultHostsEnv -> Properties.envOrNone(VaultHostsEnv),
      VaultPortEnv -> Properties.envOrNone(VaultPortEnv),
      VaultTokenEnv -> getVaultToken,
      DynamicAuthEnv -> Properties.envOrNone(DynamicAuthEnv),
      AppHeapSizeEnv -> Option(s"-Xmx${memory}m"),
      SparkHomeEnv -> Properties.envOrNone(SparkHomeEnv),
      HadoopUserNameEnv -> Properties.envOrNone(HadoopUserNameEnv),
      HdfsConfFromUriEnv -> Properties.envOrNone(HdfsConfFromUriEnv),
      CoreSiteFromUriEnv -> Properties.envOrNone(CoreSiteFromUriEnv),
      HdfsConfFromDfsEnv -> Properties.envOrNone(HdfsConfFromDfsEnv),
      HdfsConfFromDfsNotSecuredEnv -> Properties.envOrNone(HdfsConfFromDfsNotSecuredEnv),
      DefaultFsEnv -> Properties.envOrNone(DefaultFsEnv),
      DefaultHdfsConfUriEnv -> Properties.envOrNone(DefaultHdfsConfUriEnv),
      HadoopConfDirEnv -> Properties.envOrNone(HadoopConfDirEnv),
      SpartaLogAppender -> Properties.envOrNone(SpartaLogAppender),
      BashLogLevelEnv -> Properties.envOrNone(BashLogLevelEnv),
      ServiceLogLevelEnv -> Properties.envOrNone(ServiceLogLevelEnv),
      SpartaLogLevelEnv -> Properties.envOrNone(SpartaLogLevelEnv),
      SparkLogLevelEnv -> Properties.envOrNone(SparkLogLevelEnv),
      HadoopLogLevelEnv -> Properties.envOrNone(HadoopLogLevelEnv),
      ZookeeperLogLevelEnv -> Properties.envOrNone(ZookeeperLogLevelEnv),
      ParquetLogLevelEnv -> Properties.envOrNone(ParquetLogLevelEnv),
      AvroLogLevelEnv -> Properties.envOrNone(AvroLogLevelEnv),
      CrossdataLogLevelEnv -> Properties.envOrNone(CrossdataLogLevelEnv),
      HttpLogLevelEnv -> Properties.envOrNone(HttpLogLevelEnv),
      SpartaRedirectorEnv -> Properties.envOrNone(SpartaRedirectorEnv),
      LoggerStderrSizeEnv -> Properties.envOrSome(LoggerStderrSizeEnv, Option("20MB")),
      LoggerStdoutSizeEnv -> Properties.envOrSome(LoggerStdoutSizeEnv, Option("20MB")),
      LoggerStdoutRotateEnv -> Properties.envOrSome(LoggerStdoutRotateEnv, Option("rotate 10")),
      LoggerStderrRotateEnv -> Properties.envOrSome(LoggerStderrRotateEnv, Option("rotate 10")),
      HdfsRpcProtectionEnv -> Properties.envOrNone(HdfsRpcProtectionEnv),
      HdfsSecurityAuthEnv -> Properties.envOrNone(HdfsSecurityAuthEnv),
      HdfsEncryptDataEnv -> Properties.envOrNone(HdfsEncryptDataEnv),
      HdfsTokenUseIpEnv -> Properties.envOrNone(HdfsTokenUseIpEnv),
      HdfsKerberosPrincipalEnv -> Properties.envOrNone(HdfsKerberosPrincipalEnv),
      HdfsKerberosPrincipalPatternEnv -> Properties.envOrNone(HdfsKerberosPrincipalPatternEnv),
      HdfsEncryptDataTransferEnv -> Properties.envOrNone(HdfsEncryptDataTransferEnv),
      HdfsEncryptDataBitLengthEnv -> Properties.envOrNone(HdfsEncryptDataBitLengthEnv),
      SecurityTlsEnv -> Properties.envOrNone(SecurityTlsEnv),
      SpartaSecretFolderEnv -> Properties.envOrNone(SpartaSecretFolderEnv),
      SecurityTrustoreEnv -> Properties.envOrNone(SecurityTrustoreEnv),
      SecurityKerberosEnv -> Properties.envOrNone(SecurityKerberosEnv),
      SecurityOauth2Env -> Properties.envOrNone(SecurityOauth2Env),
      SecurityMesosEnv -> Properties.envOrNone(SecurityMesosEnv),
      SecurityMarathonEnv -> Properties.envOrNone(SecurityMarathonEnv),
      DcosServiceName -> Properties.envOrNone(DcosServiceName),
      CalicoNetworkEnv -> Properties.envOrNone(CalicoNetworkEnv),
      CalicoEnableEnv -> Properties.envOrNone(CalicoEnableEnv),
      MarathonAppConstraints -> submitRequest.sparkSubmitExecution.sparkConfigurations.get(SubmitMesosConstraintConf),
      SpartaZookeeperPathEnv -> Option(BaseZkPath),
      CrossdataCoreCatalogClass -> Properties.envOrNone(CrossdataCoreCatalogClass),
      CrossdataCoreCatalogPrefix -> Properties.envOrNone(CrossdataCoreCatalogPrefix),
      CrossdataStoragePath -> Properties.envOrNone(CrossdataStoragePath),
      CrossdataCoreStoragePersistence -> Properties.envOrNone(CrossdataCoreStoragePersistence),
      CrossdataSecurityManagerEnabled -> Properties.envOrNone(CrossdataSecurityManagerEnabled),
      CrossdataSecurityManagerClass -> Properties.envOrNone(CrossdataSecurityManagerClass),
      CrossdataCoreCatalogZookeeperConnectionString -> Properties.envOrNone(CrossdataCoreCatalogZookeeperConnectionString),
      CrossdataCoreCatalogZookeeperConnectionTimeout -> Properties.envOrNone(CrossdataCoreCatalogZookeeperConnectionTimeout),
      CrossdataCoreCatalogZookeeperSessionTimeout -> Properties.envOrNone(CrossdataCoreCatalogZookeeperSessionTimeout),
      CrossdataCoreCatalogZookeeperRetryAttempts -> Properties.envOrNone(CrossdataCoreCatalogZookeeperRetryAttempts),
      CrossdataCoreCatalogZookeeperRetryInterval -> Properties.envOrNone(CrossdataCoreCatalogZookeeperRetryInterval)
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> JsString(value))) }.flatten.toMap
}
