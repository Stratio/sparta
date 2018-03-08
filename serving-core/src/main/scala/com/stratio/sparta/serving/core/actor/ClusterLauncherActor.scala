/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, ActorRef}
import com.stratio.sparta.serving.core.actor.LauncherActor.{Start, StartWithRequest}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.helpers.JarsHelper
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
  private val listenerService = new ListenerService(curatorFramework, statusListenerActor)

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow) => initializeSubmitRequest(workflow)
    case StartWithRequest(workflow: Workflow, submitRequest: WorkflowExecution) => launch(workflow, submitRequest)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

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
      val zookeeperConfig = SpartaConfig.getZookeeperConfig.getOrElse {
        val message = "Impossible to extract Zookeeper Configuration"
        log.error(message)
        throw new RuntimeException(message)
      }
      val sparkHome = sparkSubmitService.validateSparkHome
      val driverFile = sparkSubmitService.extractDriverSubmit(detailConfig)
      val pluginJars = sparkSubmitService.userPluginsJars.filter(_.nonEmpty)
      val localPluginJars = JarsHelper.getLocalPathFromJars(pluginJars)
      val driverArgs = sparkSubmitService.extractDriverArgs(zookeeperConfig, pluginJars, detailConfig)
      val (sparkSubmitArgs, sparkConfs) = sparkSubmitService.extractSubmitArgsAndSparkConf(localPluginJars)
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
          lastError = Option(WorkflowError(information, PhaseEnum.Launch, exception.toString))
        ))
      case Success(Failure(exception)) =>
        val information = s"An error was encountered while creating an execution submit in the persistence"
        log.error(information, exception)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Launch, exception.toString))
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
        s"Spark submit arguments: ${submitRequest.sparkSubmitExecution.submitArguments.mkString(",")}\n\t" +
        s"Spark configurations: ${submitRequest.sparkSubmitExecution.sparkConfigurations.mkString(",")}\n\t" +
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
      spartaLauncher.startApplication()
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
        if (workflow.settings.global.executionMode.contains(ConfigMarathon))
          listenerService.addSparkClientListener(workflow.id.get, sparkHandler)
    }
  }
}