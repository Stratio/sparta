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

package com.stratio.sparta.serving.core.utils

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatusModel
import com.typesafe.config.Config

import scala.util.{Failure, Success, Try}

trait LauncherUtils extends SLF4JLogging{

  def loggingResponseWorkflowStatus(response: Try[WorkflowStatusModel]): Unit =
    response match {
      case Success(statusModel) =>
        log.info(s"Workflow status model created or updated correctly: " +
          s"\n\tId: ${statusModel.id}\n\tStatus: ${statusModel.status}")
      case Failure(e) =>
        log.error(s"An error was encountered while creating the Workflow status model. " +
          s"Error: ${e.getLocalizedMessage}", e)
    }

  def getZookeeperConfig: Config = SpartaConfig.getZookeeperConfig.getOrElse {
    val message = "Impossible to extract Zookeeper Configuration"
    log.error(message)
    throw new RuntimeException(message)
  }
}
