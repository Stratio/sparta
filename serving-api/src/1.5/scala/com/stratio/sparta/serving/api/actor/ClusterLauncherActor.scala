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

package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.SpartaClusterJob
import com.stratio.sparta.serving.api.actor.LauncherActor._
import com.stratio.sparta.serving.api.utils.SparkSubmitUtils
import com.stratio.sparta.serving.core.actor.StatusActor
import com.stratio.sparta.serving.core.actor.StatusActor.{FindById, ResponseStatus, Update}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{ClusterListenerUtils, HdfsUtils, SchedulerUtils}
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.spark.launcher.{SparkLauncher, SpartaLauncher}

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ClusterLauncherActor(val statusActor: ActorRef) extends Actor
  with SchedulerUtils with SparkSubmitUtils with ClusterListenerUtils {

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)

  val SpartaDriverClass = SpartaClusterJob.getClass.getCanonicalName.replace("$", "")
  val ZookeeperConfig = SpartaConfig.getZookeeperConfig.getOrElse {
    val message = "Impossible to extract Zookeeper Configuration"
    log.error(message)
    throw new RuntimeException(message)
  }

  override def receive: PartialFunction[Any, Unit] = {
    case Start(policy: PolicyModel) => doInitSpartaContext(policy)
    case ResponseStatus(status) => loggingResponsePolicyStatus(status)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  //scalastyle:off
  def doInitSpartaContext(policy: PolicyModel): Unit = {
    Try {
      val clusterConfig = SpartaConfig.getClusterConfig(policy.executionMode).get
      val driverLocation = Try(policy.driverLocation.getOrElse(DetailConfig.getString(DriverLocation)))
        .getOrElse(DefaultDriverLocation)
      val driverLocationConfig = SpartaConfig.initOptionalConfig(driverLocation, SpartaConfig.mainConfig)

      validateSparkHome(clusterConfig)

      log.info("Init new cluster streamingContext with name " + policy.name)

      val driverPath = driverSubmit(policy, DetailConfig, SpartaConfig.getHdfsConfig)
      val master = clusterConfig.getString(Master).trim
      val pluginsFiles = pluginsJars(policy)
      val driverParams = Seq(policy.id.get.trim,
        zkConfigEncoded,
        detailConfigEncoded,
        pluginsEncoded(pluginsFiles),
        driverLocationConfigEncoded(driverLocation, driverLocationConfig))
      val sparkArguments = submitArgsFromProps(clusterConfig) ++ submitArgsFromPolicy(policy.sparkSubmitArguments)

      log.info(s"Launching Sparta Job with options ... \n\t" +
        s"Policy name: ${policy.name}\n\t" +
        s"Main: $SpartaDriverClass\n\t" +
        s"Driver path: $driverPath\n\t" +
        s"Master: $master\n\t" +
        s"Spark arguments: ${sparkArguments.mkString(",")}\n\t" +
        s"Driver params: $driverParams\n\t" +
        s"Plugins files: ${pluginsFiles.mkString(",")}")

      (launch(policy, SpartaDriverClass, driverPath, master, sparkArguments, driverParams, pluginsFiles,
        clusterConfig), clusterConfig)
    } match {
      case Failure(exception) =>
        val information = s"Error when launching the Sparta cluster job: ${exception.toString}"
        log.error(information, exception)
        statusActor ! Update(PolicyStatusModel(id = policy.id.get, status = Failed, statusInfo = Some(information)))
      case Success((_, clusterConfig)) =>
        addClusterContextListener(policy.id.get, policy.name, clusterConfig)
        log.info("Sparta Cluster Job launched correctly")
    }
  }


  def launch(policy: PolicyModel,
             main: String,
             driverFile: String,
             master: String,
             sparkArguments: Map[String, String],
             driverArguments: Seq[String],
             pluginsFiles: Seq[String],
             clusterConfig: Config): Unit = {
    val sparkLauncher = new SparkLauncher()
      .setSparkHome(sparkHome(clusterConfig))
      .setAppResource(driverFile)
      .setMainClass(main)
      .setMaster(master)

    // Plugins files
    pluginsFiles.foreach(file => sparkLauncher.addJar(file))

    //Spark arguments
    sparkArguments.filter(_._2.nonEmpty).foreach { case (k: String, v: String) => sparkLauncher.addSparkArg(k, v) }
    sparkArguments.filter(_._2.isEmpty).foreach { case (k: String, v: String) => sparkLauncher.addSparkArg(k) }
    if (!sparkArguments.contains(SubmitSupervise) && isSupervised(policy, clusterConfig))
      sparkLauncher.addSparkArg(SubmitSupervise)

    // Kerberos Options
    val principalNameOption = HdfsUtils.getPrincipalName
    val keyTabPathOption = HdfsUtils.getKeyTabPath
    (principalNameOption, keyTabPathOption) match {
      case (Some(principalName), Some(keyTabPath)) =>
        log.info(s"Launching Spark Submit with Kerberos security, adding principal and keyTab arguments... \n\t" +
          s"Principal: $principalName \n\tKeyTab: $keyTabPath")
        sparkLauncher.addSparkArg(SubmitPrincipal, principalName)
        sparkLauncher.addSparkArg(SubmitKeyTab, keyTabPath)
      case _ =>
        log.info("Launching Spark Submit without Kerberos security")
    }

    // Spark properties
    log.info("Adding Spark options to Sparta Job ... ")
    sparkConf(clusterConfig).foreach { case (key: String, value: String) =>
      val valueParsed = if (key == "spark.app.name") s"$value-${policy.name}" else value
      log.info(s"\t$key = $valueParsed")
      sparkLauncher.setConf(key.trim, valueParsed.trim)
    }
    sparkLauncher.setConf(SparkGracefullyStopProperty, gracefulStop(policy).toString)

    // Driver (Sparta) params
    driverArguments.foreach(sparkLauncher.addAppArgs(_))

    // Starting Spark Submit Process with Spark Launcher
    log.info("Executing SparkLauncher...")

    //Scheduling task to check policy status
    scheduleOneTask(
      AppConstant.AwaitPolicyChangeStatus,
      AppConstant.DefaultAwaitPolicyChangeStatus
    )(checkPolicyStatus(policy))

    //Launch Job and manage future response
    val sparkProcessStatus: Future[(Boolean, Process)] = for {
      sparkProcess <- Future(sparkLauncher.asInstanceOf[SpartaLauncher].launch)
    } yield (Await.result(Future(sparkProcess.waitFor() == 0), 20 seconds), sparkProcess)
    sparkProcessStatus.onComplete {
      case Success((exitCode, sparkProcess)) =>
        log.info("Command: {}", sparkLauncher.asInstanceOf[SpartaLauncher].getSubmit)
        if (!sparkLauncherStreams(exitCode, sparkProcess)) throw new Exception("Errors in Spark Launcher")
      case Failure(exception) =>
        throw new Exception(exception.getMessage)
    }
  }

  //scalastyle:on

  def sparkLauncherStreams(exitCode: Boolean, sparkProcess: Process): Boolean = {
    @tailrec
    def recursiveErrors(it: Iterator[String], count: Int): Unit = {
      log.info(it.next())
      if (it.hasNext && count < 50)
        recursiveErrors(it, count + 1)
    }

    if (exitCode) log.info("Spark process exited successfully")
    else log.info("Spark process exited with errors")

    Source.fromInputStream(sparkProcess.getInputStream).close()
    sparkProcess.getOutputStream.close()

    log.info("ErrorStream:")

    val error = Source.fromInputStream(sparkProcess.getErrorStream)
    recursiveErrors(error.getLines(), 0)
    error.close()

    exitCode
  }


  /** Arguments functions **/

  def render(config: Config, key: String): String = config.atKey(key).root.render(ConfigRenderOptions.concise)

  def encode(value: String): String = BaseEncoding.base64().encode(value.getBytes)

  def zkConfigEncoded: String = encode(render(ZookeeperConfig, "zookeeper"))

  def detailConfigEncoded: String = encode(render(DetailConfig, "config"))

  def pluginsEncoded(plugins: Seq[String]): String = encode((Seq(" ") ++ plugins).mkString(","))

  def driverLocationConfigEncoded(executionMode: String, clusterConfig: Option[Config]): String =
    clusterConfig match {
      case Some(config) => encode(render(config, executionMode))
      case None => encode(" ")
    }

  def checkPolicyStatus(policy: PolicyModel): Unit = {
    for {
      statusResponse <- (statusActor ? FindById(policy.id.get)).mapTo[ResponseStatus]
    } yield statusResponse match {
      case StatusActor.ResponseStatus(Success(policyStatus)) =>
        if (policyStatus.status == Launched || policyStatus.status == Starting || policyStatus.status == Stopping) {
          val information = s"The policy-checker detects that the policy was not started/stopped correctly"
          log.error(information)
          statusActor ! Update(PolicyStatusModel(id = policy.id.get, status = Failed, statusInfo = Some(information)))
        } else {
          val information = s"The policy-checker detects that the policy was started/stopped correctly"
          log.info(information)
          statusActor ! Update(PolicyStatusModel(
            id = policy.id.get, status = NotDefined, statusInfo = Some(information)))
        }
      case StatusActor.ResponseStatus(Failure(exception)) =>
        log.error(s"Error when extract policy status in scheduler task.", exception)
    }
  }

  def loggingResponsePolicyStatus(response: Try[PolicyStatusModel]): Unit =
    response match {
      case Success(statusModel) =>
        log.info(s"Policy status model created or updated correctly: " +
          s"\n\tId: ${statusModel.id}\n\tStatus: ${statusModel.status}")
      case Failure(e) =>
        log.error(s"Policy status model creation failure. Error: ${e.getLocalizedMessage}", e)
    }
}