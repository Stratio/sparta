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
import com.stratio.sparta.driver.SpartaPipeline
import com.stratio.sparta.driver.factory.SparkContextFactory._
import com.stratio.sparta.sdk.pipeline.output.Output
import com.stratio.sparta.serving.core.actor.PolicyStatusActor._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{CheckpointUtils, PolicyUtils, SchedulerUtils}
import com.typesafe.config.Config
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

case class StreamingContextService(policyStatusActor: Option[ActorRef] = None,
                                   generalConfig: Option[Config] = None)
  extends SchedulerUtils
    with CheckpointUtils
    with PolicyUtils {

  final val OutputsSparkConfiguration = "getSparkConfiguration"

  def standAloneStreamingContext(policy: PolicyModel, files: Seq[File]): StreamingContext = {
    killLocalContextListener(policy.id.get, policy.name)

    if (autoDeleteCheckpointPath(policy)) deleteCheckpointPath(policy)

    createLocalCheckpointPath(policy)

    val ssc = SpartaPipeline(policy).run(getStandAloneSparkContext(policy, files))

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(policy.initSqlSentences.map(modelSentence => modelSentence.sentence))

    ssc
  }

  def clusterStreamingContext(policy: PolicyModel,
                              files: Seq[String],
                              detailConfig: Map[String, String]): StreamingContext = {
    if (autoDeleteCheckpointPath(policy)) deleteCheckpointPath(policy)

    val ssc = StreamingContext.getOrCreate(checkpointPath(policy), () => {
      log.info(s"Nothing in checkpoint path: ${checkpointPath(policy)}")
      SpartaPipeline(policy).run(getClusterSparkContext(policy, files, detailConfig))
    })

    setSparkContext(ssc.sparkContext)
    setSparkStreamingContext(ssc)
    setInitialSentences(policy.initSqlSentences.map(modelSentence => modelSentence.sentence))

    ssc
  }

  private def getStandAloneSparkContext(apConfig: PolicyModel, jars: Seq[File]): SparkContext = {
    val outputsSparkConfig = SpartaPipeline.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix)
    val policySparkConfig = getSparkConfigFromPolicy(apConfig)
    val standAloneConfig = Try(generalConfig.get.getConfig(ConfigLocal)).toOption

    sparkStandAloneContextInstance(standAloneConfig, policySparkConfig ++ outputsSparkConfig, jars)
  }

  private def getClusterSparkContext(policy: PolicyModel,
                                     classPath: Seq[String],
                                     detailConfig: Map[String, String]): SparkContext = {
    val outputsSparkConfig = SpartaPipeline.getSparkConfigs(policy, OutputsSparkConfiguration, Output.ClassSuffix)
    val policySparkConfig = getSparkConfigFromPolicy(policy)

    sparkClusterContextInstance(policySparkConfig ++ outputsSparkConfig ++ detailConfig, classPath)
  }

  private def killLocalContextListener(policyId: String, name: String): Unit = {
    policyStatusActor match {
      case Some(statusActor) =>
        log.info(s"Listener added for: $policyId")
        statusActor ! AddListener(policyId, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
          synchronized {
            if (policyStatus.status == Stopping) {
              try {
                log.info("Stopping message received from Zookeeper")
                closeContexts(policyId, statusActor)
              } finally {
                Try(nodeCache.close()) match {
                  case Success(_) =>
                    log.info("Node cache closed correctly")
                  case Failure(e) =>
                    log.error(s"The nodeCache in Zookeeper is not closed correctly", e)
                }
              }
            }
          }
        })
      case None => log.info("The status actor is not defined")
    }
  }

  private def closeContexts(policyId: String, policyStatusActor: ActorRef): Unit = {
    val information = "The Spark Context have been stopped correctly in the local listener"
    log.info(information)
    policyStatusActor ! Update(PolicyStatusModel(policyId, Stopped, None, None, Some(information)))
    destroySparkContext(destroyStreamingContext = true)
  }
}