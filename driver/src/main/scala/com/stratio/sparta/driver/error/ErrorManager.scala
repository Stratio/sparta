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

package com.stratio.sparta.driver.error

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.NotDefined
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowError, Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.utils.WorkflowStatusUtils
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

trait ErrorManager extends SLF4JLogging {

  val workflow: Workflow

  def traceError(error: WorkflowError): Unit

  def traceFunction[T](code: PhaseEnum.Value, okMessage: String, errorMessage: String)(f: => T): T = {
    Try(f) match {
      case Success(result) =>
        log.info(okMessage)
        result
      case Failure(ex) =>
        throw logAndCreateEx(code, ex, workflow, errorMessage)
    }
  }

  def logAndCreateEx(code: PhaseEnum.Value,
                     exception: Throwable,
                     policy: Workflow,
                     message: String
                    ): RuntimeException = {
    val originalMsg = exception.getCause match {
      case _: ClassNotFoundException => "The component couldn't be found in classpath. Please check the type."
      case _ => exception.toString
    }
    val policyError = WorkflowError(message, code, originalMsg)
    log.error("An error was detected : {}", policyError)
    Try {
      traceError(policyError)
    } recover {
      case e => log.error(s"Error while persisting error: $policyError", e)
    }
    new RuntimeException(message, exception)
  }
}

trait ZooKeeperError extends ErrorManager with WorkflowStatusUtils {

  val curatorFramework: CuratorFramework

  def traceError(error: WorkflowError): Unit =
    updateStatus(WorkflowStatus(workflow.id.get, NotDefined, None, None, lastError = Some(error)))

  def clearError(): Unit =
    clearLastError(workflow.id.get)
}

trait LogError extends ErrorManager with SLF4JLogging {

  def traceError(error: WorkflowError): Unit = log.error(s"This error was not saved to ZK : $error")
}

