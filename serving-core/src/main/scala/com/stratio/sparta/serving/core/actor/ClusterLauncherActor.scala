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

import java.io.File

import akka.actor.{Actor, ActorRef, Cancellable}
import com.stratio.sparta.serving.core.actor.LauncherActor.{Start, StartWithRequest}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.utils.SchedulerUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.launcher.SpartaLauncher

import scala.util.{Failure, Success, Try}

class ClusterLauncherActor(val curatorFramework: CuratorFramework, statusListenerActor: ActorRef) extends Actor
  with SchedulerUtils {

  private val executionService = new ExecutionService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val clusterListenerService = new ListenerService(curatorFramework, statusListenerActor)
  private val launcherService = new LauncherService(curatorFramework)
  private val checkersWorkflowStatus = scala.collection.mutable.ArrayBuffer.empty[Cancellable]

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow) => initializeSubmitRequest(workflow)
    case StartWithRequest(workflow: Workflow, submitRequest: WorkflowExecution) => launch(workflow, submitRequest)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  override def postStop(): Unit = checkersWorkflowStatus.foreach(task => if (!task.isCancelled) task.cancel())

  //scalastyle:off
  def initializeSubmitRequest(workflow: Workflow): Unit = {
    Try {
      log.info(s"Initializing cluster options submitted by workflow: ${workflow.name}")
      val sparkSubmitService = new SparkSubmitService(workflow)
      val detailConfig = SpartaConfig.getDetailConfig.getOrElse {
        val message = "Impossible to extract detail configuration"
        log.error(message)
        throw new RuntimeException(message)
      }
      val zookeeperConfig = launcherService.getZookeeperConfig
      val sparkHome = sparkSubmitService.validateSparkHome
      val driverFile = sparkSubmitService.extractDriverSubmit(detailConfig)
      val pluginJars = sparkSubmitService.userPluginsJars.filter(_.nonEmpty)
      val driverArgs = sparkSubmitService.extractDriverArgs(zookeeperConfig, pluginJars, detailConfig)
      val (sparkSubmitArgs, sparkConfs) = sparkSubmitService.extractSubmitArgsAndSparkConf(pluginJars)
      val executionSubmit = WorkflowExecution(
        id = workflow.id.get,
        sparkSubmitExecution = SparkSubmitExecution(
          driverClass = SpartaDriverClass,
          driverFile = driverFile,
          pluginFiles = pluginJars,
          master = workflow.settings.sparkSettings.master.toString,
          submitArguments = sparkSubmitArgs,
          sparkConfigurations = sparkConfs,
          driverArguments = driverArgs,
          sparkHome = sparkHome
        ),
        sparkDispatcherExecution = None,
        marathonExecution = None
      )
      executionService.create(executionSubmit)
    } match {
      case Failure(exception) =>
        val information = s"An error was encountered while initializing the submit options"
        log.error(information, exception)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
      case Success(Failure(exception)) =>
        val information = s"An error was encountered while creating an execution submit in the persistence"
        log.error(information, exception)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
      case Success(Success(submitRequestCreated)) =>
        val information = "Submit options initialized correctly"
        log.info(information)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = NotStarted,
          statusInfo = Option(information),
          lastExecutionMode = Option(workflow.settings.global.executionMode)
        ))

        launch(workflow, submitRequestCreated)
    }
  }

  def launch(workflow: Workflow, submitRequest: WorkflowExecution): Unit = {
    Try {
      log.info(s"Launching Sparta workflow with options ... \n\t" +
        s"Workflow name: ${workflow.name}\n\t" +
        s"Main Class: $SpartaDriverClass\n\t" +
        s"Driver file: ${submitRequest.sparkSubmitExecution.driverFile}\n\t" +
        s"Master: ${submitRequest.sparkSubmitExecution.master}\n\t" +
        s"Spark submit arguments: ${submitRequest.sparkSubmitExecution.submitArguments.mkString(",")}" +
        s"\n\tSpark configurations: ${submitRequest.sparkSubmitExecution.sparkConfigurations.mkString(",")}\n\t" +
        s"Driver arguments: ${submitRequest.sparkSubmitExecution.driverArguments}")

      val spartaLauncher = new SpartaLauncher()
        .setAppResource(submitRequest.sparkSubmitExecution.driverFile)
        .setMainClass(submitRequest.sparkSubmitExecution.driverClass)
        .setMaster(submitRequest.sparkSubmitExecution.master)

      //Set Spark Home
      spartaLauncher.setSparkHome(submitRequest.sparkSubmitExecution.sparkHome)
      //Spark arguments
      submitRequest.sparkSubmitExecution.submitArguments.filter(_._2.nonEmpty)
        .foreach { case (k: String, v: String) => spartaLauncher.addSparkArg(k, v) }
      submitRequest.sparkSubmitExecution.submitArguments.filter(_._2.isEmpty)
        .foreach { case (k: String, v: String) => spartaLauncher.addSparkArg(k) }
      // Spark properties
      submitRequest.sparkSubmitExecution.sparkConfigurations.filter(_._2.nonEmpty)
        .foreach { case (key: String, value: String) => spartaLauncher.setConf(key.trim, value.trim) }
      // Driver (Sparta) params
      submitRequest.sparkSubmitExecution.driverArguments.toSeq.sortWith { case (a, b) => a._1 < b._1 }
        .foreach { case (_, argValue) => spartaLauncher.addAppArgs(argValue) }
      //Redirect options
      spartaLauncher.redirectError()
      // Launch SparkApp
      spartaLauncher.startApplication(clusterListenerService.addSparkListener(workflow))
    } match {
      case Failure(exception) =>
        val information = s"An error was encountered while launching the workflow"
        log.error(information, exception)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
      case Success(sparkHandler) =>
        val information = "Workflow launched correctly"
        log.info(information)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Launched,
          statusInfo = Option(information)
        ))
        val scheduledTask = scheduleOneTask(AwaitWorkflowChangeStatus, DefaultAwaitWorkflowChangeStatus)(
          launcherService.checkWorkflowStatus(workflow))
        checkersWorkflowStatus += scheduledTask
        if (workflow.settings.global.executionMode.contains(ConfigMesos))
          clusterListenerService.addMesosDispatcherListener(workflow.id.get, Option(scheduledTask))
        if (workflow.settings.global.executionMode.contains(ConfigMarathon))
          clusterListenerService.addSparkClientListener(workflow.id.get, sparkHandler, scheduledTask)

    }
  }
}