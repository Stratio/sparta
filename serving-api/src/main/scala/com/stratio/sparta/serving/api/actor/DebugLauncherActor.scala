/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, PoisonPill}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.exception.ErrorManagerException
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.DebugWorkflowService
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class DebugLauncherActor(curatorFramework: CuratorFramework) extends Actor with SLF4JLogging {

  lazy private val contextService: ContextsService = ContextsService(curatorFramework)
  lazy private val debugWorkflowService = new DebugWorkflowService(curatorFramework)

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow, userId: Option[String]) => doDebugWorkflow(workflow)
    case _ => log.info("Unrecognized message in Debug Launcher Actor")
  }

  private def doDebugWorkflow(workflow: Workflow): Unit = {
    try {
      Try {
        val jars = JarsHelper.localUserPluginJars(workflow)
        log.info(s"Starting workflow debug")

        if (workflow.executionEngine == Streaming) {
          contextService.localStreamingContext(workflow, jars)
          stopStreamingContext()
        }
        if (workflow.executionEngine == Batch)
          contextService.localContext(workflow, jars)
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
            Option(WorkflowError(
              information,
              PhaseEnum.Execution,
              exception.toString,
              Try(exception.getCause.getMessage).toOption.getOrElse(exception.getMessage)
            ))
          )
          self ! PoisonPill
      }
      debugWorkflowService.setEndDate(workflow.id.get)
    } finally {
      stopStreamingContext()
    }
  }
}
