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
    val originalMsg = exception.getCause match {
      case _: ClassNotFoundException => "The component couldn't be found in classpath. Please check the type."
      case _ => exception.toString
    }
    val workflowError = WorkflowError(message, code, originalMsg)
    log.error("An error was detected : {}", workflowError)
    Try {
      traceError(workflowError)
    } recover {
      case e => log.error(s"Error while persisting error: $workflowError", e)
    }

    ErrorManagerException(message, exception)
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

