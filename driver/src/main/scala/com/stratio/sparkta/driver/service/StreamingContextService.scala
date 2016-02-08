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

package com.stratio.sparkta.driver.service

import java.io.{File, Serializable}
import java.net.URI

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import akka.pattern.ask
import akka.util.Timeout
import com.stratio.sparkta.driver.SparktaJob
import com.stratio.sparkta.driver.factory._
import com.stratio.sparkta.driver.util.ReflectionUtils
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.stratio.sparkta.serving.core.models._
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor.{AddListener, Kill, Update}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum
import com.typesafe.config.Config
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.spark.SparkContext
import org.apache.spark.streaming.StreamingContext

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Success, Try}

case class StreamingContextService(policyStatusActor: Option[ActorRef] = None, generalConfig: Option[Config] = None)
  extends SLF4JLogging {

  implicit val timeout: Timeout = Timeout(3.seconds)
  final val OutputsSparkConfiguration = "getSparkConfiguration"

  def standAloneStreamingContext(apConfig: CommonPoliciesModel, files: Seq[File]): Option[StreamingContext] = {
    runStatusListener(apConfig.id.get, apConfig.name)

    val ssc = StreamingContext.getOrCreate(CommonPoliciesModel.checkpointPath(apConfig), () => {
      log.info(s"Nothing in checkpoint path: ${CommonPoliciesModel.checkpointPath(apConfig)}")
      SparktaJob.run(getStandAloneSparkContext(apConfig, files), apConfig)
    })

    SparkContextFactory.setSparkContext(ssc.sparkContext)
    SparkContextFactory.setSparkStreamingContext(ssc)

    Option(ssc)
  }

  def clusterStreamingContext(apConfig: CommonPoliciesModel,
                              files: Seq[URI],
                              detailConfig: Map[String, String]): Option[StreamingContext] = {
    val exitWhenStop = true
    runStatusListener(apConfig.id.get, apConfig.name, exitWhenStop)

    val ssc = StreamingContext.getOrCreate(CommonPoliciesModel.checkpointPath(apConfig), () => {
      log.info(s"Nothing in checkpoint path: ${CommonPoliciesModel.checkpointPath(apConfig)}")
      SparktaJob.run(getClusterSparkContext(apConfig, files, detailConfig), apConfig)
    })

    SparkContextFactory.setSparkContext(ssc.sparkContext)
    SparkContextFactory.setSparkStreamingContext(ssc)

    Option(ssc)
  }

  private def getStandAloneSparkContext(apConfig: CommonPoliciesModel, jars: Seq[File]): SparkContext = {
    val pluginsSparkConfig =
      sparkConfigFromOutputs(apConfig, OutputsSparkConfiguration, Output.ClassSuffix, new ReflectionUtils())
    val standAloneConfig = Try(generalConfig.get.getConfig(AppConstant.ConfigLocal)) match {
      case Success(config) => Some(config)
      case _ => None
    }
    SparkContextFactory.sparkStandAloneContextInstance(standAloneConfig, pluginsSparkConfig, jars)
  }

  private def getClusterSparkContext(apConfig: CommonPoliciesModel,
                                     classPath: Seq[URI],
                                     detailConfig: Map[String, String]): SparkContext = {
    val pluginsSparkConfig = sparkConfigFromOutputs(apConfig,
      OutputsSparkConfiguration,
      Output.ClassSuffix,
      new ReflectionUtils
    ) ++ detailConfig
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

  private def sparkConfigFromOutputs(apConfig: CommonPoliciesModel,
                                     methodName: String,
                                     suffix: String,
                                     refUtils: ReflectionUtils): Map[String, String] = {
    log.info("Initializing reflection")
    apConfig.outputs.flatMap(o => {
      val clazzToInstance = refUtils.getClasspathMap.getOrElse(o.`type` + suffix, o.`type` + suffix)
      val clazz = Class.forName(clazzToInstance)
      clazz.getMethods.find(p => p.getName == methodName) match {
        case Some(method) => {
          method.setAccessible(true)
          method.invoke(clazz, o.configuration.asInstanceOf[Map[String, Serializable]])
            .asInstanceOf[Seq[(String, String)]]
        }
        case None => Seq()
      }
    }).toMap
  }
}