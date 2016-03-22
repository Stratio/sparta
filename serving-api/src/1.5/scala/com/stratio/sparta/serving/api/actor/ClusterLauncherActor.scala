/**
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
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.util.{ClusterSparkFiles, HdfsUtils}
import com.stratio.sparta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparta.serving.core.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SpartaSerializer}
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparta.serving.core.policy.status.PolicyStatusEnum
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.spark.launcher.SparkLauncher

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Properties, Success, Try}

class ClusterLauncherActor(policy: AggregationPoliciesModel, policyStatusActor: ActorRef) extends Actor
with SLF4JLogging
with SpartaSerializer {

  private val SpartaDriver = "com.stratio.sparta.driver.SpartaClusterJob"
  private val StandaloneSupervise = "--supervise"

  private val ClusterConfig = SpartaConfig.getClusterConfig.get
  private val ZookeeperConfig = SpartaConfig.getZookeeperConfig.get
  private val HdfsConfig = SpartaConfig.getHdfsConfig.get
  private val DetailConfig = SpartaConfig.getDetailConfig.get

  private val Hdfs = HdfsUtils(Option(HdfsConfig))
  private val Uploader = ClusterSparkFiles(policy, Hdfs)
  private val PolicyId = policy.id.get.trim
  private val Master = ClusterConfig.getString(AppConstant.Master)
  private val BasePath = s"/user/${Hdfs.userName}/${AppConstant.ConfigAppName}/$PolicyId"
  private val PluginsJarsPath = s"$BasePath/${HdfsConfig.getString(AppConstant.PluginsFolder)}/"
  private val DriverJarPath = s"$BasePath/${HdfsConfig.getString(AppConstant.ExecutionJarFolder)}/"

  implicit val timeout: Timeout = Timeout(3.seconds)

  override def receive: PartialFunction[Any, Unit] = {
    case Start => doInitSpartaContext()
  }

  def doInitSpartaContext(): Unit = {
    Try {
      log.info("Init new cluster streamingContext with name " + policy.name)
      validateSparkHome()
      val driverPath = Uploader.getDriverFile(DriverJarPath)
      val pluginsFiles = Uploader.getPluginsFiles(PluginsJarsPath)
      val driverParams = Seq(PolicyId, zkConfigEncoded, detailConfigEncoded, pluginsEncoded(pluginsFiles))

      launch(SpartaDriver, driverPath, Master, sparkArgs, driverParams, pluginsFiles)
    } match {
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        setErrorStatus()
      case Success(_) =>
      //TODO add more statuses for the policies
    }
  }

  private def setErrorStatus(): Unit =
    policyStatusActor ? Update(PolicyStatusModel(policy.id.get, PolicyStatusEnum.Failed))

  private def sparkHome: String = Properties.envOrElse("SPARK_HOME", ClusterConfig.getString(AppConstant.SparkHome))

  /**
   * Checks if we have a valid Spark home.
   */
  private def validateSparkHome(): Unit = require(Try(sparkHome).isSuccess,
    "You must set the $SPARK_HOME path in configuration or environment")

  /**
   * Checks if supervise param is set when execution mode is standalone
   *
   * @return The result of checks as boolean value
   */
  def isStandaloneSupervise: Boolean =
    if (DetailConfig.getString(AppConstant.ExecutionMode) == AppConstant.ConfigStandAlone) {
      Try(ClusterConfig.getBoolean(AppConstant.StandAloneSupervise)).getOrElse(false)
    } else false

  private def launch(main: String, hdfsDriverFile: String, master: String, args: Map[String, String],
                     driverParams: Seq[String],
                     pluginsFiles: Seq[String]): Unit = {
    val sparkLauncher = new SparkLauncher()
      .setSparkHome(sparkHome)
      .setAppResource(hdfsDriverFile)
      .setMainClass(main)
      .setMaster(master)
    pluginsFiles.foreach(file => sparkLauncher.addJar(file))
    args.map({ case (k: String, v: String) => sparkLauncher.addSparkArg(k, v) })
    if (isStandaloneSupervise) sparkLauncher.addSparkArg(StandaloneSupervise)
    //Spark params (everything starting with spark.)
    sparkConf.map({ case (key: String, value: String) =>
      sparkLauncher.setConf(key, if (key == "spark.app.name") s"$value-${policy.name}" else value)
    })
    // Driver (Sparta) params
    driverParams.map(sparkLauncher.addAppArgs(_))

    log.info("Executing SparkLauncher...")

    val sparkProcessStatus: Future[(Boolean, Process)] =
      for {
        sparkProcess <- Future(sparkLauncher.launch)
      } yield (Await.result(Future(sparkProcess.waitFor() == 0), 20 seconds), sparkProcess)

    sparkProcessStatus.onComplete {
      case Success((exitCode, sparkProcess)) =>
        sparkLauncherStreams(exitCode, sparkProcess)
      case Failure(exception) =>
        log.error(exception.getMessage)
        throw exception
    }
  }

  private def sparkLauncherStreams(exitCode: Boolean, sparkProcess: Process): Unit = {

    def recursiveErrors(it: Iterator[String], count : Int): Unit = {
      log.info(it.next())
      if(it.hasNext && count < 50){
        recursiveErrors(it, count + 1)
      }
    }

    if(exitCode) log.info("Spark process exited successfully")
    else log.info("Spark process exited with timeout")

    Source.fromInputStream(sparkProcess.getInputStream).close()
    sparkProcess.getOutputStream.close()

    log.info("ErrorStream:")

    val error = Source.fromInputStream(sparkProcess.getErrorStream)
    recursiveErrors(error.getLines(), 0)
    error.close()
  }

  private def sparkArgs: Map[String, String] =
    ClusterLauncherActor.toMap(AppConstant.DeployMode, "--deploy-mode", ClusterConfig) ++
      ClusterLauncherActor.toMap(AppConstant.NumExecutors, "--num-executors", ClusterConfig) ++
      ClusterLauncherActor.toMap(AppConstant.ExecutorCores, "--executor-cores", ClusterConfig) ++
      ClusterLauncherActor.toMap(AppConstant.TotalExecutorCores, "--total-executor-cores", ClusterConfig) ++
      ClusterLauncherActor.toMap(AppConstant.ExecutorMemory, "--executor-memory", ClusterConfig) ++
      // Yarn only
      ClusterLauncherActor.toMap(AppConstant.YarnQueue, "--queue", ClusterConfig)

  private def render(config: Config, key: String): String = config.atKey(key).root.render(ConfigRenderOptions.concise)

  private def encode(value: String): String = BaseEncoding.base64().encode(value.getBytes)

  private def zkConfigEncoded: String = encode(render(ZookeeperConfig, "zookeeper"))

  private def detailConfigEncoded: String = encode(render(DetailConfig, "config"))

  private def pluginsEncoded(plugins: Seq[String]): String = encode((Seq(" ") ++ plugins).mkString(","))

  private def sparkConf: Seq[(String, String)] =
    ClusterConfig.entrySet()
      .filter(_.getKey.startsWith("spark.")).toSeq
      .map(e => (e.getKey, e.getValue.unwrapped.toString))
}

object ClusterLauncherActor extends SLF4JLogging {

  def toMap(key: String, newKey: String, config: Config): Map[String, String] =
    Try(config.getString(key)) match {
      case Success(value) =>
        Map(newKey -> value)
      case Failure(_) =>
        log.debug(s"The key $key was not defined in config.")
        Map.empty[String, String]
    }
}
