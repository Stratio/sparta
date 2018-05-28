/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.enumerators.PhaseEnum
import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.serving.core.actor.StatusListenerActor.{ForgetWorkflowStatusActions, OnWorkflowStatusChangeDo}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.launcher.SparkAppHandle

import scala.util.{Failure, Success, Try}

class ListenerService(curatorFramework: CuratorFramework, statusListenerActor: ActorRef) extends SpartaSerializer
  with SLF4JLogging {

  private val workflowService = new WorkflowService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)

  //scalastyle:off
  def addSparkClientListener(workflowId: String, handler: SparkAppHandle): Unit = {
    val workflow = workflowService.findById(workflowId)
    log.info(s"Spark Client listener added to ${workflow.name} with id: $workflowId")
    statusListenerActor ! OnWorkflowStatusChangeDo(workflowId) { workflowStatusStream =>
      if (workflowStatusStream.workflowStatus.status == Stopping || workflowStatusStream.workflowStatus.status == Failed) {
        log.info("Stop message received from Zookeeper")
        try {
          Try {
            log.info("Stopping submission workflow with handler")
            handler.stop()
          } match {
            case Success(_) =>
              val information = s"Workflow correctly stopped with Spark Handler"
              log.info(information)
              statusService.update(WorkflowStatus(
                id = workflowId,
                status = if (workflowStatusStream.workflowStatus.status == Stopping) Stopped else Failed,
                statusInfo = Some(information)
              ))
            case Failure(e) =>
              val error = s"An error was encountered while stopping workflow with Spark Handler, killing it ..."
              log.warn(s"$error. With exception: ${e.getLocalizedMessage}")
              Try(handler.kill()) match {
                case Success(_) =>
                  val information = s"Workflow killed with Spark Handler"
                  log.info(information)
                  statusService.update(WorkflowStatus(
                    id = workflowId,
                    status = if (workflowStatusStream.workflowStatus.status == Stopping) Stopped else Failed,
                    statusInfo = Some(information)
                  ))
                case Failure(exception) =>
                  val error = s"Problems encountered while killing workflow with Spark Handler"
                  log.warn(error)
                  val wError = WorkflowError(error, PhaseEnum.Stop, exception.toString)
                  statusService.update(WorkflowStatus(
                    id = workflowId,
                    status = Failed,
                    statusInfo = Some(error)
                  ))
                  executionService.setLastError(workflowId, wError)
              }
          }
        } finally {
          statusListenerActor ! ForgetWorkflowStatusActions(workflowId)
        }
      }
    }
  }

  //scalastyle:on

}
