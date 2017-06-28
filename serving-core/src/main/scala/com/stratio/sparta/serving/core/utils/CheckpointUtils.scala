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

import java.io.File
import java.util.Calendar

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.workflow.WorkflowModel
import org.apache.commons.io.FileUtils

import scala.util.{Failure, Success, Try}

trait CheckpointUtils extends SLF4JLogging {

  /* PUBLIC METHODS */

  def deleteFromLocal(workflow: WorkflowModel): Unit = {
    val checkpointDirectory = checkpointPath(workflow, checkTime = false)
    log.info(s"Deleting checkpoint directory: $checkpointDirectory")
    FileUtils.deleteDirectory(new File(checkpointDirectory))
  }

  def deleteFromHDFS(workflow: WorkflowModel): Unit = {
    val checkpointDirectory = checkpointPath(workflow, checkTime = false)
    log.info(s"Deleting checkpoint directory: $checkpointDirectory")
    HdfsUtils().delete(checkpointDirectory)
  }

  def isHadoopEnvironmentDefined: Boolean =
    Option(System.getenv(SystemHadoopConfDir)) match {
      case Some(_) => true
      case None => false
    }

  def deleteCheckpointPath(workflow: WorkflowModel): Unit =
    Try {
      if (workflow.settings.global.executionMode == AppConstant.ConfigLocal)
        deleteFromLocal(workflow)
      else deleteFromHDFS(workflow)
    } match {
      case Success(_) => log.info(s"Checkpoint deleted in folder: ${checkpointPath(workflow, checkTime = false)}")
      case Failure(ex) => log.error("Cannot delete checkpoint folder", ex)
    }

  def createLocalCheckpointPath(workflow: WorkflowModel): Unit = {
    if (workflow.settings.global.executionMode == AppConstant.ConfigLocal)
      Try {
        createFromLocal(workflow)
      } match {
        case Success(_) => log.info(s"Checkpoint created in folder: ${checkpointPath(workflow, checkTime = false)}")
        case Failure(ex) => log.error("Cannot create checkpoint folder", ex)
      }
  }

  def checkpointPath(workflow: WorkflowModel, checkTime: Boolean = true): String = {
    val path = cleanCheckpointPath(workflow.settings.checkpointSettings.checkpointPath)

    if (checkTime && workflow.settings.checkpointSettings.addTimeToCheckpointPath)
      s"$path/${workflow.name}/${Calendar.getInstance().getTime.getTime}"
    else s"$path/${workflow.name}"
  }

  /* PRIVATE METHODS */

  private def cleanCheckpointPath(path: String): String = {
    val hdfsPrefix = "hdfs://"

    if (path.startsWith(hdfsPrefix))
      log.info(s"The path starts with $hdfsPrefix and is not valid, it is replaced with empty value")
    path.replace(hdfsPrefix, "")
  }

  private def createFromLocal(workflow: WorkflowModel): Unit = {
    val checkpointDirectory = checkpointPath(workflow)
    log.info(s"Creating checkpoint directory: $checkpointDirectory")
    FileUtils.forceMkdir(new File(checkpointDirectory))
  }
}