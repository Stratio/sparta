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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import com.typesafe.config.Config
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success}

class LauncherService(curatorFramework: CuratorFramework) extends SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)

  //scalastyle:off
  def checkWorkflowStatus(workflow: Workflow): Unit = {
    statusService.findById(workflow.id.get) match {
      case Success(workflowStatus) =>
        val wrongStartStates = Seq(Launched, Starting, Uploaded, NotStarted)
        val validStartStates = Seq(Started)
        val wrongStopStates = Seq(Stopping)
        val validStopStates = Seq(Stopped, Failed, Killed, Finished)

        workflowStatus.status match {
          case status if wrongStartStates.contains(status) =>
            val information = s"Checker: the workflow ${workflow.name} did not start correctly"
            log.error(information)
            statusService.update(WorkflowStatus(id = workflow.id.get, status = Failed, statusInfo = Some(information)))
          case status if wrongStopStates.contains(status) =>
            val information = s"Checker: the workflow ${workflow.name} did not stop correctly"
            log.error(information)
            statusService.update(WorkflowStatus(id = workflow.id.get, status = Failed, statusInfo = Some(information)))
          case status if validStartStates.contains(status) =>
            val information = s"Checker: the workflow ${workflow.name} started correctly"
            log.info(information)
            statusService.update(WorkflowStatus(id = workflow.id.get, status = NotDefined, statusInfo = Some(information)))
          case status if validStopStates.contains(status) =>
            val information = s"Checker: the workflow ${workflow.name} stopped correctly"
            log.info(information)
            statusService.update(WorkflowStatus(id = workflow.id.get, status = NotDefined, statusInfo = Some(information)))
          case _ =>
            val information = s"Checker: the workflow ${workflow.name} has invalid state ${workflowStatus.status}"
            log.info(information)
            statusService.update(WorkflowStatus(id = workflow.id.get, status = NotDefined, statusInfo = Some(information)))
        }
      case Failure(exception) =>
        log.error(s"Error when extracting workflow status in the scheduled task", exception)
    }
  }

  //scalastyle:on

  def getZookeeperConfig: Config = SpartaConfig.getZookeeperConfig.getOrElse {
    val message = "Impossible to extract Zookeeper Configuration"
    log.error(message)
    throw new RuntimeException(message)
  }
}
