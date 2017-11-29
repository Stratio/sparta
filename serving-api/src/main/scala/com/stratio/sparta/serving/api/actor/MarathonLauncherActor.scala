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

import akka.actor.{Actor, ActorRef, Cancellable, PoisonPill}
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.helpers.{ResourceManagerLinkHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.utils._
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Properties, Success, Try}

class MarathonLauncherActor(val curatorFramework: CuratorFramework, statusListenerActor: ActorRef) extends Actor
  with SchedulerUtils {

  private val executionService = new ExecutionService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val launcherService = new LauncherService(curatorFramework)
  private val clusterListenerService = new ListenerService(curatorFramework, statusListenerActor)
  private val checkersPolicyStatus = scala.collection.mutable.ArrayBuffer.empty[Cancellable]

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow) => initializeSubmitRequest(workflow)
    case _ => log.info("Unrecognized message in Marathon Launcher Actor")
  }

  override def postStop(): Unit = checkersPolicyStatus.foreach(_.cancel())

  //scalastyle:off
  def initializeSubmitRequest(workflow: Workflow): Unit = {
    Try {
      val sparkSubmitService = new SparkSubmitService(workflow)
      log.info(s"Initializing options to submit the Workflow App associated to workflow: ${workflow.name}")
      val detailConfig = SpartaConfig.getDetailConfig.getOrElse {
        val message = "Impossible to extract detail configuration"
        log.error(message)
        throw new RuntimeException(message)
      }
      val zookeeperConfig = launcherService.getZookeeperConfig
      val driverFile = sparkSubmitService.extractDriverSubmit(detailConfig)
      val pluginJars = sparkSubmitService.userPluginsJars
      val sparkHome = sparkSubmitService.validateSparkHome
      val driverArgs = sparkSubmitService.extractDriverArgs(zookeeperConfig, pluginJars, detailConfig)
      val (sparkSubmitArgs, sparkConfs) = sparkSubmitService.extractSubmitArgsAndSparkConf
      val executionSubmit = WorkflowExecution(
        id = workflow.id.get,
        sparkSubmitExecution = SparkSubmitExecution(
          driverClass = SpartaDriverClass,
          driverFile = driverFile,
          pluginFiles = pluginJars,
          master = workflow.settings.sparkSettings.master,
          submitArguments = sparkSubmitArgs,
          sparkConfigurations = sparkConfs,
          driverArguments = driverArgs,
          sparkHome = sparkHome
        ),
        sparkDispatcherExecution = None,
        marathonExecution = Option(MarathonExecution(marathonId = WorkflowHelper.getMarathonId(workflow)))
      )

      executionService.create(executionSubmit).getOrElse(
        throw new Exception("Unable to create an execution submit in Zookeeper"))
      new MarathonService(context, curatorFramework, workflow, executionSubmit)
    } match {
      case Failure(exception) =>
        val information = s"Error initializing Workflow App"
        log.error(information, exception)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed,
          statusInfo = Option(information),
          lastExecutionMode = Option(workflow.settings.global.executionMode),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
        self ! PoisonPill
      case Success(marathonApp) =>
        val information = "Workflow App configuration initialized correctly"
        log.info(information)
        val lastExecutionMode = Option(workflow.settings.global.executionMode)
        statusService.update(WorkflowStatus(
          workflow.id.get,
          status = NotStarted,
          lastExecutionMode = lastExecutionMode))
        marathonApp.launch()
        clusterListenerService.addMarathonListener(workflow.id.get, context)
        checkersPolicyStatus += scheduleOneTask(AwaitWorkflowChangeStatus, DefaultAwaitWorkflowChangeStatus)(
          launcherService.checkWorkflowStatus(workflow))
    }
  }
}