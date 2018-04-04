/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.actor.GroupPublisherActor.{GroupChange, GroupRemove}
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{StatusChange, StatusRemove}
import com.stratio.sparta.serving.core.actor.WorkflowInMemoryApi._
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow._

import scala.util.Try

class WorkflowInMemoryApi extends InMemoryServicesStatus {

  override def persistenceId: String = AkkaConstant.WorkflowApiActorName

  import WorkflowDtoImplicit._

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[StatusChange])
    context.system.eventStream.subscribe(self, classOf[StatusRemove])
    context.system.eventStream.subscribe(self, classOf[WorkflowChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRawChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.subscribe(self, classOf[WorkflowRawRemove])
    context.system.eventStream.subscribe(self, classOf[GroupChange])
    context.system.eventStream.subscribe(self, classOf[GroupRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[StatusChange])
    context.system.eventStream.unsubscribe(self, classOf[StatusRemove])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRawChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRawRemove])
    context.system.eventStream.unsubscribe(self, classOf[GroupChange])
    context.system.eventStream.unsubscribe(self, classOf[GroupRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  //scalastyle:off
  def apiReceive: Receive = {
    case FindMemoryWorkflowRaw(workflowId) =>
      log.debug(s"Find workflow by id $workflowId")
      sender ! Try {
        workflowsRaw.getOrElse(
          workflowId,
          throw new ServerException(s"No workflow with id $workflowId")
        ).copy(status = statuses.get(workflowId))
      }
    case FindMemoryWorkflowWithEnv(workflowId) =>
      log.debug(s"Find workflow with environment by id $workflowId")
      sender ! Try {
        workflowsWithEnv.getOrElse(
          workflowId,
          throw new ServerException(s"No workflow with id $workflowId")
        ).copy(status = statuses.get(workflowId))
      }
    case FindAllMemoryWorkflowRaw =>
      log.debug(s"Find all workflows")
      sender ! Try {
        workflowsRaw.values.map(workflow => workflow.copy(status = statuses.get(workflow.id.get))).toSeq
      }
    case FindAllMemoryWorkflowWithEnv =>
      log.debug(s"Find all workflows with environment")
      sender ! Try {
        workflowsWithEnv.values.map(workflow => workflow.copy(status = statuses.get(workflow.id.get))).toSeq
      }
    case FindAllMemoryWorkflowDto =>
      log.debug(s"Find all workflows dto")
      sender ! Try {
        workflowsRaw.values.map { workflow =>
          val workflowDto: WorkflowDto = workflow.copy(status = statuses.get(workflow.id.get))
          workflowDto
        }.toSeq
      }
    case FindByGroupMemoryWorkflowDto(groupId: String) =>
      log.debug(s"Find workflows dto by group id $groupId")
      sender ! Try {
        workflowsRaw.values.flatMap { workflow =>
          if (workflow.group.id.get == groupId) {
            val workflowDto: WorkflowDto = workflow.copy(status = statuses.get(workflow.id.get))
            Option(workflowDto)
          } else None
        }.toSeq
      }
    case FindByIdsMemoryWorkflowRaw(workflowsId) =>
      log.debug(s"Find workflows by workflow ids $workflowsId")
      sender ! Try {
        workflowsRaw.filterKeys(key => workflowsId.contains(key)).values.toSeq
          .map(workflow => workflow.copy(status = statuses.get(workflow.id.get)))
      }
    case FindByQueryMemoryWorkflowRaw(query) =>
      log.debug(s"Find workflow by query $query")
      sender ! Try {
        workflowsRaw.find { case (_, workflow) =>
          workflow.name == query.name && workflow.version == query.version.getOrElse(0L) && workflow.group.id.get == query.group.getOrElse(DefaultGroup.id.get)
        }.map { case (_, workflow) =>
          workflow.copy(status = statuses.get(workflow.id.get))
        }.getOrElse(throw new ServerException(s"No workflow with name ${query.name}"))
      }
  }
}

object WorkflowInMemoryApi {

  case class FindMemoryWorkflowRaw(workflowId: String)

  case class FindMemoryWorkflowWithEnv(workflowId: String)

  case object FindAllMemoryWorkflowWithEnv

  case object FindAllMemoryWorkflowRaw

  case object FindAllMemoryWorkflowDto

  case class FindByGroupMemoryWorkflowDto(group: String)

  case class FindByIdsMemoryWorkflowRaw(workflowIds: Seq[String])

  case class FindByQueryMemoryWorkflowRaw(query: WorkflowQuery)

}
