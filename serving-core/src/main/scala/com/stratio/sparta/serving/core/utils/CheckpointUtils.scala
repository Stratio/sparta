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
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.commons.io.FileUtils

import scala.util.{Failure, Success, Try}

trait CheckpointUtils extends SLF4JLogging {

  lazy private val hdfsService = HdfsService()

  /* PUBLIC METHODS */

  def deleteFromLocal(workflow: Workflow): Unit = {
    val checkpointDirectory = checkpointPathFromWorkflow(workflow, checkTime = false)
    log.debug(s"Deleting checkpoint: $checkpointDirectory")
    try {
      FileUtils.deleteDirectory(new File(checkpointDirectory))
    } catch {
      case e: Exception => log.error(s"Error deleting directory $checkpointDirectory. ${e.getLocalizedMessage}")
    }
  }

  def deleteFromHDFS(workflow: Workflow): Unit = {
    val checkpointDirectory = checkpointPathFromWorkflow(workflow, checkTime = false)
    log.debug(s"Deleting checkpoint: $checkpointDirectory")
    hdfsService.delete(checkpointDirectory)
  }

  def deleteCheckpointPath(workflow: Workflow): Unit =
    Try {
      if (workflow.settings.global.executionMode == AppConstant.ConfigLocal)
        deleteFromLocal(workflow)
      else deleteFromHDFS(workflow)
    } match {
      case Success(_) =>
        log.info(s"Checkpoint deleted: ${checkpointPathFromWorkflow(workflow, checkTime = false)}")
      case Failure(ex) =>
        log.error("Unable to delete checkpoint", ex)
    }

  def createLocalCheckpointPath(workflow: Workflow): Unit = {
    if (workflow.settings.global.executionMode == AppConstant.ConfigLocal)
      Try {
        createFromLocal(workflow)
      } match {
        case Success(_) =>
          log.info(s"Checkpoint created: ${checkpointPathFromWorkflow(workflow, checkTime = false)}")
        case Failure(ex) =>
          log.error("Unable to create checkpoint", ex)
      }
  }

  def checkpointPathFromWorkflow(workflow: Workflow, checkTime: Boolean = true): String = {
    val path = cleanCheckpointPath(workflow.settings.streamingSettings.checkpointSettings.checkpointPath.toString)

    if (checkTime && workflow.settings.streamingSettings.checkpointSettings.addTimeToCheckpointPath)
      s"$path/${workflow.name}/${Calendar.getInstance().getTime.getTime}"
    else s"$path/${workflow.name}"
  }

  /* PRIVATE METHODS */

  private def cleanCheckpointPath(path: String): String = {
    val hdfsPrefix = "hdfs://"

    if (path.startsWith(hdfsPrefix))
      log.warn(s"The path starts with $hdfsPrefix and is not valid, it was replaced with an empty value")
    path.replace(hdfsPrefix, "")
  }

  private def createFromLocal(workflow: Workflow): Unit = {
    val checkpointDirectory = checkpointPathFromWorkflow(workflow)
    log.debug(s"Creating checkpoint: $checkpointDirectory")
    try {
      FileUtils.forceMkdir(new File(checkpointDirectory))
    } catch {
      case e: Exception => log.error(s"Error creating directory $checkpointDirectory. ${e.getLocalizedMessage}")
    }
  }
}