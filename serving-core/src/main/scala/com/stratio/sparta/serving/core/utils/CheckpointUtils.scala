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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import org.apache.commons.io.FileUtils

import scala.util.{Failure, Success, Try}

trait CheckpointUtils extends SLF4JLogging {

  /* PUBLIC METHODS */

  def isLocalMode(policyModel: PolicyModel): Boolean =
    policyModel.executionMode match {
      case Some(policyExecutionMode) if policyExecutionMode.nonEmpty => policyExecutionMode.equalsIgnoreCase("local")
      case _ =>
        SpartaConfig.getDetailConfig match {
          case Some(detailConfig) => detailConfig.getString(ExecutionMode).equalsIgnoreCase("local")
          case None => true
        }
    }

  def deleteFromLocal(policy: PolicyModel): Unit = {
    val checkpointDirectory = checkpointPath(policy)
    log.info(s"Deleting checkpoint directory: $checkpointDirectory")
    FileUtils.deleteDirectory(new File(checkpointDirectory))
  }

  def deleteFromHDFS(policy: PolicyModel): Unit = {
    val checkpointDirectory = checkpointPath(policy)
    log.info(s"Deleting checkpoint directory: $checkpointDirectory")
    HdfsUtils().delete(checkpointDirectory)
  }

  def isHadoopEnvironmentDefined: Boolean =
    Option(System.getenv(SystemHadoopConfDir)) match {
      case Some(_) => true
      case None => false
    }

  def deleteCheckpointPath(policy: PolicyModel): Unit =
    Try {
      if (isLocalMode(policy)) deleteFromLocal(policy)
      else deleteFromHDFS(policy)
    } match {
      case Success(_) => log.info(s"Checkpoint deleted in folder: ${checkpointPath(policy)}")
      case Failure(ex) => log.error("Cannot delete checkpoint folder", ex)
    }

  def createLocalCheckpointPath(policy: PolicyModel): Unit = {
    if (isLocalMode(policy))
      Try {
        createFromLocal(policy)
      } match {
        case Success(_) => log.info(s"Checkpoint created in folder: ${checkpointPath(policy)}")
        case Failure(ex) => log.error("Cannot create checkpoint folder", ex)
      }
  }

  def checkpointPath(policy: PolicyModel): String =
    policy.checkpointPath.map { path =>
      s"${cleanCheckpointPath(path)}/${policy.name}"
    } getOrElse checkpointPathFromProperties(policy)

  def autoDeleteCheckpointPath(policy: PolicyModel): Boolean =
    policy.autoDeleteCheckpoint.getOrElse(autoDeleteCheckpointPathFromProperties)

  /* PRIVATE METHODS */

  private def cleanCheckpointPath(path: String): String = {
    val hdfsPrefix = "hdfs://"

    if (path.startsWith(hdfsPrefix))
      log.info(s"The path starts with $hdfsPrefix and is not valid, it is replaced with empty value")
    path.replace(hdfsPrefix, "")
  }

  private def checkpointPathFromProperties(policy: PolicyModel): String =
    (for {
      config <- SpartaConfig.getDetailConfig
      checkpointPath <- Try(cleanCheckpointPath(config.getString(ConfigCheckpointPath))).toOption
    } yield s"$checkpointPath/${policy.name}").getOrElse(generateDefaultCheckpointPath(policy))

  private def autoDeleteCheckpointPathFromProperties: Boolean =
    Try(SpartaConfig.getDetailConfig.get.getBoolean(ConfigAutoDeleteCheckpoint))
      .getOrElse(DefaultAutoDeleteCheckpoint)

  private def generateDefaultCheckpointPath(policy: PolicyModel): String = {
    val executionMode = policy.executionMode match {
      case Some(execMode) if execMode.nonEmpty => Some(execMode)
      case _ => SpartaConfig.getDetailConfig.map(_.getString(ExecutionMode))
    }

    executionMode match {
      case Some(mode) if mode == ConfigMesos || mode == ConfigYarn || mode == ConfigStandAlone =>
        DefaultCheckpointPathClusterMode +
          Try(SpartaConfig.getHdfsConfig.get.getString(HadoopUserName))
            .getOrElse(DefaultHdfsUser) +
          DefaultHdfsUser
      case Some(ConfigLocal) =>
        DefaultCheckpointPathLocalMode
      case _ =>
        throw new RuntimeException("Error getting execution mode")
    }
  }

  private def createFromLocal(policy: PolicyModel): Unit = {
    val checkpointDirectory = checkpointPath(policy)
    log.info(s"Creating checkpoint directory: $checkpointDirectory")
    FileUtils.forceMkdir(new File(checkpointDirectory))
  }
}
