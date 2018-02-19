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

package com.stratio.sparta.serving.api.actor

import java.io.File

import akka.actor.{Actor, PoisonPill}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.enumerators.{WorkflowExecutionEngine, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, Workflow, WorkflowError, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{HdfsFilesService, WorkflowStatusService}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class LocalLauncherActor(streamingContextService: StreamingContextService, val curatorFramework: CuratorFramework)
  extends Actor with SLF4JLogging {

  lazy private val statusService = new WorkflowStatusService(curatorFramework)
  lazy private val hdfsFilesService = HdfsFilesService()

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow) => doInitSpartaContext(workflow)
    case _ => log.info("Unrecognized message in Local Launcher Actor")
  }

  //scalastyle:off
  private def doInitSpartaContext(workflow: Workflow): Unit = {
    Try {
      val jars = userPluginsFiles(workflow)

      jars.foreach(file => JarsHelper.addJarToClasspath(file))
      JarsHelper.addJdbcDriversToClassPath()

      val startingInfo = s"Starting local execution for the workflow"
      log.info(startingInfo)
      statusService.update(WorkflowStatus(
        id = workflow.id.get,
        status = WorkflowStatusEnum.NotStarted,
        statusInfo = Some(startingInfo),
        lastExecutionMode = Option(AppConstant.ConfigLocal)
      ))
      if (workflow.executionEngine == WorkflowExecutionEngine.Streaming)
        executeLocalStreamingContext(workflow, jars)
      if (workflow.executionEngine == WorkflowExecutionEngine.Batch)
        executeLocalBatchContext(workflow, jars)
    } match {
      case Success(_) =>
        SparkContextFactory.destroySparkContext()
        val information = s"Workflow stopped correctly"
        log.info(information)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = WorkflowStatusEnum.Finished,
          statusInfo = Some(information)
        ))
        self ! PoisonPill
      case Failure(exception) =>
        val information = s"Error initiating the workflow"
        log.error(information, exception)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = WorkflowStatusEnum.Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
        SparkContextFactory.destroySparkContext()
        self ! PoisonPill
    }
  }

  private def executeLocalStreamingContext(workflow: Workflow, jars: Seq[File]): Unit = {
    val (spartaWorkflow, ssc) = streamingContextService.localStreamingContext(workflow, jars)
    spartaWorkflow.setup()
    ssc.start()
    val startedInformation = s"Workflow started correctly"
    log.info(startedInformation)
    statusService.update(WorkflowStatus(
      id = workflow.id.get,
      status = WorkflowStatusEnum.Started,
      statusInfo = Some(startedInformation)
    ))
    ssc.awaitTermination()
    spartaWorkflow.cleanUp()
  }

  private def executeLocalBatchContext(workflow: Workflow, jars: Seq[File]): Unit = {
    val startedInformation = s"Starting workflow"
    log.info(startedInformation)
    statusService.update(WorkflowStatus(
      id = workflow.id.get,
      status = WorkflowStatusEnum.Starting,
      statusInfo = Some(startedInformation)
    ))

    val spartaWorkflow = streamingContextService.localContext(workflow, jars)
    spartaWorkflow.cleanUp()
  }

  private def userPluginsFiles(workflow: Workflow): Seq[File] = {
    val uploadedPlugins = if (workflow.settings.global.addAllUploadedPlugins)
      Try {
        hdfsFilesService.browsePlugins.flatMap { fileStatus =>
          if (fileStatus.isFile && fileStatus.getPath.getName.endsWith(".jar")) {
            val fileName = fileStatus.getPath.toUri.toString.replace("file://", "")
            Option(new File(fileName))
          } else None
        }
      }.getOrElse(Seq.empty[File])
    else Seq.empty[File]

    val userPlugins = workflow.settings.global.userPluginsJars
      .filter(userJar => userJar.jarPath.toString.nonEmpty && userJar.jarPath.toString.endsWith(".jar"))
      .map(_.jarPath.toString)
      .distinct
      .map(filePath => new File(filePath))

    uploadedPlugins ++ userPlugins
  }

}
