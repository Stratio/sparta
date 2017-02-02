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
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.PolicyStatusModel
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.typesafe.config.Config
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.json4s.jackson.Serialization._

import scala.io.Source
import scala.util.{Failure, Success, Try}

trait ClusterListenerUtils extends SLF4JLogging with SpartaSerializer {

  val statusActor: ActorRef



  /** Sparta Listener functions for PolicyStatus **/

  def addClusterListeners(policiesStatus: Try[Seq[PolicyStatusModel]]): Unit = {
    policiesStatus match {
      case Success(statuses) =>
        statuses.foreach(policyStatus =>
          policyStatus.lastExecutionMode.foreach(execMode => {
            val pStatus = policyStatus.status
            if ((pStatus == Started || pStatus == Starting || pStatus == Launched) && execMode.contains(ClusterValue))
              SpartaConfig.getClusterConfig(Option(execMode.substring(0, execMode.lastIndexOf("-")))) match {
                case Some(clusterConfig) =>
                  addClusterContextListener(policyStatus.id, policyStatus.name.getOrElse("undefined"), clusterConfig)
                case None =>
                  log.warn("Impossible to extract cluster configuration when initializing cluster listeners")
              }
          }))
      case Failure(e) =>
        log.error("Error when find all policy statuses. Impossible to add cluster listeners", e)
    }
  }

  //scalastyle:off
  def addClusterContextListener(policyId: String, policyName: String, clusterConfig: Config): Unit = {
    log.info(s"Listener added to $policyName with id: $policyId")
    statusActor ! AddListener(policyId, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status == Stopping) {
          log.info("Stopping message received from Zookeeper")
          try {
            policyStatus.submissionId match {
              case Some(submissionId) =>
                Try {
                  val url = killUrl(clusterConfig)
                  log.info(s"Killing submission ($submissionId) with Spark Submissions API in url: $url")
                  val post = new HttpPost(s"$url/$submissionId")
                  val postResponse = HttpClientBuilder.create().build().execute(post)
                  read[SubmissionResponse](Source.fromInputStream(postResponse.getEntity.getContent).mkString)
                } match {
                  case Success(submissionResponse) if submissionResponse.success =>
                    val information = s"Stopped correctly Sparta cluster job with Spark API"
                    log.info(information)
                    statusActor ! Update(PolicyStatusModel(
                      id = policyId, status = Stopped, statusInfo = Some(information)))
                  case Success(submissionResponse) =>
                    log.debug(s"Failed response : $submissionResponse")
                    val information = s"Error while stoping task : ${submissionResponse.message.getOrElse("N/A")}"
                    log.info(information)
                    statusActor ! Update(PolicyStatusModel(
                      id = policyId, status = Failed, statusInfo = Some(information)))
                  case Failure(e) =>
                    val error = "Impossible to parse submission killing response"
                    log.error(error, e)
                    statusActor ! Update(PolicyStatusModel(
                      id = policyId, status = Failed, statusInfo = Some(error)))
                }
              case None =>
                log.info(s"The Sparta System don't have submission id associated to policy $policyName")
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

  //scalastyle:on

  private def killUrl(clusterConfig: Config): String =
    s"http://${
      clusterConfig.getString(Master).trim
        .replace("spark://", "")
        .replace("mesos://", "")
    }" + Try(clusterConfig.getString(KillUrl)).getOrElse(DefaultkillUrl)
}
