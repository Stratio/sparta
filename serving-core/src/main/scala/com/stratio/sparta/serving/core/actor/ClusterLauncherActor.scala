/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.serving.core.actor.LauncherActor.{Start, StartWithRequest}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.utils.SchedulerUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.launcher.SpartaLauncher
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

class ClusterLauncherActor(val curatorFramework: CuratorFramework, statusListenerActor: ActorRef) extends Actor
  with SchedulerUtils {

  private val executionService = new ExecutionService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val listenerService = new ListenerService(curatorFramework, statusListenerActor)

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow, userId: Option[String]) => initializeSubmitRequest(workflow, userId: Option[String])
    case StartWithRequest(workflow: Workflow, submitRequest: WorkflowExecution) => launch(workflow, submitRequest)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  //scalastyle:off
  def initializeSubmitRequest(workflow: Workflow, userId: Option[String]): Unit = {
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
        genericDataExecution = Option(GenericDataExecution(
          workflow = workflow,
          executionMode = dispatcher,
          executionId = UUID.randomUUID.toString
        )),
        sparkSubmitExecution = Option(SparkSubmitExecution(
          driverClass = SpartaDriverClass,
          driverFile = driverFile,
          pluginFiles = pluginJars,
          master = workflow.settings.sparkSettings.master.toString,
          submitArguments = sparkSubmitArgs,
          sparkConfigurations = sparkConfs,
          driverArguments = driverArgs,
          sparkHome = sparkHome
        )),
        sparkDispatcherExecution = None,
        marathonExecution = None
      )
      executionService.create(executionSubmit)
    } match {
      case Failure(exception) =>
        val information = s"An error was encountered while initializing the submit options"
        log.error(information, exception)
        val error = WorkflowError(information, PhaseEnum.Launch, exception.toString)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          statusId = UUID.randomUUID.toString,
          status = Failed,
          statusInfo = Option(information)
        ))
        executionService.setLastError(workflow.id.get, error)
      case Success(Failure(exception)) =>
        val information = s"An error was encountered while creating an execution submit in the persistence"
        log.error(information, exception)
        val error = WorkflowError(information, PhaseEnum.Launch, exception.toString)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          statusId = UUID.randomUUID.toString,
          status = Failed,
          statusInfo = Option(information)
        ))
        executionService.setLastError(workflow.id.get, error)
      case Success(Success(submitRequestCreated)) =>
        val information = "Submit options initialized correctly"
        log.info(information)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          statusId = UUID.randomUUID.toString,
          status = NotStarted,
          statusInfo = Option(information)
        ))
        executionService.setLaunchDate(workflow.id.get, new DateTime())

        launch(workflow, submitRequestCreated)
    }
  }

  def launch(workflow: Workflow, submitRequest: WorkflowExecution): Unit = {
    Try {
      val submitExecution = submitRequest.sparkSubmitExecution.get
      log.info(s"Launching Sparta workflow with options ... \n\t" +
        s"Workflow name: ${workflow.name}\n\t" +
        s"Main Class: $SpartaDriverClass\n\t" +
        s"Driver file: ${submitExecution.driverFile}\n\t" +
        s"Master: ${submitExecution.master}\n\t" +
        s"Spark submit arguments: ${submitExecution.submitArguments.mkString(",")}\n\t" +
        s"Spark configurations: ${submitExecution.sparkConfigurations.mkString(",")}\n\t" +
        s"Driver arguments: ${submitExecution.driverArguments}")

      val spartaLauncher = new SpartaLauncher()
        .setAppResource(submitExecution.driverFile)
        .setMainClass(submitExecution.driverClass)
        .setMaster(submitExecution.master)

      //Set Spark Home
      spartaLauncher.setSparkHome(submitExecution.sparkHome)
      //Spark arguments
      submitExecution.submitArguments.filter(_._2.nonEmpty)
        .foreach { case (k: String, v: String) => spartaLauncher.addSparkArg(k, v) }
      submitExecution.submitArguments.filter(_._2.isEmpty)
        .foreach { case (k: String, v: String) => spartaLauncher.addSparkArg(k) }
      // Spark properties
      submitExecution.sparkConfigurations.filter(_._2.nonEmpty)
        .foreach { case (key: String, value: String) => spartaLauncher.setConf(key.trim, value.trim) }
      // Driver (Sparta) params
      submitExecution.driverArguments.toSeq.sortWith { case (a, b) => a._1 < b._1 }
        .foreach { case (_, argValue) => spartaLauncher.addAppArgs(argValue) }
      //Redirect options
      spartaLauncher.redirectError()
      // Launch SparkApp
      spartaLauncher.startApplication()
    } match {
      case Failure(exception) =>
        val information = s"An error was encountered while launching the workflow"
        log.error(information, exception)
        val error = WorkflowError(information, PhaseEnum.Execution, exception.toString)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          statusId = UUID.randomUUID.toString,
          status = Failed,
          statusInfo = Option(information)
        ))
        executionService.setLastError(workflow.id.get, error)
      case Success(sparkHandler) =>
        val information = "Workflow launched correctly"
        log.info(information)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          statusId = UUID.randomUUID.toString,
          status = Launched,
          statusInfo = Option(information)
        ))
        executionService.setStartDate(workflow.id.get, new DateTime())
        if (workflow.settings.global.executionMode == marathon)
          listenerService.addSparkClientListener(workflow.id.get, sparkHandler)
    }
  }
}