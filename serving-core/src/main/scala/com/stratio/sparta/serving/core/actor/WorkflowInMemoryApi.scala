/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import scala.util.Try

import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.{ExecutionChange, ExecutionRemove}
import com.stratio.sparta.serving.core.actor.GroupPublisherActor.{GroupChange, GroupRemove}
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{StatusChange, StatusRemove}
import com.stratio.sparta.serving.core.actor.WorkflowInMemoryApi._
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow._

class WorkflowInMemoryApi extends InMemoryServicesStatus {

  override def persistenceId: String = AkkaConstant.WorkflowApiActorName

  import WorkflowDtoImplicit._

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[StatusChange])
    context.system.eventStream.subscribe(self, classOf[StatusRemove])
    context.system.eventStream.subscribe(self, classOf[ExecutionChange])
    context.system.eventStream.subscribe(self, classOf[ExecutionRemove])
    context.system.eventStream.subscribe(self, classOf[WorkflowChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.subscribe(self, classOf[GroupChange])
    context.system.eventStream.subscribe(self, classOf[GroupRemove])

  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[StatusChange])
    context.system.eventStream.unsubscribe(self, classOf[StatusRemove])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionChange])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionRemove])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.unsubscribe(self, classOf[GroupChange])
    context.system.eventStream.unsubscribe(self, classOf[GroupRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  //scalastyle:off
  def apiReceive: Receive = {
    case FindMemoryWorkflowRaw(workflowId) =>
      log.debug(s"Find workflow by id $workflowId")
      sender ! Try {
        workflows.getOrElse(
          workflowId,
          throw new ServerException(s"No workflow with id $workflowId")
        ).copy(status = statuses.get(workflowId), execution = executions.get(workflowId))
      }
    case FindAllMemoryWorkflowRaw =>
      log.debug(s"Find all workflows")
      sender ! Try {
        workflows.values.map(workflow => wfCopy(workflow)).toSeq
      }
    case FindAllMemoryWorkflowDto =>
      log.debug(s"Find all workflows dto")
      sender ! Try {
        workflows.values.map { workflow =>
          val workflowDto: WorkflowDto = wfCopy(workflow)
          workflowDto
        }.toSeq
      }
    case FindByGroupMemoryWorkflowDto(groupId: String) =>
      log.debug(s"Find workflows dto by group id $groupId")
      sender ! Try {
        workflows.values.flatMap { workflow =>
          if (workflow.group.id.get == groupId) {
            val workflowDto: WorkflowDto = wfCopy(workflow)
            Option(workflowDto)
          } else None
        }.toSeq
      }
    case FindByIdsMemoryWorkflowRaw(workflowsId) =>
      log.debug(s"Find workflows by workflow ids $workflowsId")
      sender ! Try {
        workflows.filterKeys(key => workflowsId.contains(key)).values.toSeq
          .map(workflow => wfCopy(workflow))
      }
    case FindByQueryMemoryWorkflowRaw(query) =>
      log.debug(s"Find workflow by query $query")
      sender ! Try {
        workflows.find { case (_, workflow) =>
          workflow.name == query.name && workflow.version == query.version.getOrElse(0L) && workflow.group.id.get == query.group.getOrElse(DefaultGroup.id.get)
        }.map { case (_, workflow) => wfCopy(workflow)
        }.getOrElse(throw new ServerException(s"No workflow with name ${query.name}"))
      }
  }


  /** PRIVATE METHODS **/

  private [sparta] def wfCopy(workflow: Workflow): Workflow = {
    workflow.copy(status = statuses.get(workflow.id.get),
      execution = executions.get(workflow.id.get))
  }
}

object WorkflowInMemoryApi {

  case class FindMemoryWorkflowRaw(workflowId: String)

  case object FindAllMemoryWorkflowRaw

  case object FindAllMemoryWorkflowDto

  case class FindByGroupMemoryWorkflowDto(group: String)

  case class FindByIdsMemoryWorkflowRaw(workflowIds: Seq[String])

  case class FindByQueryMemoryWorkflowRaw(query: WorkflowQuery)

}
