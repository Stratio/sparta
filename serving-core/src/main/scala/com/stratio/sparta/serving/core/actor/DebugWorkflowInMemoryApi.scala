/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.sdk.models.WorkflowError
import com.stratio.sparta.sdk.workflow.step.{DebugResults, ResultStep}
import com.stratio.sparta.serving.core.actor.DebugStepDataPublisherActor._
import com.stratio.sparta.serving.core.actor.DebugStepErrorPublisherActor._
import com.stratio.sparta.serving.core.actor.DebugWorkflowPublisherActor._
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow.DebugWorkflow

import scala.util.Try

class DebugWorkflowInMemoryApi extends InMemoryServicesStatus {

  import DebugWorkflowInMemoryApi._

  override def persistenceId: String = AkkaConstant.DebugWorkflowApiActorName

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[DebugWorkflowChange])
    context.system.eventStream.subscribe(self, classOf[DebugStepDataChange])
    context.system.eventStream.subscribe(self, classOf[DebugStepErrorChange])
    context.system.eventStream.subscribe(self, classOf[DebugWorkflowRemove])
    context.system.eventStream.subscribe(self, classOf[DebugStepDataRemove])
    context.system.eventStream.subscribe(self, classOf[DebugStepErrorRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[DebugWorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[DebugStepDataChange])
    context.system.eventStream.unsubscribe(self, classOf[DebugStepErrorChange])
    context.system.eventStream.unsubscribe(self, classOf[DebugWorkflowRemove])
    context.system.eventStream.unsubscribe(self, classOf[DebugStepDataRemove])
    context.system.eventStream.unsubscribe(self, classOf[DebugStepErrorRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def apiReceive: Receive = {
    case FindAllMemoryDebugWorkflows =>
      log.debug(s"Find all debug workflows")
      sender ! Try {
        debugWorkflows.map { case (id, debugWorkflow) =>
          id -> getDebugWorkflowWithStep(id, debugWorkflow)
        }.values.toSeq
      }
    case FindMemoryDebugWorkflow(debugWorkflowId) =>
      log.debug(s"Find debug workflow by id $debugWorkflowId")
      sender ! Try {
        debugWorkflows.get(debugWorkflowId) match {
          case Some(debugWorkflow) =>
            getDebugWorkflowWithStep(debugWorkflowId, debugWorkflow)
          case None =>
            throw new ServerException(s"No debug workflow with id $debugWorkflowId")
        }
      }
    case FindMemoryDebugResultsWorkflow(debugWorkflowId) =>
      log.debug(s"Find workflow debug results by id $debugWorkflowId")
      val res: Try[DebugResults] = Try {
        val debug = debugWorkflows.get(debugWorkflowId) match {
          case Some(debugWorkflow) =>
            getDebugWorkflowWithStep(debugWorkflowId, debugWorkflow)
          case None =>
            throw new ServerException(s"No debug workflow with id $debugWorkflowId")
        }
        if (debug.result.isDefined) debug.result.get
        else throw new ServerException(s"No debug results for workflow with id $debugWorkflowId")
      }

      sender ! res
  }

  private def getDebugWorkflowWithStep(id: String, debugWorkflow: DebugWorkflow): DebugWorkflow = {
    val stepDataResults = stepDataResultsFromId(id)
    val stepErrorResults = stepErrorResultsFromId(id)

    debugWorkflow.copy(
      result = debugWorkflow.result.map { results =>
        results.copy(stepResults = stepDataResults, stepErrors = stepErrorResults)
      }
    )
  }

  private def stepDataResultsFromId(id: String): Map[String, ResultStep] =
    debugStepData
      .filter { case (key, _) => key.contains(id) }
      .toMap
      .map { case (_, value) => value.step -> value }

  private def stepErrorResultsFromId(id: String): Map[String, WorkflowError] =
    debugStepError
      .filter { case (key, _) => key.contains(id) }
      .toMap
      .map { case (key, value) => value.step.getOrElse(key.substring(key.indexOf("-"))) -> value }
}

object DebugWorkflowInMemoryApi {

  case class FindMemoryDebugWorkflow(debugWorkflowId: String)

  case class FindMemoryDebugResultsWorkflow(debugWorkflowId: String)

  case object FindAllMemoryDebugWorkflows

}


