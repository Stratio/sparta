/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.io.File

import akka.actor.{Actor, ActorRef, PoisonPill}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ErrorManagerException
import com.stratio.sparta.serving.core.factory.SparkContextFactory
import com.stratio.sparta.serving.core.helpers.{JarsHelper, LinkHelper}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, Workflow, WorkflowError, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{HdfsFilesService, WorkflowStatusService}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class LocalLauncherActor(statusListenerActor: ActorRef, val curatorFramework: CuratorFramework)
  extends Actor with SLF4JLogging {

  lazy private val contextService: ContextsService = ContextsService(curatorFramework, statusListenerActor)
  lazy private val statusService = new WorkflowStatusService(curatorFramework)
  lazy private val hdfsFilesService = HdfsFilesService()

  override def receive: PartialFunction[Any, Unit] = {
    case Start(workflow: Workflow) => doInitSpartaContext(workflow)
    case _ => log.info("Unrecognized message in Local Launcher Actor")
  }

  //scalastyle:off
  private def doInitSpartaContext(workflow: Workflow): Unit = {
    Try {
      statusService.update(WorkflowStatus(
        id = workflow.id.get,
        status = NotStarted,
        sparkURI = LinkHelper.getClusterLocalLink,
        lastUpdateDateWorkflow = workflow.lastUpdateDate,
        lastExecutionMode = Option(AppConstant.ConfigLocal)
      ))
      val jars = userPluginsFiles(workflow)
      jars.foreach(file => JarsHelper.addJarToClasspath(file))
      val startedInformation = s"Starting workflow in local mode"
      log.info(startedInformation)
      statusService.update(WorkflowStatus(
        id = workflow.id.get,
        status = Starting,
        statusInfo = Some(startedInformation)
      ))
      if (workflow.executionEngine == Streaming)
        executeLocalStreamingContext(workflow, jars)
      if (workflow.executionEngine == Batch) {
        executeLocalBatchContext(workflow, jars)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Stopping,
          statusInfo = Some("Workflow executed correctly")
        ))
      }
    } match {
      case Success(_) =>
        log.info("Workflow executed correctly")
        self ! PoisonPill
      case Failure(_: ErrorManagerException) =>
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed
        ))
        self ! PoisonPill
      case Failure(exception) =>
        val information = s"Error initiating the workflow"
        log.error(information, exception)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
        ))
        self ! PoisonPill
    }
  }

  private def executeLocalStreamingContext(workflow: Workflow, jars: Seq[File]): Unit =
    contextService.localStreamingContext(workflow, jars)

  private def executeLocalBatchContext(workflow: Workflow, jars: Seq[File]): Unit =
    contextService.localContext(workflow, jars)

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
