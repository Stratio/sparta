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
import com.stratio.sparta.driver.SpartaJob._
import com.stratio.sparta.driver.factory._
import com.stratio.sparta.sdk._
import com.stratio.sparta.serving.core.actor.PolicyStatusActor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.helpers.DateOperationsHelper
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.typesafe.config.Config
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

case class StreamingContextService(policyStatusActor: Option[ActorRef] = None,
                                   generalConfig: Option[Config] = None) {

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)
  final val OutputsSparkConfiguration = "getSparkConfiguration"

  def standAloneStreamingContext(policy: PolicyModel, files: Seq[File]): StreamingContext = {
    runStatusListener(policy.id.get, policy.name, exit = false)
    if (autoDeleteCheckpointPath(policy)) deleteCheckpointPath(policy)

    val ssc = StreamingContext.getOrCreate(generateCheckpointPath(policy), () => {
      log.info(s"Nothing in checkpoint path: ${generateCheckpointPath(policy)}")
      SpartaJob(policy).run(getStandAloneSparkContext(policy, files))
    })

    SparkContextFactory.setSparkContext(ssc.sparkContext)
    SparkContextFactory.setSparkStreamingContext(ssc)
    SparkContextFactory.setInitialSentences(policy.initSqlSentences.map(modelSentence => modelSentence.sentence))

    ssc
  }

  def clusterStreamingContext(policy: PolicyModel,
                              files: Seq[String],
                              detailConfig: Map[String, String]): StreamingContext = {
    val exitWhenStop = true

    runStatusListener(policy.id.get, policy.name, exitWhenStop)
    if (autoDeleteCheckpointPath(policy)) deleteCheckpointPath(policy)

    val ssc = StreamingContext.getOrCreate(generateCheckpointPath(policy), () => {
      log.info(s"Nothing in checkpoint path: ${generateCheckpointPath(policy)}")
      SpartaJob(policy).run(getClusterSparkContext(policy, files, detailConfig))
    })

    SparkContextFactory.setSparkContext(ssc.sparkContext)
    SparkContextFactory.setSparkStreamingContext(ssc)
    SparkContextFactory.setInitialSentences(policy.initSqlSentences.map(modelSentence => modelSentence.sentence))

    ssc
  }

  private def getStandAloneSparkContext(apConfig: PolicyModel, jars: Seq[File]): SparkContext = {
    val pluginsSparkConfig = SpartaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix)
    val policySparkConfig = SpartaJob.getSparkConfigFromPolicy(apConfig)
    val standAloneConfig = Try(generalConfig.get.getConfig(AppConstant.ConfigLocal)).toOption



    SparkContextFactory.sparkStandAloneContextInstance(standAloneConfig, policySparkConfig ++ pluginsSparkConfig, jars)
  }

  private def getClusterSparkContext(policy: PolicyModel,
                                     classPath: Seq[String],
                                     detailConfig: Map[String, String]): SparkContext = {
    val pluginsSparkConfig = SpartaJob.getSparkConfigs(policy, OutputsSparkConfiguration, Output.ClassSuffix)
    val policySparkConfig = SpartaJob.getSparkConfigFromPolicy(policy)

    SparkContextFactory.sparkClusterContextInstance(policySparkConfig ++ pluginsSparkConfig ++ detailConfig, classPath)
  }

  //scalastyle:off
  private def runStatusListener(policyId: String,
                                name: String,
                                exit: Boolean = false): Unit = {
    if (policyStatusActor.isDefined) {
      log.info(s"Listener added for: $policyId")
      policyStatusActor.get ? AddListener(policyId, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
        synchronized {
          if (policyStatus.status.id equals Stopping.id) {
            try {
              log.info("Stopping message received from Zookeeper")

              import scala.concurrent.ExecutionContext.Implicits.global

              val awaitTimeOut =
                Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.AwaitStopTermination)).toOption
                  .flatMap(x => if (x == "") None else Some(x)).getOrElse(AppConstant.DefaultAwaitStopTermination)
              log.info(s"Starting scheduler to supervise the Spark Job termination timeout, with time: $awaitTimeOut")
              AppConstant.SchedulerSystem.scheduler.scheduleOnce(
                DateOperationsHelper.parseValueToMilliSeconds(awaitTimeOut) milli)(closeForcibly(
                policyId, policyStatusActor.get, exit))

              SparkContextFactory.destroySparkStreamingContext()
            } finally {
              try {
                Await.result(policyStatusActor.get ? Update(PolicyStatusModel(policyId, Stopped)), timeout.duration)
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
                SparkContextFactory.destroySparkContext()
                shutdownSchedulerSystem()
                log.info("Closing the application")
                System.exit(0)
              }
            }
          }
        }
      })
    }
  }

  private def closeForcibly(policyId: String,
                            policyStatusActor: ActorRef,
                            exit: Boolean): Unit = {
    val status = Stopped
    log.warn(s"The Spark Context will been stopped, the policy will be set in status: $status")
    policyStatusActor ! Update(PolicyStatusModel(policyId, status))
    SparkContextFactory.destroySparkContext(destroyStreamingContext = false)
    if (exit) {
      shutdownSchedulerSystem()
      log.info("Closing the application")
      System.exit(0)
    }
  }

  private def shutdownSchedulerSystem(): Unit = {
    if (!AppConstant.SchedulerSystem.isTerminated) {
      log.info("Shutdown the scheduler system")
      AppConstant.SchedulerSystem.shutdown()
    }
  }
}