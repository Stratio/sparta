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

import akka.actor.{Actor, Cancellable, PoisonPill}
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.actor.StatusActor.ResponseStatus
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.helpers.WorkflowHelper
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.utils._
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class MarathonLauncherActor(val curatorFramework: CuratorFramework) extends Actor
  with SchedulerUtils{

  private val executionService = new ExecutionService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val launcherService = new LauncherService(curatorFramework)
  private val clusterListenerService = new ListenerService(curatorFramework)
  private val checkersPolicyStatus = scala.collection.mutable.ArrayBuffer.empty[Cancellable]

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow) => initializeSubmitRequest(workflow)
    case ResponseStatus(status) => launcherService.loggingResponseWorkflowStatus(status)
    case _ => log.info("Unrecognized message in Marathon Launcher Actor")
  }

  override def postStop(): Unit = checkersPolicyStatus.foreach(_.cancel())

  def initializeSubmitRequest(workflow: Workflow): Unit = {
    Try {
      val sparkSubmitService = new SparkSubmitService(workflow)
      log.info(s"Initializing options to submit the Workflow App associated to workflow: ${workflow.name}")
      val detailConfig = SpartaConfig.getDetailConfig.getOrElse {
        val message = "Impossible to extract Detail Configuration"
        log.error(message)
        throw new RuntimeException(message)
      }
      val zookeeperConfig = launcherService.getZookeeperConfig
      val driverFile = sparkSubmitService.extractDriverSubmit
      val pluginsFiles = sparkSubmitService.userPluginsJars
      val driverArgs = sparkSubmitService.extractDriverArgs(zookeeperConfig, pluginsFiles, detailConfig)
      val (sparkSubmitArgs, sparkConfs) = sparkSubmitService.extractSubmitArgsAndSparkConf(pluginsFiles)
      val executionSubmit = WorkflowExecution(
        workflow.id.get,
        SpartaDriverClass,
        driverFile,
        workflow.settings.sparkSettings.master,
        sparkSubmitArgs,
        sparkConfs,
        driverArgs,
        workflow.settings.global.executionMode,
        workflow.settings.sparkSettings.killUrl.getOrElse(DefaultkillUrl)
      )

      executionService.create(executionSubmit).getOrElse(
        throw new Exception("Unable to create an execution submit in Zookeeper"))

      new MarathonService(context, curatorFramework, workflow, executionSubmit)
    } match {
      case Failure(exception) =>
        val information = s"Error initializing Workflow App"
        log.error(information, exception)
        statusService.update(WorkflowStatus(id = workflow.id.get, status = Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
        self ! PoisonPill
      case Success(marathonApp) =>
        val information = "Workflow App configuration initialized correctly"
        log.info(information)
        statusService.update(WorkflowStatus(id = workflow.id.get, status = NotStarted,
          marathonId = Option(WorkflowHelper.getMarathonId(workflow)), statusInfo = Option(information),
          lastExecutionMode = Option(workflow.settings.global.executionMode)))
        marathonApp.launch()
        clusterListenerService.addMarathonContextListener(workflow.id.get, workflow.name, context, Option(self))
        checkersPolicyStatus += scheduleOneTask(AwaitWorkflowChangeStatus, DefaultAwaitWorkflowChangeStatus)(
          launcherService.checkWorkflowStatus(workflow, self, context))
    }
  }
}