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
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowErrorModel, WorkflowModel, WorkflowStatusModel}
import com.stratio.sparta.serving.core.services.ClusterCheckerService
import com.stratio.sparta.serving.core.utils._
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class MarathonLauncherActor(val curatorFramework: CuratorFramework) extends Actor
  with LauncherUtils with SchedulerUtils with ClusterListenerUtils
  with PolicyStatusUtils with RequestUtils {

  private val clusterCheckerService = new ClusterCheckerService(curatorFramework)
  private val checkersPolicyStatus = scala.collection.mutable.ArrayBuffer.empty[Cancellable]

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: WorkflowModel) => initializeSubmitRequest(workflow)
    case ResponseStatus(status) => loggingResponseWorkflowStatus(status)
    case _ => log.info("Unrecognized message in Marathon Launcher Actor")
  }

  override def postStop(): Unit = checkersPolicyStatus.foreach(_.cancel())

  def initializeSubmitRequest(workflow: WorkflowModel): Unit = {
    Try {
      val sparkSubmitService = new SparkSubmitService(workflow)
      log.info(s"Initializing options to submit the Marathon application associated to policy: ${workflow.name}")
      val detailConfig = SpartaConfig.getDetailConfig.getOrElse {
        val message = "Impossible to extract Detail Configuration"
        log.error(message)
        throw new RuntimeException(message)
      }
      val zookeeperConfig = getZookeeperConfig
      val driverFile = sparkSubmitService.extractDriverSubmit
      val pluginsFiles = sparkSubmitService.userPluginsJars
      val driverArgs = sparkSubmitService.extractDriverArgs(zookeeperConfig, pluginsFiles, detailConfig)
      val (sparkSubmitArgs, sparkConfs) = sparkSubmitService.extractSubmitArgsAndSparkConf(pluginsFiles)
      val submitRequest = SubmitRequest(
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

      createRequest(submitRequest).getOrElse(throw new Exception("Unable to create a submit request in persistence"))

      new MarathonService(context, curatorFramework, workflow, submitRequest)
    } match {
      case Failure(exception) =>
        val information = s"Error 0 initializing Sparta Marathon App options"
        log.error(information, exception)
        updateStatus(WorkflowStatusModel(id = workflow.id.get, status = Failed, statusInfo = Option(information),
          lastError = Option(WorkflowErrorModel(information, PhaseEnum.Execution, exception.toString))
        ))
        self ! PoisonPill
      case Success(marathonApp) =>
        val information = "Sparta Marathon App configuration initialized correctly"
        log.info(information)
        updateStatus(WorkflowStatusModel(id = workflow.id.get, status = NotStarted,
          statusInfo = Option(information), lastExecutionMode = Option(workflow.settings.global.executionMode)))
        marathonApp.launch()
        addMarathonContextListener(workflow.id.get, workflow.name, context, Option(self))
        checkersPolicyStatus += scheduleOneTask(AwaitPolicyChangeStatus, DefaultAwaitPolicyChangeStatus)(
          clusterCheckerService.checkPolicyStatus(workflow, self, context))
    }
  }
}