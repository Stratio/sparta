/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangeListenerActor.{ForgetExecutionStatusActions, OnExecutionStatusChangeDo}
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.spark.launcher.SparkAppHandle

import scala.util.{Failure, Success, Try}

class ListenerService(executionStatusListenerActor: ActorRef) extends SpartaSerializer with SLF4JLogging {

  private val executionService = PostgresDaoFactory.executionPgService

  //scalastyle:off
  def addSparkClientListener(executionId: String, handler: SparkAppHandle): Unit = {
    log.info(s"Spark Client listener added to execution id: $executionId")
    executionStatusListenerActor ! OnExecutionStatusChangeDo(executionId) { executionStatusChange =>

      val state = executionStatusChange.newExecution.lastStatus.state

      if (state == Stopping ||state == StoppingByUser || state == Failed) {
        log.info("Stop message received from Zookeeper")
        try {
          Try {
            log.info("Stopping execution with handler")
            handler.stop()
          } match {
            case Success(_) =>
              val information = s"Workflow correctly stopped with Spark Handler"
              log.info(information)
              executionService.updateStatus(ExecutionStatusUpdate(
                executionId,
                ExecutionStatus(
                  state = if (state == StoppingByUser) StoppedByUser else Failed,
                  statusInfo = Option(information)
                )))
            case Failure(e) =>
              val error = s"An error was encountered while stopping workflow with Spark Handler, killing it ..."
              log.warn(s"$error with exception: ${e.getLocalizedMessage}")
              Try(handler.kill()) match {
                case Success(_) =>
                  val information = s"Workflow killed with Spark Handler"
                  log.info(information)
                  executionService.updateStatus(ExecutionStatusUpdate(
                    executionId,
                    ExecutionStatus(
                      state = if (state == StoppingByUser) StoppedByUser else Failed,
                      statusInfo = Option(information)
                    )))
                case Failure(exception) =>
                  val error = s"Problems encountered while killing workflow with Spark Handler"
                  log.warn(s"$error with exception: ${exception.getLocalizedMessage}")
                  val wError = WorkflowError(
                    error,
                    PhaseEnum.Stop,
                    exception.toString,
                    ExceptionHelper.toPrintableException(exception)
                  )
                  executionService.updateStatus(ExecutionStatusUpdate(
                    executionId,
                    ExecutionStatus(
                      state = Failed,
                      statusInfo = Option(error)
                    )), wError)
              }
          }
        } finally {
          executionStatusListenerActor ! ForgetExecutionStatusActions(executionId)
        }
      }
    }
  }

  //scalastyle:on

}
