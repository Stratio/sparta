/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, ActorRef}
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.serving.core.actor.LauncherActor.{Run, Start}
import com.stratio.sparta.serving.core.constants.SparkConstant._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services._
import com.stratio.sparta.serving.core.utils.SchedulerUtils
import org.apache.spark.launcher.SpartaLauncher
import org.joda.time.DateTime

import scala.util.{Failure, Properties, Success, Try}

class ClusterLauncherActor(executionStatusListenerActor: Option[ActorRef] = None) extends Actor with SchedulerUtils {

  lazy val executionService = PostgresDaoFactory.executionPgService
  lazy val listenerService = executionStatusListenerActor.map(new ListenerService(_))

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflowExecution) => doStartExecution(workflowExecution)
    case Run(execution: WorkflowExecution) => doRun(execution)
    case _ => log.info("Unrecognized message in Cluster Launcher Actor")
  }

  def doStartExecution(workflowExecution: WorkflowExecution): Unit = {
    val updateDateResult = if(workflowExecution.genericDataExecution.launchDate.isEmpty)
      executionService.setLaunchDate(workflowExecution, new DateTime())
    else workflowExecution
    doRun(updateDateResult)
  }

  //scalastyle:off
  def doRun(workflowExecution: WorkflowExecution): Unit = {
    import com.stratio.sparta.serving.core.constants._
    Try {
      val workflow = workflowExecution.getWorkflowToExecute
      val submitExecution = workflowExecution.sparkSubmitExecution.get
      val sparkSubmitWithMetrics = submitExecution.submitArguments
        .mkString(",")
        .replaceAll(
          SparkDriverMetricsReplaceRegex,
          Properties.envOrNone(MarathonConstant.PrometheusEnvironmentPortHost)
            .orElse(Properties.envOrNone(MarathonConstant.PrometheusEnvironmentPortCalico))
            .getOrElse(MarathonConstant.PrometheusEnvironmentPortCalico)
        )
      log.info(s"Launching Sparta workflow with options ... \n\t" +
        s"Workflow name: ${workflow.name}\n\t" +
        s"Main Class: $SpartaDriverClass\n\t" +
        s"Driver file: ${submitExecution.driverFile}\n\t" +
        s"Master: ${submitExecution.master}\n\t" +
        s"Spark submit arguments: $sparkSubmitWithMetrics\n\t" +
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
        val error = WorkflowError(
          information,
          PhaseEnum.Execution,
          exception.toString,
          ExceptionHelper.toPrintableException(exception)
        )
        executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(state = Failed, statusInfo = Option(information))
        ), error)
        log.debug(s"Updated correctly the execution status ${workflowExecution.getExecutionId} to $Failed in ClusterLauncherActor")
      case Success(sparkHandler) =>
        if (workflowExecution.getWorkflowToExecute.settings.global.executionMode == marathon)
          listenerService.foreach(_.addSparkClientListener(workflowExecution.getExecutionId, sparkHandler))
        val information = "Workflow launched correctly"
        log.info(information)
        val updateStateResult = executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(state = Launched, statusInfo = Option(information))
        ))
        executionService.setStartDate(updateStateResult, new DateTime())
        log.debug(s"Updated correctly the execution status ${workflowExecution.getExecutionId} to $Launched in ClusterLauncherActor")
    }
  }
}