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

package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, PoisonPill}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.{JarsHelper, ResourceManagerLinkHelper}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, Workflow, WorkflowError, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{SparkSubmitService, WorkflowStatusService}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class LocalLauncherActor(streamingContextService: StreamingContextService, val curatorFramework: CuratorFramework)
  extends Actor with SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow) => doInitSpartaContext(workflow)
    case _ => log.info("Unrecognized message in Local Launcher Actor")
  }

  //scalastyle:off
  private def doInitSpartaContext(workflow: Workflow): Unit = {
    val sparkSubmitService = new SparkSubmitService(workflow)
    val jars = sparkSubmitService.userPluginsFiles

    jars.foreach(file => JarsHelper.addToClasspath(file))

    Try {
      val startingInfo = s"Starting a Sparta local job for the workflow"
      log.info(startingInfo)
      statusService.update(WorkflowStatus(
        id = workflow.id.get,
        status = WorkflowStatusEnum.NotStarted,
        statusInfo = Some(startingInfo),
        lastExecutionMode = Option(AppConstant.ConfigLocal)
      ))
      val (spartaWorkflow, ssc) = streamingContextService.localStreamingContext(workflow, jars)
      spartaWorkflow.setup()
      ssc.start()
      val startedInformation = s"Sparta local job was started correctly"
      log.info(startedInformation)
      statusService.update(WorkflowStatus(
        id = workflow.id.get,
        status = WorkflowStatusEnum.Started,
        statusInfo = Some(startedInformation),
        sparkUi = ResourceManagerLinkHelper.getLink(
          workflow.settings.global.executionMode,
          workflow.settings.sparkSettings.master,
          workflow.settings.global.monitoringLink
        )
      ))
      ssc.awaitTermination()
      spartaWorkflow.cleanUp()
    } match {
      case Success(_) =>
        SparkContextFactory.destroySparkContext()
        val information = s"Sparta local job stopped correctly"
        log.info(information)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = WorkflowStatusEnum.Stopped,
          statusInfo = Some(information)
        ))
        self ! PoisonPill
      case Failure(exception) =>
        val information = s"Error initiating the Sparta local job"
        log.error(information, exception)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = WorkflowStatusEnum.Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
        SparkContextFactory.destroySparkContext()
        self ! PoisonPill
    }
  }
}
