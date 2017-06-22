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

import akka.actor.{ActorContext, ActorRef}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyErrorModel, PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.typesafe.config.Config
import org.apache.curator.framework.recipes.cache.NodeCache
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.spark.launcher.SparkAppHandle
import org.json4s.jackson.Serialization._

import scala.io.Source
import scala.util.{Failure, Success, Try}

trait ClusterListenerUtils extends PolicyStatusUtils {

  /** Spark Launcher functions **/

  def addSparkListener(policy: PolicyModel): SparkAppHandle.Listener =
    new SparkAppHandle.Listener() {
      override def stateChanged(handle: SparkAppHandle): Unit = {
        log.info(s"Submission state changed to ... ${handle.getState.name()}")
        updateStatus(PolicyStatusModel(policy.id.get, NotDefined, None, Try(handle.getState.name()).toOption))
      }

      override def infoChanged(handle: SparkAppHandle): Unit = {
        log.info(s"Submission info changed with status ... ${handle.getState.name()}")
      }
    }

  /** Sparta Listener functions for PolicyStatus **/

  //scalastyle:off
  def addClusterListeners(policiesStatus: Try[Seq[PolicyStatusModel]], context: ActorContext): Unit = {
    policiesStatus match {
      case Success(statuses) =>
        statuses.foreach(policyStatus =>
          policyStatus.lastExecutionMode.foreach(execMode => {
            val pStatus = policyStatus.status
            if (pStatus == Started || pStatus == Starting || pStatus == Launched ||
              pStatus == Stopping || pStatus == Uploaded){
              if(execMode.contains(ClusterValue))
                SpartaConfig.getClusterConfig(Option(execMode.substring(0, execMode.lastIndexOf("-")))) match {
                  case Some(clusterConfig) =>
                    addClusterContextListener(policyStatus.id,
                      policyStatus.name.getOrElse("undefined"),
                      killUrl(clusterConfig))
                  case None =>
                    log.warn("Impossible to extract cluster configuration when initializing cluster listeners")
                }
              if(execMode.contains(MarathonValue))
                addMarathonContextListener(policyStatus.id, policyStatus.name.getOrElse("undefined"), context)
            }
          }))
      case Failure(e) =>
        log.error("Error when find all policy statuses. Impossible to add cluster listeners", e)
    }
  }

  def addMarathonContextListener(policyId: String,
                                 policyName: String,
                                 akkaContext: ActorContext,
                                 launcherActor: Option[ActorRef] = None): Unit = {
    log.info(s"Marathon context listener added to $policyName with id: $policyId")
    addListener(policyId, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status == Stopped || policyStatus.status == Failed) {
          log.info("Stopping message received from Zookeeper")
          try {
            val info = s"Finishing Sparta cluster job with Marathon API"
            log.info(info)
            policyStatus.marathonId match {
              case Some(marathonId) =>
                Try {
                  new MarathonService(akkaContext, curatorFramework).kill(marathonId)
                } match {
                  case Success(_) =>
                    val information = s"Finished correctly Sparta cluster job with Marathon API"
                    val newStatus = if (policyStatus.status == Failed) NotDefined else Finished
                    log.info(information)
                    updateStatus(PolicyStatusModel(id = policyId, status = newStatus, statusInfo = Some(information)))
                  case Failure(e) =>
                    val error = "Error when sending stop to Marathon API"
                    log.error(error, e)
                    updateStatus(PolicyStatusModel(id = policyId, status = Failed, statusInfo = Some(error),
                      lastError = Option(PolicyErrorModel(error, PhaseEnum.Execution, e.toString))
                    ))
                }
              case None =>
                log.warn(s"The Sparta System don't have Marathon id associated to policy $policyName")
            }
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache to marathon context listener closed correctly")
              case Failure(e) =>
                log.error(s"Node Cache to marathon context listener is not closed correctly", e)
            }
            launcherActor.foreach(actor => akkaContext.stop(actor))
          }
        }
      }
    })
  }

  def addClusterContextListener(policyId: String,
                                policyName: String,
                                killUrl: String,
                                launcherActor: Option[ActorRef] = None,
                                akkaContext: Option[ActorContext] = None): Unit = {
    log.info(s"Cluster context listener added to $policyName with id: $policyId")
    addListener(policyId, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status == Stopping || policyStatus.status == Failed) {
          log.info("Stopping message received from Zookeeper")
          try {
            policyStatus.submissionId match {
              case Some(submissionId) =>
                Try {
                  log.info(s"Killing submission ($submissionId) with Spark Submissions API in url: $killUrl")
                  val post = new HttpPost(s"$killUrl/$submissionId")
                  val postResponse = HttpClientBuilder.create().build().execute(post)

                  read[SubmissionResponse](Source.fromInputStream(postResponse.getEntity.getContent).mkString)
                } match {
                  case Success(submissionResponse) if submissionResponse.success =>
                    val information = s"Killed correctly Sparta cluster job with Spark API"
                    val newStatus = if (policyStatus.status == Failed) NotDefined else Killed
                    log.info(information)
                    updateStatus(PolicyStatusModel(id = policyId, status = newStatus, statusInfo = Some(information)))
                  case Success(submissionResponse) =>
                    log.debug(s"Failed response : $submissionResponse")
                    val information = s"Error while stopping task"
                    log.info(information)
                    updateStatus(PolicyStatusModel(id = policyId, status = Failed, statusInfo = Some(information),
                      lastError = submissionResponse.message.map(error => PolicyErrorModel(information, PhaseEnum.Execution, error))))
                  case Failure(e) =>
                    val error = "Impossible to parse submission killing response"
                    log.error(error, e)
                    updateStatus(PolicyStatusModel(id = policyId, status = Failed, statusInfo = Some(error),
                      lastError = Option(PolicyErrorModel(error, PhaseEnum.Execution, e.toString))
                    ))
                }
              case None =>
                log.info(s"The Sparta System don't have submission id associated to policy $policyName")
            }
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache to cluster context listener closed correctly")
              case Failure(e) =>
                log.error(s"Node Cache to cluster context listener is not closed correctly", e)
            }
            akkaContext.foreach(context => launcherActor.foreach(actor => context.stop(actor)))
          }
        }
      }
    })
  }

  def addClientContextListener(policyId: String,
                               policyName: String,
                               handler: SparkAppHandle,
                               launcherActor: ActorRef,
                               akkaContext: ActorContext): Unit = {
    log.info(s"Client context Listener added to $policyName with id: $policyId")
    addListener(policyId, (policyStatus: PolicyStatusModel, nodeCache: NodeCache) => {
      synchronized {
        if (policyStatus.status == Stopping || policyStatus.status == Failed) {
          log.info("Stopping message received from Zookeeper")
          try {
            policyStatus.submissionId match {
              case Some(_) =>
                Try {
                  log.info("Stopping submission policy with handler")
                  handler.stop()
                } match {
                  case Success(_) =>
                    val information = s"Stopped correctly Sparta cluster job with Spark Handler"
                    log.info(information)
                    updateStatus(PolicyStatusModel(id = policyId, status = Stopped, statusInfo = Some(information)))
                  case Failure(e: Exception) =>
                    val error = s"Error stopping Sparta cluster job with Spark Handler, killing it ..."
                    log.warn(error, e)
                    Try(handler.kill()) match {
                      case Success(_) =>
                        val information = s"Killed Sparta cluster job with Spark Handler"
                        log.info(information)
                        updateStatus(PolicyStatusModel(
                          id = policyId, status = Stopped, statusInfo = Some(information),
                          lastError = Option(PolicyErrorModel(error, PhaseEnum.Execution, e.toString))))
                      case Failure(e: Exception) =>
                        val error = s"Problems killing Sparta cluster job with Spark Handler"
                        log.info(error)
                        updateStatus(PolicyStatusModel(
                          id = policyId, status = Stopped, statusInfo = Some(error),
                          lastError = Option(PolicyErrorModel(error, PhaseEnum.Execution, e.toString))))
                    }
                }
              case None =>
                val information = s"The Sparta System don't have submission id associated to policy $policyName"
                log.info(information)
                updateStatus(PolicyStatusModel(id = policyId, status = Failed, statusInfo = Some(information)))
            }
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache to client context listener closed correctly")
              case Failure(e) =>
                log.error(s"Node Cache to client context listener is not closed correctly", e)
            }
            akkaContext.stop(launcherActor)
          }
        }
      }
    })
  }

  //scalastyle:on

  def killUrl(clusterConfig: Config): String =
    Try(clusterConfig.getString(KillUrl)).getOrElse(DefaultkillUrl).trim
}
