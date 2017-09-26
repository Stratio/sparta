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

package com.stratio.sparta.serving.core.services

import akka.actor.{ActorContext, ActorRef}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, Workflow, WorkflowError, WorkflowStatus}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener}
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.spark.launcher.SparkAppHandle
import org.json4s.jackson.Serialization._

import scala.io.Source
import scala.util.{Failure, Success, Try}

class ListenerService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)


  /**
   * Adds a listener to one workflow and executes the callback when it changed.
   *
   * @param id       of the workflow.
   * @param callback with a function that will be executed.
   */
  def addWorkflowStatusListener(id: String, callback: (WorkflowStatus, NodeCache) => Unit): Unit = {
    val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
    val nodeCache: NodeCache = new NodeCache(curatorFramework, statusPath)
    nodeCache.getListenable.addListener(new NodeCacheListener {
      override def nodeChanged(): Unit = {
        Try(new String(nodeCache.getCurrentData.getData)) match {
          case Success(value) =>
            callback(read[WorkflowStatus](value), nodeCache)
          case Failure(e) =>
            log.error(s"NodeCache value: ${nodeCache.getCurrentData}", e)
        }
      }
    })
    nodeCache.start()
  }

  /** Spark Launcher functions **/

  def addSparkListener(workflow: Workflow): SparkAppHandle.Listener =
    new SparkAppHandle.Listener() {
      override def stateChanged(handle: SparkAppHandle): Unit = {
        log.info(s"Submission state changed to ... ${handle.getState.name()}")
      }

      override def infoChanged(handle: SparkAppHandle): Unit = {
        log.info(s"Submission info changed with status ... ${handle.getState.name()}")
      }
    }

  /** Sparta Listener functions for WorkflowStatus **/

  //scalastyle:off
  def addClusterListeners(workflowStatuses: Try[Seq[WorkflowStatus]], context: ActorContext): Unit = {
    workflowStatuses match {
      case Success(statuses) =>
        statuses.foreach(workflowStatus =>
          workflowStatus.lastExecutionMode.foreach(execMode => {
            val pStatus = workflowStatus.status
            if (pStatus == Started || pStatus == Starting || pStatus == Launched ||
              pStatus == Stopping || pStatus == Uploaded) {
              if (execMode.contains(ConfigMesos) && workflowStatus.killUrl.isDefined)
                addClusterContextListener(
                  workflowStatus.id,
                  workflowStatus.name.getOrElse("undefined"),
                  workflowStatus.killUrl.get
                )
              if (execMode.contains(ConfigMarathon))
                addMarathonContextListener(workflowStatus.id, workflowStatus.name.getOrElse("undefined"), context)
            }
          }))
      case Failure(e) =>
        log.error("An error was encounter while retrieving all the workflow statuses. Impossible to add cluster listeners", e)
    }
  }

  def addMarathonContextListener(workflowId: String,
                                 workflowName: String,
                                 akkaContext: ActorContext,
                                 launcherActor: Option[ActorRef] = None): Unit = {
    log.info(s"Marathon context listener added to $workflowName with id: $workflowId")
    addWorkflowStatusListener(workflowId, (workflowStatus: WorkflowStatus, nodeCache: NodeCache) => {
      synchronized {
        if (workflowStatus.status == Stopped || workflowStatus.status == Failed) {
          log.info("Stop message received from Zookeeper")
          try {
            val info = s"Finishing Sparta cluster job in Marathon API"
            log.info(info)
            workflowStatus.marathonId match {
              case Some(marathonId) =>
                Try {
                  new MarathonService(akkaContext, curatorFramework).kill(marathonId)
                } match {
                  case Success(_) =>
                    val information = s"Sparta cluster job correctly finished in Marathon API"
                    val newStatus = if (workflowStatus.status == Failed) NotDefined else Finished
                    log.info(information)
                    statusService.update(WorkflowStatus(
                      id = workflowId,
                      status = newStatus,
                      statusInfo = Some(information)
                    ))
                  case Failure(e) =>
                    val error = "An error was encountered while sending a stop message to Marathon API"
                    log.error(error, e)
                    statusService.update(WorkflowStatus(
                      id = workflowId,
                      status = Failed,
                      statusInfo = Some(error),
                      lastError = Option(WorkflowError(error, PhaseEnum.Execution, e.toString))
                    ))
                }
              case None =>
                log.warn(s"The Sparta System does not have a Marathon id associated to workflow: $workflowName")
            }
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache related to marathon context listener correctly closed")
              case Failure(e) =>
                log.error(s"Node Cache related to marathon context listener was not correctly closed", e)
            }
            launcherActor.foreach(actor => akkaContext.stop(actor))
          }
        }
      }
    })
  }

  def addClusterContextListener(workflowId: String,
                                workflowName: String,
                                killUrl: String,
                                launcherActor: Option[ActorRef] = None,
                                akkaContext: Option[ActorContext] = None): Unit = {
    log.info(s"Cluster context listener added to $workflowName with id: $workflowId")
    addWorkflowStatusListener(workflowId, (workflowStatus: WorkflowStatus, nodeCache: NodeCache) => {
      synchronized {
        if (workflowStatus.status == Stopping || workflowStatus.status == Failed) {
          log.info("Stop message received from Zookeeper")
          try {
            workflowStatus.applicationId match {
              case Some(applicationId) =>
                Try {
                  log.info(s"Killing application ($applicationId) with Spark Submissions API in url: $killUrl")
                  val post = new HttpPost(s"$killUrl/$applicationId")
                  val postResponse = HttpClientBuilder.create().build().execute(post)

                  read[SubmissionResponse](Source.fromInputStream(postResponse.getEntity.getContent).mkString)
                } match {
                  case Success(submissionResponse) if submissionResponse.success =>
                    val information = s"Sparta cluster job killed correctly  with Spark API"
                    val newStatus = if (workflowStatus.status == Failed) NotDefined else Killed
                    log.info(information)
                    statusService.update(WorkflowStatus(
                      id = workflowId,
                      status = newStatus,
                      statusInfo = Some(information)
                    ))
                  case Success(submissionResponse) =>
                    log.debug(s"Failed response: $submissionResponse")
                    val information = s"Error while stopping task"
                    log.info(information)
                    statusService.update(WorkflowStatus(
                      id = workflowId,
                      status = Failed,
                      statusInfo = Some(information),
                      lastError = submissionResponse.message.map(error => WorkflowError(information, PhaseEnum.Execution, error))
                    ))
                  case Failure(e) =>
                    val error = "Impossible to parse submission killing response"
                    log.error(error, e)
                    statusService.update(WorkflowStatus(
                      id = workflowId,
                      status = Failed,
                      statusInfo = Some(error),
                      lastError = Option(WorkflowError(error, PhaseEnum.Execution, e.toString))
                    ))
                }
              case None =>
                log.info(s"The Sparta System does not have a submission id associated to workflow $workflowName")
            }
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Node cache to cluster context listener correctly closed")
              case Failure(e) =>
                log.error(s"Node Cache to cluster context listener was not correctly closed", e)
            }
            akkaContext.foreach(context => launcherActor.foreach(actor => context.stop(actor)))
          }
        }
      }
    })
  }

  def addClientContextListener(workflowId: String,
                               workflowName: String,
                               handler: SparkAppHandle,
                               launcherActor: ActorRef,
                               akkaContext: ActorContext): Unit = {
    log.info(s"Client context Listener added to $workflowName with id: $workflowId")
    addWorkflowStatusListener(workflowId, (workflowStatus: WorkflowStatus, nodeCache: NodeCache) => {
      synchronized {
        if (workflowStatus.status == Stopping || workflowStatus.status == Failed) {
          log.info("Stop message received from Zookeeper")
          try {
            workflowStatus.applicationId match {
              case Some(_) =>
                Try {
                  log.info("Stopping submission workflow with handler")
                  handler.stop()
                } match {
                  case Success(_) =>
                    val information = s"Sparta cluster job correctly stopped with Spark Handler"
                    log.info(information)
                    statusService.update(WorkflowStatus(
                      id = workflowId,
                      status = Stopped,
                      statusInfo = Some(information)
                    ))
                  case Failure(e: Exception) =>
                    val error = s"An error was encountered while stopping Sparta cluster job with Spark Handler, killing it ..."
                    log.warn(error, e)
                    Try(handler.kill()) match {
                      case Success(_) =>
                        val information = s"Sparta cluster job killed with Spark Handler"
                        log.info(information)
                        statusService.update(WorkflowStatus(
                          id = workflowId,
                          status = Stopped,
                          statusInfo = Some(information)
                        ))
                      case Failure(e: Exception) =>
                        val error = s"Problems encountered while killing Sparta cluster job with Spark Handler"
                        log.info(error)
                        statusService.update(WorkflowStatus(
                          id = workflowId,
                          status = Stopped,
                          statusInfo = Some(error),
                          lastError = Option(WorkflowError(error, PhaseEnum.Execution, e.toString))
                        ))
                    }
                }
              case None =>
                val information = s"The Sparta System does not have a submission id associated to workflow $workflowName"
                log.info(information)
                statusService.update(WorkflowStatus(
                  id = workflowId,
                  status = Failed,
                  statusInfo = Some(information)))
            }
          } finally {
            Try(nodeCache.close()) match {
              case Success(_) =>
                log.info("Client context listener node cache correctly closed")
              case Failure(e) =>
                log.error(s"Client context listener node cache was not correctly closed", e)
            }
            akkaContext.stop(launcherActor)
          }
        }
      }
    })
  }

  //scalastyle:on

}
