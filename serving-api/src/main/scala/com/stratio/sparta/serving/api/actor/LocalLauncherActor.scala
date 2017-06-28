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
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.actor.StatusActor.ResponseStatus
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.{JarsHelper, ResourceManagerLinkHelper}
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowErrorModel, WorkflowModel, WorkflowStatusModel}
import com.stratio.sparta.serving.core.utils.{LauncherUtils, PolicyStatusUtils, SparkSubmitService}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class LocalLauncherActor(streamingContextService: StreamingContextService, val curatorFramework: CuratorFramework)
  extends Actor with LauncherUtils with PolicyStatusUtils {

  override def receive: PartialFunction[Any, Unit] = {
    case Start(policy: WorkflowModel) => doInitSpartaContext(policy)
    case ResponseStatus(status) => loggingResponseWorkflowStatus(status)
    case _ => log.info("Unrecognized message in Local Launcher Actor")
  }

  private def doInitSpartaContext(workflow: WorkflowModel): Unit = {
    val sparkSubmitService = new SparkSubmitService(workflow)
    val jars = sparkSubmitService.userPluginsFiles

    jars.foreach(file => JarsHelper.addToClasspath(file))
    Try {
      val startingInfo = s"Starting a Sparta local job for the workflow"
      log.info(startingInfo)
      updateStatus(WorkflowStatusModel(
        id = workflow.id.get,
        status = PolicyStatusEnum.NotStarted,
        statusInfo = Some(startingInfo),
        lastExecutionMode = Option(AppConstant.ConfigLocal)
      ))
      val (spartaWorkflow, ssc) = streamingContextService.localStreamingContext(workflow, jars)
      spartaWorkflow.setup()
      ssc.start()
      val startedInformation = s"Sparta local job was started correctly"
      log.info(startedInformation)
      updateStatus(WorkflowStatusModel(
        id = workflow.id.get,
        status = PolicyStatusEnum.Started,
        statusInfo = Some(startedInformation),
        resourceManagerUrl = ResourceManagerLinkHelper.getLink(
          workflow.settings.global.executionMode,
          workflow.settings.sparkSettings.master,
          workflow.settings.global.monitoringLink
        )
      ))
      ssc.awaitTermination()
      spartaWorkflow.cleanUp()
    } match {
      case Success(_) =>
        val information = s"Sparta local job stopped correctly"
        log.info(information)
        updateStatus(WorkflowStatusModel(
          id = workflow.id.get, status = PolicyStatusEnum.Stopped, statusInfo = Some(information)))
        self ! PoisonPill
      case Failure(exception) =>
        val information = s"Error initiating the Sparta local job"
        log.error(information, exception)
        updateStatus(WorkflowStatusModel(
          id = workflow.id.get,
          status = PolicyStatusEnum.Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowErrorModel(information, PhaseEnum.Execution, exception.toString))
        ))
        SparkContextFactory.destroySparkContext()
        self ! PoisonPill
    }
  }
}
