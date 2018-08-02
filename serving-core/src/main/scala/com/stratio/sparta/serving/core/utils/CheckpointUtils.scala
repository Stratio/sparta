/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import java.io.File
import java.util.Calendar

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.commons.io.FileUtils

import scala.util.{Failure, Success, Try}

trait CheckpointUtils extends SLF4JLogging {

  lazy private val hdfsService = HdfsService()

  /* PUBLIC METHODS */

  def deleteFromLocal(workflow: Workflow): Unit = {
    val checkpointDirectory = cleanLocalCheckpointPath(checkpointPathFromWorkflow(workflow, checkTime = false))
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
      if (workflow.settings.global.executionMode == local)
        deleteFromLocal(workflow)
      else deleteFromHDFS(workflow)
    } match {
      case Success(_) =>
        log.info(s"Checkpoint deleted: ${checkpointPathFromWorkflow(workflow, checkTime = false)}")
      case Failure(ex) =>
        log.error("Unable to delete checkpoint", ex)
    }

  def createLocalCheckpointPath(workflow: Workflow): Unit = {
    if (workflow.settings.global.executionMode == local)
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
  private def cleanLocalCheckpointPath(path: String): String =
    path.replace("file://", "")

  private def cleanCheckpointPath(path: String): String = {
    val hdfsPrefix = "hdfs://"

    if (path.startsWith(hdfsPrefix))
      log.warn(s"The path starts with $hdfsPrefix and is not valid, it was replaced with an empty value")
    path.replace(hdfsPrefix, "")
  }

  private def createFromLocal(workflow: Workflow): Unit = {
    val checkpointDirectory = cleanLocalCheckpointPath(checkpointPathFromWorkflow(workflow))
    log.debug(s"Creating checkpoint: $checkpointDirectory")
    try {
      FileUtils.forceMkdir(new File(checkpointDirectory))
    } catch {
      case e: Exception => log.error(s"Error creating directory $checkpointDirectory. ${e.getLocalizedMessage}")
    }
  }
}