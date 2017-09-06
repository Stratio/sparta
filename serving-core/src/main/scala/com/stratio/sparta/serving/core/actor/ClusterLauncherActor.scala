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

package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, Cancellable, PoisonPill}
import com.stratio.sparta.serving.core.actor.LauncherActor.{Start, StartWithRequest}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowError, WorkflowExecution, Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.services.ClusterCheckerService
import com.stratio.sparta.serving.core.utils.{ClusterListenerUtils, LauncherUtils, RequestUtils, SchedulerUtils, SparkSubmitService, WorkflowStatusUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.launcher.SparkLauncher

import scala.util.{Failure, Success, Try}

class ClusterLauncherActor(val curatorFramework: CuratorFramework) extends Actor
  with SchedulerUtils with WorkflowStatusUtils
  with ClusterListenerUtils with LauncherUtils with RequestUtils {

  private val clusterCheckerService = new ClusterCheckerService(curatorFramework)
  private val checkersPolicyStatus = scala.collection.mutable.ArrayBuffer.empty[Cancellable]

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow) => initializeSubmitRequest(workflow)
    case StartWithRequest(policy: Workflow, submitRequest: WorkflowExecution) => launch(policy, submitRequest)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  override def postStop(): Unit = checkersPolicyStatus.foreach(_.cancel())

  def initializeSubmitRequest(workflow: Workflow): Unit = {
    Try {
      log.info(s"Initializing cluster options submitted by policy: ${workflow.name}")
      val sparkSubmitService = new SparkSubmitService(workflow)
      val detailConfig = SpartaConfig.getDetailConfig.getOrElse {
        val message = "Impossible to extract Detail Configuration"
        log.error(message)
        throw new RuntimeException(message)
      }
      val zookeeperConfig = getZookeeperConfig
      val sparkHome = sparkSubmitService.validateSparkHome
      val driverFile = sparkSubmitService.extractDriverSubmit
      val pluginsFiles = sparkSubmitService.userPluginsJars
      val driverArgs = sparkSubmitService.extractDriverArgs(zookeeperConfig, pluginsFiles, detailConfig)
      val (sparkSubmitArgs, sparkConfs) = sparkSubmitService.extractSubmitArgsAndSparkConf(pluginsFiles)
      val submitRequest = WorkflowExecution(
        workflow.id.get,
        SpartaDriverClass,
        driverFile,
        workflow.settings.sparkSettings.master,
        sparkSubmitArgs,
        sparkConfs,
        driverArgs,
        workflow.settings.global.executionMode,
        workflow.settings.sparkSettings.killUrl.getOrElse(DefaultkillUrl),
        Option(sparkHome)
      )
      createRequest(submitRequest)
    } match {
      case Failure(exception) =>
        val information = s"An error was encountered while initializing the Sparta submit options"
        log.error(information, exception)
        updateStatus(WorkflowStatus(id = workflow.id.get, status = Failed, statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))))
        self ! PoisonPill
      case Success(Failure(exception)) =>
        val information = s"An error was encountered while creating a submit request in the persistence"
        log.error(information, exception)
        updateStatus(WorkflowStatus(id = workflow.id.get, status = Failed, statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
        self ! PoisonPill
      case Success(Success(submitRequestCreated)) =>
        val information = "Sparta submit options initialized correctly"
        log.info(information)
        updateStatus(WorkflowStatus(id = workflow.id.get, status = NotStarted,
          statusInfo = Option(information), lastExecutionMode = Option(submitRequestCreated.executionMode)))

        launch(workflow, submitRequestCreated)
    }
  }

  def launch(policy: Workflow, submitRequest: WorkflowExecution): Unit = {
    Try {
      log.info(s"Launching Sparta job with options ... \n\tPolicy name: ${policy.name}\n\t" +
        s"Main Class: $SpartaDriverClass\n\tDriver file: ${submitRequest.driverFile}\n\t" +
        s"Master: ${submitRequest.master}\n\tSpark submit arguments: ${submitRequest.submitArguments.mkString(",")}" +
        s"\n\tSpark configurations: ${submitRequest.sparkConfigurations.mkString(",")}\n\t" +
        s"Driver arguments: ${submitRequest.driverArguments}")
      val sparkLauncher = new SparkLauncher()
        .setAppResource(submitRequest.driverFile)
        .setMainClass(submitRequest.driverClass)
        .setMaster(submitRequest.master)

      //Set Spark Home
      submitRequest.sparkHome.foreach(home => sparkLauncher.setSparkHome(home))
      //Spark arguments
      submitRequest.submitArguments.filter(_._2.nonEmpty)
        .foreach { case (k: String, v: String) => sparkLauncher.addSparkArg(k, v) }
      submitRequest.submitArguments.filter(_._2.isEmpty)
        .foreach { case (k: String, v: String) => sparkLauncher.addSparkArg(k) }
      // Spark properties
      submitRequest.sparkConfigurations.filter(_._2.nonEmpty)
        .foreach { case (key: String, value: String) => sparkLauncher.setConf(key.trim, value.trim) }
      // Driver (Sparta) params
      submitRequest.driverArguments.toSeq.sortWith { case (a, b) => a._1 < b._1 }
        .foreach { case (_, argValue) => sparkLauncher.addAppArgs(argValue) }
      //Redirect Log
      sparkLauncher.redirectError()
      // Launch SparkApp
      sparkLauncher.startApplication(addSparkListener(policy))
    } match {
      case Failure(exception) =>
        val information = s"An error was encountered while launching the Sparta cluster job"
        log.error(information, exception)
        updateStatus(WorkflowStatus(id = policy.id.get, status = Failed, statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
        self ! PoisonPill
      case Success(sparkHandler) =>
        val information = "Sparta cluster job launched correctly"
        log.info(information)
        updateStatus(WorkflowStatus(id = policy.id.get, status = Launched,
          submissionId = Option(sparkHandler.getAppId), submissionStatus = Option(sparkHandler.getState.name()),
          statusInfo = Option(information)))
        if (submitRequest.executionMode.contains(ConfigMesos))
          addClusterContextListener(policy.id.get, policy.name, submitRequest.killUrl, Option(self), Option(context))
        else addClientContextListener(policy.id.get, policy.name, sparkHandler, self, context)
        checkersPolicyStatus += scheduleOneTask(AwaitPolicyChangeStatus, DefaultAwaitPolicyChangeStatus)(
          clusterCheckerService.checkPolicyStatus(policy, self, context))
    }
  }
}