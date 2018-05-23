/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.error

import java.util.Date

import akka.event.Logging
import akka.event.Logging.LogLevel
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.models
import com.stratio.sparta.sdk.workflow.enumerators.PhaseEnum
import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.serving.core.exception.ErrorManagerException
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.NotDefined
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{DebugWorkflowService, WorkflowStatusService}
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{ExecutionService, WorkflowStatusService}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

trait ErrorManager extends SLF4JLogging {

  val workflow: Workflow

  def traceFunction[T](
                        code: PhaseEnum.Value,
                        okMessage: String,
                        errorMessage: String,
                        logLevel: LogLevel = Logging.InfoLevel,
                        step: Option[String] = None
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
      case Failure(ex: Exception) =>
        throw logAndCreateEx(code, ex, workflow, errorMessage, step)
    }
  }

  def clearError(): Unit

  protected def traceError(error: WorkflowError): Unit

  private def logAndCreateEx(
                              code: PhaseEnum.Value,
                              exception: Exception,
                              workflow: Workflow,
                              message: String,
                              step: Option[String] = None
                            ): Throwable = {
    val originalMsg = exception.toString
    val workflowError = WorkflowError(message, code, originalMsg, new Date, step)
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
  val executionService = new ExecutionService(curatorFramework)

  def traceError(error: WorkflowError): Unit = {
    statusService.update(WorkflowStatus(workflow.id.get, NotDefined, None))
    executionService.setLastError(workflow.id.get, error)
  }

  def clearError(): Unit =
    workflow.id.foreach(id => executionService.clearLastError(id))

}

trait ZooKeeperDebugError extends ErrorManager {

  val curatorFramework: CuratorFramework
  val debugService = new DebugWorkflowService(curatorFramework)

  def traceError(error: WorkflowError): Unit =
    workflow.id.foreach(id => debugService.setError(id, Option(error)))

  def clearError(): Unit =
    workflow.id.foreach(id => debugService.clearLastError(id))

}

case class ZookeeperErrorImpl(workflow: Workflow, curatorFramework: CuratorFramework) extends ZooKeeperError

case class ZookeeperDebugErrorImpl(workflow: Workflow, curatorFramework: CuratorFramework) extends ZooKeeperDebugError

trait LogError extends ErrorManager with SLF4JLogging {

  def traceError(error: WorkflowError): Unit = log.error(s"This error was not saved to Zookeeper : $error")

  def clearError(): Unit = log.error(s"Cleaned errors")
}

