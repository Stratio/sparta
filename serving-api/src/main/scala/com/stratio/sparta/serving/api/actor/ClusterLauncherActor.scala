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
import com.stratio.sparta.driver.utils.ClusterSparkFilesUtils
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparta.serving.api.utils.SparkSubmitUtils
import com.stratio.sparta.serving.core.actor.PolicyStatusActor
import com.stratio.sparta.serving.core.actor.PolicyStatusActor.{FindById, ResponseStatus, Update}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{HdfsUtils, SchedulerUtils}
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.spark.launcher.SpartaLauncher

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ClusterLauncherActor(policyStatusActor: ActorRef) extends Actor
  with SpartaSerializer with SchedulerUtils with SparkSubmitUtils {

  override val ClusterConfig = SpartaConfig.getClusterConfig.get

  val DetailConfig = SpartaConfig.getDetailConfig.get
  val SpartaDriver = SpartaClusterJob.getClass.getCanonicalName
  val ZookeeperConfig = SpartaConfig.getZookeeperConfig.get
  val HdfsConfig = SpartaConfig.getHdfsConfig.get
  val Hdfs = HdfsUtils()

  implicit val timeout: Timeout = Timeout(3.seconds)

  override def receive: PartialFunction[Any, Unit] = {
    case Start(policy: PolicyModel) => doInitSpartaContext(policy)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  def doInitSpartaContext(policy: PolicyModel): Unit = {
    Try {
      validateSparkHome()

      val Uploader = ClusterSparkFilesUtils(policy, Hdfs)
      val PolicyId = policy.id.get.trim
      val Master = ClusterConfig.getString(AppConstant.Master)
      val BasePath = s"/user/${Hdfs.userName}/${AppConstant.ConfigAppName}/$PolicyId"
      val PluginsJarsPath = s"$BasePath/${HdfsConfig.getString(AppConstant.PluginsFolder)}/"
      val DriverJarPath = s"$BasePath/${HdfsConfig.getString(AppConstant.ExecutionJarFolder)}/"

      log.info("Init new cluster streamingContext with name " + policy.name)

      val driverPath = Uploader.getDriverFile(DriverJarPath)
      val pluginsFiles = Uploader.getPluginsFiles(PluginsJarsPath)
      val driverParams =
        Seq(PolicyId, zkConfigEncoded, detailConfigEncoded, pluginsEncoded(pluginsFiles), hdfsConfigEncoded)
      val sparkArguments = submitArgumentsFromProperties ++ submitArgumentsFromPolicy(policy.sparkSubmitArguments)

      log.info(s"Launching Sparta Job with options ... \n\tPolicy name: ${policy.name}\n\tMain: $SpartaDriver\n\t" +
        s"Driver path: $driverPath\n\tMaster: $Master\n\tSpark arguments: ${sparkArguments.mkString(",")}\n\t" +
        s"Driver params: $driverParams\n\tPlugins files: ${pluginsFiles.mkString(",")}")
      launch(policy, SpartaDriver, driverPath, Master, sparkArguments, driverParams, pluginsFiles)
    } match {
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        setPolicyStatus(policy.id.get, PolicyStatusEnum.Failed)
      case Success(_) =>
        log.info("Sparta Cluster Job launched correctly")
    }
  }

  //scalastyle:off
  def launch(policy: PolicyModel,
                     main: String,
                     driverFile: String,
                     master: String,
                     sparkArguments: Map[String, String],
                     driverArguments: Seq[String],
                     pluginsFiles: Seq[String]): Unit = {
    val sparkLauncher = new SpartaLauncher()
      .setSparkHome(sparkHome)
      .setAppResource(driverFile)
      .setMainClass(main)
      .setMaster(master)
    
    // Plugins files
    pluginsFiles.foreach(file => sparkLauncher.addJar(file))

    //Spark arguments
    sparkArguments.map { case (k: String, v: String) => sparkLauncher.addSparkArg(k, v) }
    if (isSupervised(policy, DetailConfig)) sparkLauncher.addSparkArg(SubmitSupervise)

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
    sparkConf.foreach { case (key: String, value: String) =>
      val valueParsed = if (key == "spark.app.name") s"$value-${policy.name}" else value
      log.info(s"\t$key = $valueParsed")
      sparkLauncher.setConf(key, valueParsed)
    }

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
        if (!sparkLauncherStreams(exitCode, sparkProcess)) throw new Exception("TimeOut in Spark Launcher")
      case Failure(exception) =>
        log.error(exception.getMessage)
        throw exception
    }
  }

  //scalastyle:on

  def sparkLauncherStreams(exitCode: Boolean, sparkProcess: Process): Boolean = {

    def recursiveErrors(it: Iterator[String], count: Int): Unit = {
      log.info(it.next())
      if (it.hasNext && count < 50)
        recursiveErrors(it, count + 1)
    }

    if (exitCode) log.info("Spark process exited successfully")
    else log.info("Spark process exited with timeout")

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

  def hdfsConfigEncoded: String = encode(render(HdfsConfig, "hdfs"))

  /** Policy Status Functions **/

  def setPolicyStatus(policyId: String, status: PolicyStatusEnum.Value): Unit =
    policyStatusActor ! Update(PolicyStatusModel(policyId, status))

  def checkPolicyStatus(policy: PolicyModel): Unit = {
    for {
      statusResponse <- (policyStatusActor ? FindById(policy.id.get)).mapTo[ResponseStatus]
    } yield statusResponse match {
      case PolicyStatusActor.ResponseStatus(Success(policyStatus)) =>
        if (policyStatus.status == PolicyStatusEnum.Launched || policyStatus.status == PolicyStatusEnum.Starting) {
          log.info(s"The policy ${policy.name} was not started correctly, setting the failed status...")
          setPolicyStatus(policy.id.get, PolicyStatusEnum.Failed)
        } else log.info(s"The policy ${policy.name} was started correctly")
      case PolicyStatusActor.ResponseStatus(Failure(exception)) =>
        log.error(s"Error when extract policy status in scheduler task.", exception)
    }
  }
}