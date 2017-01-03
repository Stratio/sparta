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

package com.stratio.sparta.driver.service

import java.io.File

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparta.driver.SpartaJob
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.serving.core.actor.PolicyStatusActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, SchedulerUtils}
import com.typesafe.config.Config
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

case class StreamingContextService(policyStatusActor: Option[ActorRef] = None,
                                   generalConfig: Option[Config] = None) extends SchedulerUtils with CheckpointUtils {

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)

  final val OutputsSparkConfiguration = "getSparkConfiguration"

  def standAloneStreamingContext(policy: PolicyModel, files: Seq[File]): StreamingContext = {
    runStatusListener(policy.id.get, policy.name, exit = false)

    if (autoDeleteCheckpointPath(policy)) deleteCheckpointPath(policy)

    createLocalCheckpointPath(policy)

    val ssc = SpartaJob(policy).run(getStandAloneSparkContext(policy, files))

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(policy.initSqlSentences.map(modelSentence => modelSentence.sentence))

    ssc
  }

  def clusterStreamingContext(policy: PolicyModel,
                              files: Seq[String],
                              detailConfig: Map[String, String]): StreamingContext = {
    runStatusListener(policy.id.get, policy.name, exit = true)
    if (autoDeleteCheckpointPath(policy)) deleteCheckpointPath(policy)

    val ssc = StreamingContext.getOrCreate(checkpointPath(policy), () => {
      log.info(s"Nothing in checkpoint path: ${checkpointPath(policy)}")
      SpartaJob(policy).run(getClusterSparkContext(policy, files, detailConfig))
    })

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(policy.initSqlSentences.map(modelSentence => modelSentence.sentence))

    ssc
  }

  private def getStandAloneSparkContext(apConfig: PolicyModel, jars: Seq[File]): SparkContext = {
    val outputsSparkConfig = SpartaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix)
    val policySparkConfig = SpartaJob.getSparkConfigFromPolicy(apConfig)
    val standAloneConfig = Try(generalConfig.get.getConfig(ConfigLocal)).toOption

    sparkStandAloneContextInstance(standAloneConfig, policySparkConfig ++ outputsSparkConfig, jars)
  }

  private def getClusterSparkContext(policy: PolicyModel,
                                     classPath: Seq[String],
                                     detailConfig: Map[String, String]): SparkContext = {
    val outputsSparkConfig = SpartaJob.getSparkConfigs(policy, OutputsSparkConfiguration, Output.ClassSuffix)
    val policySparkConfig = SpartaJob.getSparkConfigFromPolicy(policy)

    sparkClusterContextInstance(policySparkConfig ++ outputsSparkConfig ++ detailConfig, classPath)
  }

  //scalastyle:off
  private def runStatusListener(policyId: String,
                                name: String,
                                exit: Boolean = false): Unit = {
    policyStatusActor match {
      case Some(statusActor) =>
        log.info(s"Listener added for: $policyId")
        statusActor ? AddListener(policyId, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
          synchronized {
            if (policyStatus.status.id equals Stopping.id) {
              try {
                log.info("Stopping message received from Zookeeper")
                val closeStreamingContext = scheduleOneTask(
                  AwaitStreamingContextStop,
                  DefaultAwaitStreamingContextStop
                )(closeSpartaForcibly(policyId, statusActor, exit))

                destroySparkStreamingContext()
                if (sparkStreamingInstance.isEmpty) closeStreamingContext.cancel()
              } finally {
                try {
                  Await.result(statusActor ? Update(PolicyStatusModel(policyId, Stopped)), timeout.duration)
                  match {
                    case None => log.warn(s"The policy status can not be changed")
                    case Some(_) => log.info(s"The policy status is changed to Stopped in finish action")
                  }
                } catch {
                  case e: Exception =>
                    log.warn(s"The policy status could not be changed correctly. Exception: ${e.getLocalizedMessage}")
                }
                try {
                  nodeCache.close()
                } catch {
                  case e: Exception =>
                    log.warn(s"The nodeCache in Zookeeper is not closed correctly.  Exception: ${e.getLocalizedMessage}")
                }
                if (exit) {
                  val scheduledExit = scheduleOneTask(AwaitSparkContextStop, DefaultAwaitSparkContextStop)(exitApp())

                  destroySparkContext()
                  if (sparkContextInstance.isEmpty) scheduledExit.cancel()
                  exitApp()
                }
              }
            }
          }
        })
      case None => log.info("The status actor is not defined")
    }
  }

  //scalastyle:on

  private def closeSpartaForcibly(policyId: String,
                                  policyStatusActor: ActorRef,
                                  exit: Boolean): Unit = {
    val status = Stopped

    log.warn(s"The Spark Context will been stopped, the policy will be set in status: $status")
    policyStatusActor ! Update(PolicyStatusModel(policyId, status))
    if (exit) {
      val scheduledExit = scheduleOneTask(AwaitSparkContextStop, DefaultAwaitSparkContextStop)(exitApp())

      destroySparkContext(destroyStreamingContext = false)
      if (sparkContextInstance.isEmpty) scheduledExit.cancel()
      exitApp()
    } else destroySparkContext(destroyStreamingContext = false)
  }

  private def exitApp(): Unit = {
    shutdownSchedulerSystem()
    log.info("Closing the application")
    System.exit(0)
  }

  private def shutdownSchedulerSystem(): Unit = {
    if (!SchedulerSystem.isTerminated) {
      log.info("Shutting down the scheduler system")
      SchedulerSystem.shutdown()
    }
  }
}