/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.dg.agent.lineage

import scala.util.{Failure, Success, Try}
import scalax.collection.Graph
import scalax.collection.edge.LDiEdge

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorRef, AllForOneStrategy, Props}
import akka.cluster.Cluster
import akka.event.slf4j.SLF4JLogging

import com.stratio.governance.commons.agent.actors.MetadataComparator.{FailedMetadata, RemovedMetadata, StoredMetadata}
import com.stratio.governance.commons.agent.actors.PostgresSender
import com.stratio.governance.commons.agent.actors.PostgresSender.{EndSending, NewEvent}
import com.stratio.sparta.dg.agent.commons.LineageUtils
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangeListenerActor.{ForgetExecutionStatusActions, OnExecutionStatusesChangeDo}
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, WorkflowExecutionStatusChange}
import com.stratio.sparta.serving.core.utils.SpartaClusterUtils

class LineageService(executionStatusChangeListenerActor: ActorRef) extends Actor with SpartaClusterUtils with SLF4JLogging {

  import LineageService._

  val cluster = Cluster(context.system)

  override val supervisorStrategy =
    AllForOneStrategy() {
      case t: Exception ⇒ {
        log.error(s"Exception on Lineage PostgresSender actor $sender : ${t.getMessage}", t)
        Restart
      }
    }

  implicit val system = context.system

  val senderPostgres = context.actorOf(Props(new PostgresSender(AppConstant.ConfigLineage)))

  override def preStart(): Unit = {
    extractTenantMetadata()
    extractStatusChanges()
  }

  override def receive: Receive = {
    case m: StoredMetadata => log.debug(s"Receiving StoredMetadata message from lineage ${m.newMetadata.mkString(",")}")
    case m: RemovedMetadata => log.debug(s"Receiving RemovedMetadata message from lineage ${m.removedMetadata.mkString(",")}")
    case m: FailedMetadata => log.error(s"Receiving FailedMetadata message from lineage ${m.failedMetadata.mkString(",")}")
    case _ => log.error("Unrecognized message in LineageService Actor")
  }

  private def extractTenantMetadata(): Unit = {
    if (isThisNodeClusterLeader(cluster)) {
      log.debug(s"Sending tenant lineage")
      Try(LineageUtils.tenantMetadataLineage()) match {
        case Success(tenantMetadataList) =>
          senderPostgres ! NewEvent(tenantMetadataList)
          senderPostgres ! EndSending
        case Failure(ex) =>
          log.warn(s"The tenant event couldn't be sent to Postgres. Error was: ${ex.getMessage}")
      }
    }
  }

  def extractStatusChanges(): Unit =
    executionStatusChangeListenerActor ! OnExecutionStatusesChangeDo(ExecutionStatusLineageKey) { executionStatusChange =>
      extractWorkflowChanges(executionStatusChange)
      Try(LineageUtils.executionStatusMetadataLineage(executionStatusChange)) match {
        case Success(maybeList) =>
          maybeList.fold() { listMetadata =>
            senderPostgres ! NewEvent(listMetadata)
            log.debug(s"Sending lineage for execution: " + s"${executionStatusChange.newExecution.id.get}")
          }
        case Failure(exception) =>
          log.warn(s"Error while generating the metadata related to a status event:${exception.getMessage}")
      }
      senderPostgres ! EndSending
    }

  private def extractWorkflowChanges(executionStatusChange: WorkflowExecutionStatusChange): Unit = {
    if (executionStatusChange.newExecution.lastStatus.state == WorkflowStatusEnum.Created ||
      executionStatusChange.newExecution.lastStatus.state == WorkflowStatusEnum.Starting) {

      val workflow = executionStatusChange.newExecution.getWorkflowToExecute
      val executionId = executionStatusChange.newExecution.getExecutionId
      val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)
      Try(
        LineageUtils.workflowMetadataLineage(workflow, executionId) :::
          LineageUtils.inputMetadataLineage(workflow, graph, executionId) :::
          LineageUtils.transformationMetadataLineage(workflow, graph, executionId) :::
          LineageUtils.outputMetadataLineage(workflow, graph, executionId)) match {
        case Success(listSteps) =>
          senderPostgres ! NewEvent(listSteps)
          log.debug(s"Sending workflow lineage for workflow: ${workflow.id.get}")
        case Failure(exception) =>
          log.warn(s"Error while generating the metadata related to the workflow steps:${exception.getMessage}")
      }
    }
  }

  def stopWorkflowStatusChangesExtraction(): Unit =
    executionStatusChangeListenerActor ! ForgetExecutionStatusActions(ExecutionStatusLineageKey)
}

object LineageService {

  def props(executionListenerActor: ActorRef): Props = Props(new LineageService(executionListenerActor))

  val WorkflowLineageKey = "workflow-lineage"
  val ExecutionStatusLineageKey = "execution-status-lineage"
}
