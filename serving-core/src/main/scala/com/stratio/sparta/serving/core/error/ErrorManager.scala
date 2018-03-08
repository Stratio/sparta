/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.error

import akka.event.Logging
import akka.event.Logging.LogLevel
import akka.event.Logging.LogLevel._
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.exception.ErrorManagerException
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.NotDefined
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, Workflow, WorkflowError, WorkflowStatus}
import com.stratio.sparta.serving.core.services.WorkflowStatusService
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

trait ErrorManager extends SLF4JLogging {

  val workflow: Workflow

  def traceFunction[T](
                        code: PhaseEnum.Value,
                        okMessage: String,
                        errorMessage: String,
                        logLevel: LogLevel = Logging.InfoLevel
                      )(f: => T): T = {
    Try(f) match {
      case Success(result) =>
        logLevel.asInt match {
          case 1 => log.error(okMessage)
          case 2 => log.warn(okMessage)
          case 3 => log.info(okMessage)
          case _ => log.debug(okMessage)
        }

        result
      case Failure(ex) =>
        throw logAndCreateEx(code, ex, workflow, errorMessage)
    }
  }

  protected def traceError(error: WorkflowError): Unit

  private def logAndCreateEx(code: PhaseEnum.Value,
                     exception: Throwable,
                     workflow: Workflow,
                     message: String
                    ): Throwable = {
    val originalMsg = exception.getCause.toString
    val workflowError = WorkflowError(message, code, originalMsg)
    log.error("An error was detected : {}", workflowError)
    Try {
      traceError(workflowError)
    } recover {
      case e => log.error(s"Error while persisting error: $workflowError", e)
    }

    ErrorManagerException(s"$message. Message: ${exception.getLocalizedMessage}", exception)
  }
}

trait ZooKeeperError extends ErrorManager {

  val curatorFramework: CuratorFramework
  val statusService = new WorkflowStatusService(curatorFramework)

  def traceError(error: WorkflowError): Unit =
    statusService.update(WorkflowStatus(workflow.id.get, NotDefined, None, None, lastError = Some(error)))

  def clearError(): Unit =
    workflow.id.foreach(id => statusService.clearLastError(id))

}

trait LogError extends ErrorManager with SLF4JLogging {

  def traceError(error: WorkflowError): Unit = log.error(s"This error was not saved to Zookeeper : $error")
}

