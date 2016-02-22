/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.api.actor

import java.io.{OutputStream, ByteArrayOutputStream}

import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparkta.driver.util.ClusterSparkFiles
import com.stratio.sparkta.serving.api.actor.SparkStreamingContextActor._
import com.stratio.sparkta.serving.core.SparktaConfig
import com.stratio.sparkta.serving.core.dao.ConfigDAO
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SparktaSerializer}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.spark.launcher.SparkLauncher

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Properties, Success, Try}

class ClusterLauncherActor(policy: AggregationPoliciesModel, policyStatusActor: ActorRef) extends Actor
with SLF4JLogging
with SparktaSerializer {

  private val SparktaDriver = "com.stratio.sparkta.driver.SparktaClusterJob"
  private val StandaloneSupervise = "--supervise"

  private val ClusterConfig = SparktaConfig.getClusterConfig.get
  private val ZookeeperConfig = SparktaConfig.getZookeeperConfig.get
  private val HdfsConfig = SparktaConfig.getHdfsConfig.get
  private val DetailConfig = SparktaConfig.getDetailConfig.get

  private val Uploader = ClusterSparkFiles(policy)
  private val PolicyId = policy.id.get.trim
  private val Master = ClusterConfig.getString(AppConstant.Master)

  implicit val timeout: Timeout = Timeout(3.seconds)

  override def receive: PartialFunction[Any, Unit] = {
    case Start => doInitSparktaContext()
  }

  def doInitSparktaContext(): Unit = {
    Try {
      log.info("Init new cluster streamingContext with name " + policy.name)
      validateSparkHome()
      saveHdfsConfig()
      val driverPath = Uploader.getDriverFile
      val clusterFiles = Uploader.getClasspathFiles ++ Uploader.getPluginsFiles
      val driverParams = Seq(PolicyId, zkConfigEncoded, detailConfigEncoded)
      launch(SparktaDriver, driverPath, Master, sparkArgs, driverParams, clusterFiles.values.toSeq)
    } match {
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        setErrorStatus()
      case Success(_) =>
      //TODO add more statuses for the policies
    }
  }

  def saveHdfsConfig(): Unit = {
    val configRendered = render(HdfsConfig, "hdfs")
    ConfigDAO().dao.upsert(AppConstant.HdfsID, configRendered)
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
                     driverParams: Seq[String], clusterFiles: Seq[String]): Unit = {
    val sparkLauncher = new SparkLauncher()
      .setSparkHome(sparkHome)
      .setAppResource(hdfsDriverFile)
      .setMainClass(main)
      .setMaster(master)
      .setVerbose(true)
    clusterFiles.foreach(file => sparkLauncher.addJar(file))
    args.map({ case (k: String, v: String) => sparkLauncher.addSparkArg(k, v) })
    if (isStandaloneSupervise) sparkLauncher.addSparkArg(StandaloneSupervise)
    //Spark params (everything starting with spark.)
    sparkConf.map({ case (key: String, value: String) =>
      sparkLauncher.setConf(key, if(key == "spark.app.name")  s"$value-${policy.name}" else value)
    })
    // Driver (Sparkta) params
    driverParams.map(sparkLauncher.addAppArgs(_))

    log.info(s"$sparkHome/bin/spark-submit --master $master " ++ args.foldLeft("") {
      (s: String, pair: (String, String)) =>  s"$s ${pair._1} ${pair._2}"
    })

    Future[(Int, Process, BufferedSource, OutputStream)] {
      val sparkProcess = sparkLauncher.launch()
      val inputStream = Source.fromInputStream(sparkProcess.getInputStream)
      val outputStream = sparkProcess.getOutputStream

      Source.fromInputStream(sparkProcess.getErrorStream).close()
      (sparkProcess.waitFor(), sparkProcess, inputStream, outputStream)
    } onComplete {
      case Success((0, sparkProcess, inputStream, out)) =>
        log.info("Spark process launched")
        inputStream.getLines.foreach(log.info)
        val outStream = new ByteArrayOutputStream()
        out.write(outStream.toByteArray)
        log.info(outStream.toString)
        log.info("Spark process exited successfully")
      case Success((exitCode, sparkProcess, inputStream, out)) =>
        log.info("Spark process launched")
        inputStream.getLines.foreach(log.info)
        log.error(s"Spark process exited with code $exitCode")
        val outStream = new ByteArrayOutputStream()
        out.write(outStream.toByteArray)
        log.info(outStream.toString)
      case Failure(exception) =>
        log.error(exception.getMessage)
        throw exception
    }
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
