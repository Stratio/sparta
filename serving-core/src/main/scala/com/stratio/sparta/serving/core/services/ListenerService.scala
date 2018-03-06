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

import akka.actor.{ActorContext, ActorRef, Cancellable}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.StatusListenerActor.{ForgetWorkflowStatusActions, OnWorkflowStatusChangeDo}
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmissionResponse
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.curator.framework.CuratorFramework
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.spark.launcher.SparkAppHandle
import org.json4s.jackson.Serialization._

import scala.io.Source
import scala.util.{Failure, Success, Try}

class ListenerService(curatorFramework: CuratorFramework, statusListenerActor: ActorRef) extends SpartaSerializer
  with SLF4JLogging {

  private val workflowService = new WorkflowService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)

  //scalastyle:off
  def addSparkClientListener(workflowId: String, handler: SparkAppHandle): Unit = {
    val workflow = workflowService.findById(workflowId)
    log.info(s"Spark Client listener added to ${workflow.name} with id: $workflowId")
    statusListenerActor ! OnWorkflowStatusChangeDo(workflowId) { workflowStatusStream =>
      if (workflowStatusStream.workflowStatus.status == Stopping || workflowStatusStream.workflowStatus.status == Failed) {
        log.info("Stop message received from Zookeeper")
        try {
          Try {
            log.info("Stopping submission workflow with handler")
            handler.stop()
          } match {
            case Success(_) =>
              val information = s"Workflow correctly stopped with Spark Handler"
              log.info(information)
              statusService.update(WorkflowStatus(
                id = workflowId,
                status = if (workflowStatusStream.workflowStatus.status == Stopping) Stopped else Failed,
                statusInfo = Some(information)
              ))
            case Failure(e: Exception) =>
              val error = s"An error was encountered while stopping workflow with Spark Handler, killing it ..."
              log.warn(s"$error. With exception: ${e.getLocalizedMessage}")
              Try(handler.kill()) match {
                case Success(_) =>
                  val information = s"Workflow killed with Spark Handler"
                  log.info(information)
                  statusService.update(WorkflowStatus(
                    id = workflowId,
                    status = if (workflowStatusStream.workflowStatus.status == Stopping) Stopped else Failed,
                    statusInfo = Some(information)
                  ))
                case Failure(e: Exception) =>
                  val error = s"Problems encountered while killing workflow with Spark Handler"
                  log.warn(error)
                  statusService.update(WorkflowStatus(
                    id = workflowId,
                    status = Failed,
                    statusInfo = Some(error),
                    lastError = Option(WorkflowError(error, PhaseEnum.Stop, e.toString))
                  ))
              }
          }
        } finally {
          statusListenerActor ! ForgetWorkflowStatusActions(workflowId)
        }
      }
    }
  }

  //scalastyle:on

}
