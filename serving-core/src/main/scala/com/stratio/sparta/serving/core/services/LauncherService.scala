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
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import com.typesafe.config.Config
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class LauncherService(curatorFramework: CuratorFramework) extends SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)
  
  def checkWorkflowStatus(workflow: Workflow, launcherActor: ActorRef, akkaContext: ActorContext): Unit = {
    statusService.findById(workflow.id.get) match {
      case Success(workflowStatus) =>
        if (workflowStatus.status == Launched || workflowStatus.status == Starting ||
          workflowStatus.status == Uploaded || workflowStatus.status == Stopping ||
          workflowStatus.status == NotStarted) {
          val information = s"CHECKER: the workflow did not start/stop correctly"
          log.error(information)
          statusService.update(WorkflowStatus(
            id = workflow.id.get,
            status = Failed,
            statusInfo = Some(information)
          ))
          akkaContext.stop(launcherActor)
        } else {
          val information = s"CHECKER: the workflow started/stopped correctly"
          log.info(information)
          statusService.update(WorkflowStatus(id = workflow.id.get,
            status = NotDefined,
            statusInfo = Some(information)
          ))
        }
      case Failure(exception) =>
        log.error(s"Error when extracting workflow status in the scheduled task", exception)
    }
  }
  
  def loggingResponseWorkflowStatus(response: Try[WorkflowStatus]): Unit =
    response match {
      case Success(statusModel) =>
        log.info(s"Workflow status model created or updated correctly: " +
          s"\n\tId: ${statusModel.id}\n\tStatus: ${statusModel.status}")
      case Failure(e) =>
        log.error(s"An error was encountered while creating the Workflow status model. " +
          s"Error: ${e.getLocalizedMessage}", e)
    }

  def getZookeeperConfig: Config = SpartaConfig.getZookeeperConfig.getOrElse {
    val message = "Impossible to extract Zookeeper Configuration"
    log.error(message)
    throw new RuntimeException(message)
  }
}
