/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.ExecutionChange
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.StatusChange
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{WorkflowStatus, WorkflowStatusStream}

import scala.concurrent._

class StatusListenerActor extends InMemoryServicesStatus {

  override def persistenceId: String = AkkaConstant.StatusChangeActorName

  import StatusListenerActor._

  private val workflowActions = scala.collection.mutable.Map[String, List[StatusChangeAction]]()
  private val genericActions = scala.collection.mutable.Map[String, List[StatusChangeAction]]()

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[StatusChange])
    context.system.eventStream.subscribe(self, classOf[ExecutionChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[StatusChange])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRemove])
  }

  val receiveCommand: Receive = managementReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def managementReceive: Receive = {
    case request@OnWorkflowStatusChangeDo(id) =>
      workflowActions += ((id, request.action :: workflowActions.getOrElse(id, Nil)))
    case request@OnWorkflowStatusesChangeDo(key) =>
      genericActions += ((key, request.action :: genericActions.getOrElse(key, Nil)))
    case ForgetWorkflowStatusActions(id) =>
      genericActions -= id
      workflowActions -= id
    case request@WorkflowRemove(path, workflow) =>
      persist(request) { case wRemove =>
        wRemove.workflow.id.foreach { id =>
          statuses.get(id).foreach { wStatus =>
            doWorkflowChange(wStatus.copy(status = WorkflowStatusEnum.Stopped))
          }
          workflowActions -= id
          removeWorkflows(wRemove.workflow)
          removeExecution(id)
          removeStatus(id)
        }
        checkSaveSnapshot()
      }
    case request@StatusChange(path, workflowStatus) =>
      persist(request) { case stChange =>
        val cachedStatus = statuses.get(stChange.workflowStatus.id)
        if (cachedStatus.isEmpty || cachedStatus.get.status != stChange.workflowStatus.status) {
          doWorkflowChange(stChange.workflowStatus)
        }
        addStatus(stChange.workflowStatus)
        checkSaveSnapshot()
      }
  }

  def doWorkflowChange(workflowStatus: WorkflowStatus): Unit = {
    workflowActions.getOrElse(workflowStatus.id, Nil) foreach { callback =>
      Future {
        try {
          blocking(callback(WorkflowStatusStream(
            workflowStatus,
            workflows.get(workflowStatus.id),
            executions.get(workflowStatus.id)
          )))
        } catch {
          case e: Exception => log.error(s"Error executing action for workflow status ${workflowStatus.id}." +
            s" With exception: ${e.getLocalizedMessage}")
        }
      }(context.dispatcher)
    }
    genericActions.foreach { case (_, gActions) =>
      gActions.foreach { callback =>
        Future {
          try {
            blocking(callback(WorkflowStatusStream(
              workflowStatus,
              workflows.get(workflowStatus.id),
              executions.get(workflowStatus.id)
            )))
          } catch {
            case e: Exception => log.error(s"Error executing action for workflow status ${workflowStatus.id}." +
              s" With exception: ${e.getLocalizedMessage}")
          }
        }(context.dispatcher)
      }
    }
  }

}

object StatusListenerActor {

  type StatusChangeAction = WorkflowStatusStream => Unit

  case class OnWorkflowStatusChangeDo(workflowId: String)(val action: StatusChangeAction)

  case class OnWorkflowStatusesChangeDo(key: String)(val action: StatusChangeAction)

  case class ForgetWorkflowStatusActions(id: String)

}