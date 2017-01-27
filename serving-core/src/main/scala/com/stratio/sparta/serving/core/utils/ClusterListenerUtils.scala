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


package com.stratio.sparta.serving.core.utils

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.StatusActor.{AddListener, Update}
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.typesafe.config.Config
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.spark.launcher.SparkAppHandle
import org.json4s.jackson.Serialization._

import scala.io.Source
import scala.util.{Failure, Success, Try}


trait ClusterListenerUtils extends SLF4JLogging with SpartaSerializer with PolicyStatusUtils {

  val statusActor: ActorRef

  /** Spark Launcher functions **/

  def addSparkListener(policy: PolicyModel): SparkAppHandle.Listener =
    new SparkAppHandle.Listener() {
      override def stateChanged(handle: SparkAppHandle): Unit = {
        log.info(s"Submission state changed to ... ${handle.getState.name()}")
        statusActor ! Update(PolicyStatusModel(policy.id.get, NotDefined, None, Try(handle.getState.name()).toOption))
      }

      override def infoChanged(handle: SparkAppHandle): Unit = {
        log.info(s"Submission info changed with status ... ${handle.getState.name()}")
      }
    }

  /** Sparta Listener functions for PolicyStatus **/

  //scalastyle:off
  def addClusterContextListener(policy: PolicyModel, clusterConfig: Config): Unit = {
    log.info(s"Listener added to ${policy.name} with id: ${policy.id.get}")
    statusActor ! AddListener(policy.id.get, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status != Launched && policyStatus.status != Starting && policyStatus.status != Started) {
          log.info("Stopping message received from Zookeeper")
          try {
            policyStatus.submissionId match {
              case Some(submissionId) =>
                try {
                  val url = killUrl(clusterConfig)
                  log.info(s"Killing submission ($submissionId) with Spark Submissions API in url: $url")
                  val post = if (submissionId.contains("driver")) {
                    val frameworkId = submissionId.substring(0, submissionId.indexOf("driver") - 1)
                    val sparkApplicationId = submissionId.substring(submissionId.indexOf("driver"))
                    log.info(s"The extracted Framework id is: $frameworkId")
                    log.info(s"The extracted Spark application id is: $sparkApplicationId")
                    new HttpPost(s"$url/$sparkApplicationId")
                  } else new HttpPost(s"$url/$submissionId")
                  val postResponse = HttpClientBuilder.create().build().execute(post)
                  Try {
                    read[SubmissionResponse](Source.fromInputStream(postResponse.getEntity.getContent).mkString)
                  } match {
                    case Success(submissionResponse) =>
                      log.info(s"Kill submission response status: ${submissionResponse.success}")
                    case Failure(e) =>
                      log.error("Impossible to parse submission killing response", e)
                  }
                } finally {
                  val message = s"The policy ${policy.name} was stopped correctly"
                  log.info(message)
                  statusActor ! Update(PolicyStatusModel(policy.id.get, Stopped, None, None, Some(message)))
                }
              case None =>
                log.info(s"The Sparta System don't have submission id associated to policy ${policy.name}")
            }
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache to contextListener closed correctly")
              case Failure(e) =>
                log.error(s"The node Cache to contextListener in Zookeeper is not closed correctly", e)
            }
          }
        }
      }
    })
  }

  def addClientContextListener(policy: PolicyModel, clusterConfig: Config, handler: SparkAppHandle): Unit = {
    log.info(s"Listener added to ${policy.name} with id: ${policy.id.get}")
    statusActor ! AddListener(policy.id.get, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status != Launched && policyStatus.status != Starting && policyStatus.status != Started) {
          log.info("Stopping message received from Zookeeper")
          try {
            policyStatus.submissionId match {
              case Some(submissionId) =>
                try {
                  log.info("Stopping submission policy with handler")
                  handler.stop()
                } finally {
                  val message = s"The policy ${policy.name} was stopped correctly"
                  log.info(message)
                  statusActor ! Update(PolicyStatusModel(policy.id.get, Stopped, None, None, Some(message)))
                }
              case None =>
                log.info(s"The Sparta System don't have submission id associated to policy ${policy.name}")
            }
          } finally {
            log.info("Killing submission policy with handler")
            handler.kill()
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache to contextListener closed correctly")
              case Failure(e) =>
                log.error(s"The node Cache to contextListener in Zookeeper is not closed correctly", e)
            }
          }
        }
      }
    })
  }

  //scalastyle:on

  def isCluster(policy: PolicyModel, clusterConfig: Config): Boolean = {
    policy.sparkConf.find(sparkProp =>
      sparkProp.sparkConfKey == DeployMode && sparkProp.sparkConfValue == ClusterValue) match {
      case Some(mode) => true
      case _ => Try(clusterConfig.getString(DeployMode)) match {
        case Success(mode) => mode == ClusterValue
        case Failure(e) => false
      }
    }
  }

  private def killUrl(clusterConfig: Config): String =
    s"http://${
      clusterConfig.getString(Master).trim
        .replace("spark://", "")
        .replace("mesos://", "")
    }" + Try(clusterConfig.getString(KillUrl)).getOrElse(DefaultkillUrl)
}
