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

import java.util.Calendar

import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant._
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.helpers.{InfoHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowErrorModel, WorkflowModel, WorkflowStatusModel}
import com.stratio.sparta.serving.core.utils.PolicyStatusUtils
import com.stratio.tikitakka.common.message._
import com.stratio.tikitakka.common.model._
import com.stratio.tikitakka.core.UpAndDownActor
import com.stratio.tikitakka.updown.UpAndDownComponent
import com.typesafe.config.Config
import org.apache.curator.framework.CuratorFramework
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Properties, Try}

//scalastyle:off
class MarathonService(context: ActorContext,
                      val curatorFramework: CuratorFramework,
                      workflowModel: Option[WorkflowModel],
                      sparkSubmitRequest: Option[SubmitRequest]) extends OauthTokenUtils with PolicyStatusUtils {

  def this(context: ActorContext,
           curatorFramework: CuratorFramework,
           policyModel: WorkflowModel,
           sparkSubmitRequest: SubmitRequest) =
    this(context, curatorFramework, Option(policyModel), Option(sparkSubmitRequest))

  def this(context: ActorContext, curatorFramework: CuratorFramework) = this(context, curatorFramework, None, None)

  /* Implicit variables */

  implicit val actorSystem: ActorSystem = context.system
  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)
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
  val ServiceName = workflowModel.fold("") { workflow =>
    WorkflowHelper.getMarathonId(workflow)
  }
  val DefaultMemory = 1024
  val Krb5ConfFile = "/etc/krb5.conf:/etc/krb5.conf:ro"
  val ContainerCertificatePath = "/etc/ssl/certs/java/cacerts"
  val HostCertificatePath = "/etc/pki/ca-trust/extracted/java/cacerts"
  val DefaultGracePeriodSeconds = 240
  val DefaultIntervalSeconds = 60
  val DefaultTimeoutSeconds = 20
  val DefaultMaxConsecutiveFailures = 3
  val DefaultForcePullImage = false
  val DefaultPrivileged = false
  val DefaultSparkUIPort = 4040

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
    s"${AkkaConstant.UpDownMarathonActor}-${Calendar.getInstance().getTimeInMillis}")

  /* PUBLIC METHODS */

  def launch(): Unit = {
    assert(workflowModel.isDefined && sparkSubmitRequest.isDefined, "It is mandatory to specify a policy and a request")
    val createApp = addRequirements(getMarathonAppFromFile, workflowModel.get, sparkSubmitRequest.get)
    for {
      response <- (upAndDownActor ? UpServiceRequest(createApp, Try(getToken).toOption)).mapTo[UpAndDownMessage]
    } response match {
      case response: UpServiceFails =>
        val information = s"An error was encountered while launching the Workflow App in the Marathon API with id: ${response.appInfo.id}"
        log.error(information)
        updateStatus(WorkflowStatusModel(
          id = workflowModel.get.id.get,
          status = Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowErrorModel(information, PhaseEnum.Execution, response.msg))))
        log.error(s"Service ${response.appInfo.id} cannot be deployed: ${response.msg}")
      case response: UpServiceResponse =>
        val information = s"Workflow App correctly launched to Marathon API with id: ${response.appInfo.id}"
        log.info(information)
        updateStatus(WorkflowStatusModel(id = workflowModel.get.id.get, status = Uploaded,
          statusInfo = Option(information)))
      case _ =>
        val information = "Unrecognized message received from Marathon API"
        log.warn(information)
        updateStatus(WorkflowStatusModel(id = workflowModel.get.id.get, status = NotDefined,
          statusInfo = Option(information)))
    }
  }

  def kill(containerId: String): Unit = upAndDownActor ! DownServiceRequest(ContainerId(containerId), Try(getToken).toOption)

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

  private def getKrb5ConfVolume: Seq[Parameter] = Properties.envOrNone(VaultEnableEnv) match {
    case Some(vaultEnable) if Try(vaultEnable.toBoolean).getOrElse(false) =>
      Seq(Parameter("volume", Krb5ConfFile))
    case None =>
      Seq.empty[Parameter]
  }

  private def transformMemoryToInt(memory: String): Int = Try(memory match {
    case mem if mem.contains("G") => mem.replace("G", "").toInt * 1024
    case mem if mem.contains("g") => mem.replace("g", "").toInt * 1024
    case mem if mem.contains("m") => mem.replace("m", "").toInt
    case mem if mem.contains("M") => mem.replace("M", "").toInt
    case _ => memory.toInt
  }).getOrElse(DefaultMemory)

  private def addRequirements(app: CreateApp, policyModel: WorkflowModel, submitRequest: SubmitRequest): CreateApp = {
    val newCpus = submitRequest.sparkConfigurations.get("spark.driver.cores").map(_.toDouble).getOrElse(app.cpus)
    val newMem = submitRequest.sparkConfigurations.get("spark.driver.memory").map(transformMemoryToInt)
      .getOrElse(app.mem)
    val envFromSubmit = submitRequest.sparkConfigurations.flatMap { case (key, value) =>
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
    val newEnv = Option(envProperties(policyModel, submitRequest, newMem) ++ envFromSubmit ++ dynamicAuthEnv)
    val javaCertificatesVolume = {
      if (Properties.envOrNone(VaultEnableEnv).isDefined &&
        Properties.envOrNone(VaultHostsEnv).isDefined &&
        Properties.envOrNone(VaultTokenEnv).isDefined)
        Seq.empty[Volume]
      else Seq(Volume(ContainerCertificatePath, HostCertificatePath, "RO"))
    }

    val newPortMappings = if (calicoEnabled) Option(Seq(PortMapping(portSpark, portSpark, Option(0),
      protocol = Option("tcp")))) else None
    val networkType = if (calicoEnabled) Option("USER") else app.container.docker.network
    val newDockerContainerInfo = Properties.envOrNone(MesosNativeJavaLibraryEnv) match {
      case Some(_) =>
        ContainerInfo(app.container.docker.copy(
          image = spartaDockerImage,
          volumes = Option(javaCertificatesVolume),
          parameters = Option(getKrb5ConfVolume),
          forcePullImage = Option(forcePullImage),
          network = networkType,
          portMappings = newPortMappings,
          privileged = Option(privileged)
        ))
      case None =>
        ContainerInfo(app.container.docker.copy(
          volumes = Option(Seq(
            Volume(HostMesosNativeLibPath, mesosphereLibPath, "RO"),
            Volume(HostMesosNativePackagesPath, mesospherePackagesPath, "RO")) ++ javaCertificatesVolume),
          image = spartaDockerImage,
          parameters = Option(getKrb5ConfVolume),
          forcePullImage = Option(forcePullImage),
          network = networkType,
          portMappings = newPortMappings,
          privileged = Option(privileged)
        ))
    }
    val newHealthChecks = Option(Seq(HealthCheck(
      protocol = "TCP",
      portIndex = Option(0),
      gracePeriodSeconds = gracePeriodSeconds,
      intervalSeconds = intervalSeconds,
      timeoutSeconds = timeoutSeconds,
      maxConsecutiveFailures = maxConsecutiveFailures
    )))

    val newConstraints = Properties.envOrNone(Constraints).map(constraint =>
      Seq(Seq("label", "CLUSTER", constraint)))

    val newIpAddress = if (calicoEnabled) Option(IpAddress(networkName = Properties.envOrNone(CalicoNetworkEnv)))
    else None

    val newPortDefinitions = if (calicoEnabled) None else app.portDefinitions

    app.copy(
      id = ServiceName,
      cpus = newCpus,
      mem = newMem,
      env = newEnv,
      container = newDockerContainerInfo,
      healthChecks = newHealthChecks,
      secrets = newSecrets,
      portDefinitions = newPortDefinitions,
      ipAddress = newIpAddress,
      constraints = newConstraints
    )
  }

  private def envProperties(workflowModel: WorkflowModel,
                            submitRequest: SubmitRequest,
                            memory: Int): Map[String, JsString] =
    Map(
      AppMainEnv -> Option(AppMainClass),
      AppTypeEnv -> Option(MarathonApp),
      MesosNativeJavaLibraryEnv -> mesosNativeLibrary.orElse(Option(HostMesosNativeLib)),
      LdLibraryEnv -> ldNativeLibrary,
      AppJarEnv -> marathonJar,
      ZookeeperConfigEnv -> submitRequest.driverArguments.get("zookeeperConfig"),
      DetailConfigEnv -> submitRequest.driverArguments.get("detailConfig"),
      WorkflowIdEnv -> workflowModel.id,
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
      ServiceLogLevelEnv -> Properties.envOrNone(ServiceLogLevelEnv),
      SpartaLogLevelEnv -> Properties.envOrNone(SpartaLogLevelEnv),
      SparkLogLevelEnv -> Properties.envOrNone(SparkLogLevelEnv),
      HadoopLogLevelEnv -> Properties.envOrNone(HadoopLogLevelEnv),
      ZookeeperLogLevelEnv -> Properties.envOrNone(ZookeeperLogLevelEnv),
      ParquetLogLevelEnv -> Properties.envOrNone(ParquetLogLevelEnv),
      AvroLogLevelEnv -> Properties.envOrNone(AvroLogLevelEnv),
      NettyLogLevelEnv -> Properties.envOrNone(NettyLogLevelEnv),
      HdfsRpcProtectionEnv -> Properties.envOrNone(HdfsRpcProtectionEnv),
      HdfsSecurityAuthEnv -> Properties.envOrNone(HdfsSecurityAuthEnv),
      HdfsEncryptDataEnv -> Properties.envOrNone(HdfsEncryptDataEnv),
      HdfsTokenUseIpEnv -> Properties.envOrNone(HdfsTokenUseIpEnv),
      HdfsKerberosPrincipalEnv -> Properties.envOrNone(HdfsKerberosPrincipalEnv),
      HdfsKerberosPrincipalPatternEnv -> Properties.envOrNone(HdfsKerberosPrincipalPatternEnv),
      HdfsEncryptDataTransferEnv -> Properties.envOrNone(HdfsEncryptDataTransferEnv),
      HdfsEncryptDataBitLengthEnv -> Properties.envOrNone(HdfsEncryptDataBitLengthEnv),
      SecurityTlsEnv -> Properties.envOrNone(SecurityTlsEnv),
      SecurityTrustoreEnv -> Properties.envOrNone(SecurityTrustoreEnv),
      SecurityKerberosEnv -> Properties.envOrNone(SecurityKerberosEnv),
      SecurityOauth2Env -> Properties.envOrNone(SecurityOauth2Env),
      SecurityMesosEnv -> Properties.envOrNone(SecurityMesosEnv),
      SecurityMarathonEnv -> Properties.envOrNone(SecurityMarathonEnv),
      DcosServiceName -> Properties.envOrNone(DcosServiceName),
      CalicoNetworkEnv -> Properties.envOrNone(CalicoNetworkEnv),
      CalicoEnableEnv -> Properties.envOrNone(CalicoEnableEnv),
      SpartaZookeeperPathEnv -> Option(BaseZKPath),
      CrossdataCoreCatalogClass -> Properties.envOrNone(CrossdataCoreCatalogClass),
      CrossdataCoreCatalogPrefix -> Properties.envOrNone(CrossdataCoreCatalogPrefix),
      CrossdataCoreCatalogZookeeperConnectionString -> Properties.envOrNone(CrossdataCoreCatalogZookeeperConnectionString),
      CrossdataCoreCatalogZookeeperConnectionTimeout -> Properties.envOrNone(CrossdataCoreCatalogZookeeperConnectionTimeout),
      CrossdataCoreCatalogZookeeperSessionTimeout -> Properties.envOrNone(CrossdataCoreCatalogZookeeperSessionTimeout),
      CrossdataCoreCatalogZookeeperRetryAttempts -> Properties.envOrNone(CrossdataCoreCatalogZookeeperRetryAttempts),
      CrossdataCoreCatalogZookeeperRetryInterval -> Properties.envOrNone(CrossdataCoreCatalogZookeeperRetryInterval)
    ).flatMap { case (k, v) => v.notBlank.map(value => Option(k -> JsString(value))) }.flatten.toMap
}
