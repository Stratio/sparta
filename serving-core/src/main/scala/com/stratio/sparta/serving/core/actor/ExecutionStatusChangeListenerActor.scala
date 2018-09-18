/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor.ExecutionStatusChange
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecutionStatusChange

import scala.concurrent._

class ExecutionStatusChangeListenerActor extends Actor with SpartaSerializer with SLF4JLogging {

  import ExecutionStatusChangeListenerActor._

  private val executionStatusActions = scala.collection.mutable.Map[String, List[ExecutionStatusChangeAction]]()
  private val genericActions = scala.collection.mutable.Map[String, List[ExecutionStatusChangeAction]]()

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[ExecutionStatusChange])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[ExecutionStatusChange])
  }

  override def receive: Receive = {
    case request@OnExecutionStatusChangeDo(id) =>
      executionStatusActions += ((id, request.action :: executionStatusActions.getOrElse(id, Nil)))
    case request@OnExecutionStatusesChangeDo(key) =>
      genericActions += ((key, request.action :: genericActions.getOrElse(key, Nil)))
    case ForgetExecutionStatusActions(id) =>
      genericActions -= id
      executionStatusActions -= id
    case ExecutionStatusChange(_, executionStatusChange) =>
      import executionStatusChange._
      if (originalExecution.lastStatus.state != newExecution.lastStatus.state) {
        doExecutionStatusChange(executionStatusChange)
      }
  }

  def doExecutionStatusChange(executionStatusChange: WorkflowExecutionStatusChange): Unit = {
    val executionId = executionStatusChange.newExecution.getExecutionId
    executionStatusActions.getOrElse(executionId, Nil) foreach { callback =>
      Future {
        try {
          blocking(callback(executionStatusChange))
        } catch {
          case e: Exception =>
            log.error(s"Error executing action for workflow execution $executionId. " +
              s"With exception: ${e.getLocalizedMessage}")
        }
      }(context.dispatcher)
    }
    genericActions.foreach { case (_, gActions) =>
      gActions.foreach { callback =>
        Future {
          try {
            blocking(callback(executionStatusChange))
          } catch {
            case e: Exception =>
              log.error(s"Error executing action for workflow execution $executionId." +
                s" With exception: ${e.getLocalizedMessage}")
          }
        }(context.dispatcher)
      }
    }
  }

}

object ExecutionStatusChangeListenerActor {

  type ExecutionStatusChangeAction = WorkflowExecutionStatusChange => Unit

  case class OnExecutionStatusChangeDo(executionId: String)(val action: ExecutionStatusChangeAction)

  case class OnExecutionStatusesChangeDo(key: String)(val action: ExecutionStatusChangeAction)

  case class ForgetExecutionStatusActions(id: String)

}