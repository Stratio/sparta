/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, PoisonPill}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.exception.ErrorManagerException
import com.stratio.sparta.serving.core.factory.SparkContextFactory.stopSparkContext
import com.stratio.sparta.serving.core.helpers.{JarsHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.{DebugWorkflowService, HdfsFilesService}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class DebugLauncherActor(curatorFramework: CuratorFramework) extends Actor with SLF4JLogging {

  lazy private val contextService: ContextsService = ContextsService(curatorFramework)
  lazy private val debugWorkflowService = new DebugWorkflowService(curatorFramework)
  lazy private val hdfsFilesService = HdfsFilesService()

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow, userId: Option[String]) => doDebugWorkflow(workflow)
    case _ => log.info("Unrecognized message in Debug Launcher Actor")
  }

  private def doDebugWorkflow(workflow: Workflow): Unit = {
    Try {
      val jars = WorkflowHelper.userPluginsFiles(workflow, hdfsFilesService)
      jars.foreach(file => JarsHelper.addJarToClasspath(file))
      log.info(s"Starting workflow debug")

      if (workflow.executionEngine == Streaming)
        contextService.localStreamingContext(workflow, jars)
      if (workflow.executionEngine == Batch)
        contextService.localContext(workflow, jars)

      stopSparkContext()
    } match {
      case Success(_) =>
        log.info("Workflow debug executed successfully")
        debugWorkflowService.setSuccessful(workflow.id.get, state = true)
        self ! PoisonPill
      case Failure(_: ErrorManagerException) =>
        debugWorkflowService.setSuccessful(workflow.id.get, state = false)
        self ! PoisonPill
      case Failure(exception) =>
        val information = s"Error initiating the workflow debug"
        log.error(information, exception)
        debugWorkflowService.setSuccessful(workflow.id.get, state = false)
        debugWorkflowService.setError(
          workflow.id.get,
          Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        )
        self ! PoisonPill
    }
    debugWorkflowService.setEndDate(workflow.id.get)
    stopSparkContext()
  }
}
