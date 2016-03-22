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
package com.stratio.sparta.driver.service

import java.io.File
import java.net.URI
import com.stratio.sparta.driver.util.HdfsUtils
import com.stratio.sparta.serving.core.SpartaConfig
import org.apache.hadoop.conf.Configuration

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Success
import scala.util.Try

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext

import com.stratio.sparta.driver.SpartaJob
import com.stratio.sparta.driver.factory._
import com.stratio.sparta.sdk._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.AddListener
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.Kill
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparta.serving.core.policy.status.PolicyStatusEnum

case class StreamingContextService(policyStatusActor: Option[ActorRef] = None, generalConfig: Option[Config] = None)
  extends SLF4JLogging {

  implicit val timeout: Timeout = Timeout(3.seconds)
  final val OutputsSparkConfiguration = "getSparkConfiguration"

  def standAloneStreamingContext(apConfig: AggregationPoliciesModel, files: Seq[File]): Option[StreamingContext] = {
    runStatusListener(apConfig.id.get, apConfig.name)

    val ssc =
      if(AggregationPoliciesModel.goesCheckpointToHDFS(apConfig)) {
        StreamingContext.getOrCreate(
          s"${HdfsUtils(SpartaConfig.getHdfsConfig).getPolicyCheckpointPath(apConfig)}",
          () => {
            log.info(s"Nothing in checkpoint path: ${AggregationPoliciesModel.checkpointPath(apConfig)}")
            SpartaJob(apConfig).run(getStandAloneSparkContext(apConfig, files))
          },
          HdfsUtils.hdfsConfiguration(SpartaConfig.getHdfsConfig)
        )
      } else
        StreamingContext.getOrCreate(
          AggregationPoliciesModel.checkpointPath(apConfig),
          () => {
            log.info(s"Nothing in checkpoint path: ${AggregationPoliciesModel.checkpointPath(apConfig)}")
            SpartaJob(apConfig).run(getStandAloneSparkContext(apConfig, files))
          }
        )

    SparkContextFactory.setSparkContext(ssc.sparkContext)
    SparkContextFactory.setSparkStreamingContext(ssc)

    Option(ssc)
  }

  def clusterStreamingContext(apConfig: AggregationPoliciesModel,
                              files: Seq[String],
                              detailConfig: Map[String, String]): Option[StreamingContext] = {
    val exitWhenStop = true
    runStatusListener(apConfig.id.get, apConfig.name, exitWhenStop)

    val ssc = StreamingContext.getOrCreate(AggregationPoliciesModel.checkpointPath(apConfig), () => {
      log.info(s"Nothing in checkpoint path: ${AggregationPoliciesModel.checkpointPath(apConfig)}")
      SpartaJob(apConfig).run(getClusterSparkContext(apConfig, files, detailConfig))
    })

    SparkContextFactory.setSparkContext(ssc.sparkContext)
    SparkContextFactory.setSparkStreamingContext(ssc)

    Option(ssc)
  }

  private def getStandAloneSparkContext(apConfig: AggregationPoliciesModel, jars: Seq[File]): SparkContext = {
    val pluginsSparkConfig = SpartaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix)
    val standAloneConfig = Try(generalConfig.get.getConfig(AppConstant.ConfigLocal)) match {
      case Success(config) => Some(config)
      case _ => None
    }
    SparkContextFactory.sparkStandAloneContextInstance(standAloneConfig, pluginsSparkConfig, jars)
  }

  private def getClusterSparkContext(apConfig: AggregationPoliciesModel,
                                     classPath: Seq[String],
                                     detailConfig: Map[String, String]): SparkContext = {
    val pluginsSparkConfig =
      SpartaJob.getSparkConfigs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix) ++ detailConfig
    SparkContextFactory.sparkClusterContextInstance(pluginsSparkConfig, classPath)
  }

  private def runStatusListener(policyId: String, name: String, exit: Boolean = false): Unit = {
    if (policyStatusActor.isDefined) {
      log.info(s"Listener added for: $policyId")
      policyStatusActor.get ? AddListener(policyId, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
        synchronized {
          if (policyStatus.status.id equals PolicyStatusEnum.Stopping.id) {
            try {

              log.info("Stopping message received from Zookeeper")
              SparkContextFactory.destroySparkStreamingContext()

            } finally {

              Try(Await.result(policyStatusActor.get ? Kill(name), timeout.duration) match {
                case false => log.warn(s"The actor with name: $name has been stopped previously")
              }).getOrElse(log.warn(s"The actor with name: $name could not be stopped correctly"))

              Try(Await.result(policyStatusActor.get ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Stopped)),
                timeout.duration) match {
                case None => log.warn(s"The policy status can not be changed")
              }).getOrElse(log.warn(s"The policy status could be wrong"))

              Try(nodeCache.close()).getOrElse(log.warn(s"The nodeCache in Zookeeper is not closed correctly"))

              if (exit) {
                SparkContextFactory.destroySparkContext()
                log.info("Closing the application")
                System.exit(0)
              }
            }
          }
        }
      })
    }
  }
}