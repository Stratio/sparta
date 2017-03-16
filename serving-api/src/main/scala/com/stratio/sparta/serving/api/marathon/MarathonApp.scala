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

package com.stratio.sparta.serving.api.marathon

import akka.actor.{ActorContext, ActorRef}
import akka.pattern.ask
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import com.stratio.sparta.serving.core.actor.StatusActor.Update
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyErrorModel, PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.stratio.tikitakka.common.message.{UpAndDownMessage, UpServiceFails, UpServiceRequest, UpServiceResponse}
import com.stratio.tikitakka.common.model.{ContainerInfo, CreateApp, Volume}
import com.stratio.tikitakka.core.UpAndDownActor
import com.stratio.tikitakka.updown.UpAndDownComponent
import org.joda.time.DateTime
import play.api.libs.json._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Properties, Try}

class MarathonApp(context: ActorContext, policyModel: PolicyModel, sparkSubmitRequest: SubmitRequest)
  extends OauthTokenUtils {

  /* Implicit variables */

  implicit val actorSystem = context.system
  implicit val timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

  /* Constant variables */

  val AppMainClass = "com.stratio.sparta.driver.SpartaMarathonApp"
  val DefaultMarathonTemplateFile = "/etc/sds/sparta/marathon-app-template.json"
  val MarathonApp = "marathon"
  val MesosNativeLibPath = "/opt/mesosphere/lib"
  val MesosNativePackagesPath = "/opt/mesosphere/packages"
  val MesosNativeLib = s"$MesosNativeLibPath/libmesos.so"
  val DefaultCpus = 1d
  val DefaultMem = 1024

  /* Environment variables to Marathon Application */

  val AppTypeEnv = "SPARTA_APP_TYPE"
  val MesosNativeJavaLibraryEnv = "MESOS_NATIVE_JAVA_LIBRARY"
  val AppMainEnv = "SPARTA_MARATHON_MAIN_CLASS"
  val AppJarEnv = "SPARTA_MARATHON_JAR"
  val VaultHostEnv = "VAULT_HOST"
  val VaultPortEnv = "VAULT_PORT"
  val VaultTokenEnv = "VAULT_TOKEN"
  val CpusEnv = "MARATHON_CPUS"
  val MemEnv = "MARATHON_MEM"
  val PolicyIdEnv = "SPARTA_POLICY_ID"
  val ZookeeperConfigEnv = "SPARTA_ZOOKEEPER_CONFIG"
  val DetailConfigEnv = "SPARTA_DETAIL_CONFIG"
  val AppHeapSizeEnv = "MARATHON_APP_HEAP_SIZE"
  val AppHeapMinimunSizeEnv = "MARATHON_APP_HEAP_MINIMUM_SIZE"
  val SparkHomeEnv = "SPARK_HOME"
  val HadoopUserNameEnv = "HADOOP_USER_NAME"
  val CoreSiteFromUriEnv = "CORE_SITE_FROM_URI"
  val CoreSiteFromDfsEnv = "CORE_SITE_FROM_DFS"
  val DefaultFsEnv = "DEFAULT_FS"
  val HadoopConfDirEnv = "HADOOP_CONF_DIR"
  val ServiceLogLevelEnv = "SERVICE_LOG_LEVEL"
  val SpartaLogLevelEnv = "SPARTA_LOG_LEVEL"
  val SparkLogLevelEnv = "SPARK_LOG_LEVEL"
  val ZookeeperLogLevelEnv = "ZOOKEEPER_LOG_LEVEL"
  val HadoopLogLevelEnv = "HADOOP_LOG_LEVEL"
  val DcosServiceName = "DCOS_SERVICE_NAME"

  /* Lazy variables */

  lazy val marathonConfig = SpartaConfig.getClusterConfig(Option(ConfigMarathon)).get
  lazy val upAndDownComponent: UpAndDownComponent = SpartaMarathonComponent.apply
  lazy val upAndDownActor = actorSystem.actorOf(UpAndDownActor.props(upAndDownComponent))
  lazy val substitutionProperties = Map(
    AppTypeEnv -> Option(MarathonApp),
    MesosNativeJavaLibraryEnv -> Option(MesosNativeLib),
    AppJarEnv -> marathonJar,
    ZookeeperConfigEnv -> sparkSubmitRequest.driverArguments.get("zookeeperConfig"),
    DetailConfigEnv -> sparkSubmitRequest.driverArguments.get("detailConfig"),
    PolicyIdEnv -> policyModel.id,
    VaultHostEnv -> envVaultHost,
    VaultPortEnv -> envVaulPort,
    VaultTokenEnv -> envVaultToken,
    AppHeapSizeEnv -> envMarathonDriverMem.map(memory => s"-Xmx${memory}m"),
    AppHeapMinimunSizeEnv -> envMarathonDriverMem.map(memory => s"-Xms${memory.toInt / 2}m"),
    SparkHomeEnv -> envSparkHome,
    HadoopUserNameEnv -> envHadoopUserName,
    CoreSiteFromUriEnv -> envCoreSiteFromUri,
    CoreSiteFromDfsEnv -> envCoreSiteFromDfs,
    DefaultFsEnv -> envDefaultFs,
    HadoopConfDirEnv -> envHadoopConfDir,
    ServiceLogLevelEnv -> envServiceLogLevel,
    SpartaLogLevelEnv -> envSpartaLogLevel,
    SparkLogLevelEnv -> envSparkLogLevel,
    HadoopLogLevelEnv -> envHadoopLogLevel,
    DcosServiceName -> Option(s"sparta-driver-${policyModel.name}")
  ).flatMap { case (k, v) => v.map(value => Option(k -> value)) }.flatten.toMap

  /* PUBLIC METHODS */

  def launch(statusActor: ActorRef, detailExecMode: String): Unit =
    for {
     // response <- (upAndDownActor ? UpServiceRequest(addRequirements(getMarathonAppFromFile), Option(getToken)))
      response <- (upAndDownActor ? UpServiceRequest(addRequirements(getMarathonAppFromFile), Try(getToken).toOption))
        .mapTo[UpAndDownMessage]
    } response match {
      case response: UpServiceFails =>
        val information = s"Error when launching Sparta Marathon App to Marathon API with id: ${response.appInfo.id}"
        log.error(information)
        statusActor ! Update(PolicyStatusModel(
          id = policyModel.id.get, status = Failed, statusInfo = Option(information),
          lastError = Option(PolicyErrorModel(information, PhaseEnum.Execution, response.msg))))
        log.error(s"Service ${response.appInfo.id} can't be deployed: ${response.msg}")
      case response: UpServiceResponse =>
        val information = s"Sparta Marathon App launched correctly to Marathon API with id: ${response.appInfo.id}"
        log.info(information)
        statusActor ! Update(PolicyStatusModel(id = policyModel.id.get, status = NotStarted,
          statusInfo = Option(information), lastExecutionMode = Option(detailExecMode)))
      case _ =>
        val information = "Unrecognized message received from Marathon API"
        log.warn(information)
        statusActor ! Update(PolicyStatusModel(id = policyModel.id.get,
          status = NotDefined, statusInfo = Option(information)))
    }

  /* PRIVATE METHODS */

  private def marathonJar: Option[String] =
    Try(marathonConfig.getString("jar")).toOption.orElse(Option(AppConstant.DefaultMarathonDriverURI))

  private def mesosphereLibPath: String =
    Try(marathonConfig.getString("mesosphere.lib")).toOption.getOrElse(MesosNativeLibPath)

  private def mesospherePackagesPath: String =
    Try(marathonConfig.getString("mesosphere.packages")).toOption.getOrElse(MesosNativePackagesPath)

  private def envSparkHome: Option[String] = Properties.envOrNone(SparkHomeEnv)

  private def envVaultHost: Option[String] = Properties.envOrNone(VaultHostEnv)

  private def envVaulPort: Option[String] = Properties.envOrNone(VaultPortEnv)

  private def envVaultToken: Option[String] = Properties.envOrNone(VaultTokenEnv)

  private def envMarathonDriverCpus: Option[String] = Properties.envOrNone(CpusEnv)

  private def envMarathonDriverMem: Option[String] = Properties.envOrNone(MemEnv)

  private def envHadoopUserName: Option[String] = Properties.envOrNone(HadoopUserNameEnv)

  private def envCoreSiteFromUri: Option[String] = Properties.envOrNone(CoreSiteFromUriEnv)

  private def envCoreSiteFromDfs: Option[String] = Properties.envOrNone(CoreSiteFromDfsEnv)

  private def envDefaultFs: Option[String] = Properties.envOrNone(DefaultFsEnv)

  private def envHadoopConfDir: Option[String] = Properties.envOrNone(HadoopConfDirEnv)

  private def envServiceLogLevel: Option[String] = Properties.envOrNone(ServiceLogLevelEnv)

  private def envSpartaLogLevel: Option[String] = Properties.envOrNone(SpartaLogLevelEnv)

  private def envSparkLogLevel: Option[String] = Properties.envOrNone(SparkLogLevelEnv)

  private def envHadoopLogLevel: Option[String] = Properties.envOrNone(HadoopLogLevelEnv)

  private def getMarathonAppFromFile: CreateApp = {
    val templateFile = Try(marathonConfig.getString("template.file")).toOption.getOrElse(DefaultMarathonTemplateFile)
    val fileContent = Source.fromFile(templateFile).mkString
    Json.parse(fileContent).as[CreateApp]
  }

  private def addRequirements(app: CreateApp): CreateApp = {
    val newEnv = app.env.map { properties =>
      properties.flatMap { case (k, v) =>
        if (v == "???")
          substitutionProperties.get(k).map(vParsed => (k, vParsed))
        else Some((k, v))
      }
    }
    val newLabels = app.labels.flatMap { case (k, v) =>
      if (v == "???")
        substitutionProperties.get(k).map(vParsed => (k, vParsed))
      else Some((k, v))
    }
    val newDockerContainerInfo = app.container.docker.copy(volumes = Some(Seq(
      Volume(mesosphereLibPath, MesosNativeLibPath, "ro"),
      Volume(mesospherePackagesPath, MesosNativePackagesPath, "ro")
    )))

    app.copy(
      id = s"sparta-${policyModel.name}-${DateTime.now().getMillis}",
      cpus = envMarathonDriverCpus.map(_.toDouble).getOrElse(DefaultCpus),
      mem = envMarathonDriverMem.map(_.toInt).getOrElse(DefaultMem),
      env = newEnv,
      labels = newLabels,
      container = ContainerInfo(newDockerContainerInfo)
    )
  }
}
