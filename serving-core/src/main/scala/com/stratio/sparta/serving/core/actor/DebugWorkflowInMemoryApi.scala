/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.sdk.workflow.step.DebugResults
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor._
import com.stratio.sparta.serving.core.actor.DebugWorkflowPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServerException

import scala.util.Try

class DebugWorkflowInMemoryApi extends InMemoryServicesStatus{

  import DebugWorkflowInMemoryApi._

  override def persistenceId: String = AkkaConstant.DebugWorkflowApiActorName

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[WorkflowChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.subscribe(self, classOf[DebugWorkflowChange])
    context.system.eventStream.subscribe(self, classOf[DebugWorkflowRemove])
  }

  override def postStop(): Unit = {
    //@TODO[fl] ask if is possible to trigger the update of WorkflowDebug when the WorkflowChanges
    context.system.eventStream.unsubscribe(self, classOf[WorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRemove])
    context.system.eventStream.unsubscribe(self, classOf[DebugWorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[DebugWorkflowRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  //scalastyle:off
  def apiReceive: Receive = {
    case FindAllMemoryDebugWorkflows =>
      log.debug(s"Find all debug workflows")
      sender ! Try {
        debugWorkflows.values.toSeq
      }
    case FindMemoryDebugWorkflow(debugWorkflowId) =>
      log.debug(s"Find debug workflow by id $debugWorkflowId")
      sender ! Try {
        debugWorkflows.getOrElse(
          debugWorkflowId,
          throw new ServerException(s"No debug workflow with id $debugWorkflowId")
        )
      }
    case FindMemoryDebugResultsWorkflow(debugWorkflowId) =>
      log.debug(s"Find workflow debug results by id $debugWorkflowId")
      val res: Try[DebugResults] = Try {
        val debug = debugWorkflows.getOrElse(
          debugWorkflowId,
          throw new ServerException(s"No debug workflow with id $debugWorkflowId")
        )
        if(debug.result.isDefined) debug.result.get
        else throw new ServerException(s"No debug results for workflow with id $debugWorkflowId")
      }

      sender ! res
  }
}

object DebugWorkflowInMemoryApi {

  case class FindMemoryDebugWorkflow(debugWorkflowId: String)

  case class FindMemoryDebugResultsWorkflow(debugWorkflowId: String)

  case object FindAllMemoryDebugWorkflows

}


