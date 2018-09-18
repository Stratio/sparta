/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, PoisonPill}
import akka.event.slf4j.SLF4JLogging

import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.models.WorkflowError

import com.stratio.sparta.serving.core.exception.ErrorManagerException
import com.stratio.sparta.serving.core.factory.SparkContextFactory._
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.workflow._

import scala.util.{Failure, Success, Try}

import scala.concurrent.ExecutionContext.Implicits.global
import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.serving.core.actor.LauncherActor.StartDebug
import com.stratio.sparta.serving.core.services.dao.DebugWorkflowPostgresDao

class DebugLauncherActor() extends Actor with SLF4JLogging {

  lazy private val contextService: ContextsService = ContextsService()
  lazy private val debugWorkflowPgService = new DebugWorkflowPostgresDao()

  override def receive: PartialFunction[Any, Unit] = {
    case StartDebug(execution) => doDebugWorkflow(execution)
    case _ => log.info("Unrecognized message in Debug Launcher Actor")
  }

  private def doDebugWorkflow(execution: WorkflowExecution): Unit = {
    try {
      val workflow = execution.getWorkflowToExecute
      Try {
        val jars = JarsHelper.localUserPluginJars(workflow)
        log.info(s"Starting workflow debug")

        if (workflow.executionEngine == Streaming) {
          contextService.localStreamingContext(execution, jars)
          stopStreamingContext()
        }
        if (workflow.executionEngine == Batch)
          contextService.localContext(execution, jars)
      } match {
        case Success(_) =>
          log.info("Workflow debug executed successfully")
          for {
            _ <- debugWorkflowPgService.setSuccessful(workflow.id.get, state = true)
          } yield {
            self ! PoisonPill
          }
        case Failure(_: ErrorManagerException) =>
          for {_ <- debugWorkflowPgService.setSuccessful(workflow.id.get, state = false)} yield {
            self ! PoisonPill
          }
        case Failure(exception) =>
          val information = s"Error initiating the workflow debug"
          log.error(information, exception)
          for {
            _ <- debugWorkflowPgService.setSuccessful(workflow.id.get, state = false)
            _ <- debugWorkflowPgService.setError(
              workflow.id.get,
              Option(WorkflowError(
                information,
                PhaseEnum.Execution,
                exception.toString,
                Try(exception.getCause.getMessage).toOption.getOrElse(exception.getMessage)
              ))
            )
          } yield {
            self ! PoisonPill
          }
      }
      debugWorkflowPgService.setEndDate(workflow.id.get)
    } finally {
      stopStreamingContext()
    }
  }
}
