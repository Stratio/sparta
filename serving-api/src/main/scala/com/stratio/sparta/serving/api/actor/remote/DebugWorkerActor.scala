/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor.remote

import akka.actor.{ActorRef, Props}
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ErrorManagerException
import com.stratio.sparta.serving.core.factory.SparkContextFactory.stopStreamingContext
import com.stratio.sparta.serving.core.factory.{PostgresDaoFactory, SparkContextFactory}
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import org.json4s.native.Serialization.read
import DebugWorkerActor._
import com.stratio.sparta.serving.core.models.RocketModes

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class DebugWorkerActor extends WorkerActor {

  val dispatcherActorName = AkkaConstant.DebugDispatcherActorName
  val rocketMode = RocketModes.Debug
  val workerTopic = DebugWorkerTopic

  lazy val debugWorkflowPgService = PostgresDaoFactory.debugWorkflowPgService

  override def workerPreStart(): Unit = {
    SparkContextFactory.getOrCreateStandAloneXDSession(None)
  }

  // TODO ALVARO Legacy code: make it functional and inject it into this actor.
  //scalastyle:off
  override def executeJob(job: String, jobSender: ActorRef): Future[Unit] = Future {
    try {
      val execution = read[WorkflowExecution](job)
      val workflow = execution.getWorkflowToExecute
      Try {
        val jars = JarsHelper.localUserPluginJars(workflow)
        log.info(s"Starting workflow debug")

        if (workflow.executionEngine == Streaming) {
          ContextsService.localStreamingContext(execution, jars)
          stopStreamingContext()
        }
        if (workflow.executionEngine == Batch)
          ContextsService.localContext(execution, jars)
      } match {
        case Success(_) =>
          log.info("Workflow debug executed successfully")
          for {
            _ <- debugWorkflowPgService.setSuccessful(workflow.id.get, state = true)
            _ <- debugWorkflowPgService.setEndDate(workflow.id.get)
          } yield {
            log.info("Workflow debug results updated successfully")
          }
        case Failure(_: org.I0Itec.zkclient.exception.ZkInterruptedException) =>
          log.info("Workflow debug executed successfully with Gosec-Zookeeper exception")
          for {
            _ <- debugWorkflowPgService.setSuccessful(workflow.id.get, state = true)
            _ <- debugWorkflowPgService.setEndDate(workflow.id.get)
          } yield {
            log.info("Workflow debug results updated successfully")
          }
        case Failure(_: ErrorManagerException) =>
          log.info("Workflow debug executed with ErrorManager exception")
          for {
            _ <- debugWorkflowPgService.setSuccessful(workflow.id.get, state = false)
            _ <- debugWorkflowPgService.setEndDate(workflow.id.get)
          } yield {
            log.info("Workflow debug results updated successfully")
          }
        case Failure(exception) =>
          val information = s"Error initiating the workflow debug"
          log.info(information, exception)
          for {
            _ <- debugWorkflowPgService.setSuccessful(workflow.id.get, state = false)
            _ <- debugWorkflowPgService.setError(
              workflow.id.get,
              Option(WorkflowError(
                information,
                PhaseEnum.Execution,
                exception.toString,
                Try(exception.getCause.getMessage).toOption.getOrElse(exception.getMessage)
              ))
            )
            _ <- debugWorkflowPgService.setEndDate(workflow.id.get)
          } yield {
            log.info("Workflow debug results updated successfully")
          }
      }
    } finally {
      stopStreamingContext()
    }
  }
}

object DebugWorkerActor {

  val DebugWorkerTopic = "debug-worker"

  def props: Props = Props[DebugWorkerActor]

}
