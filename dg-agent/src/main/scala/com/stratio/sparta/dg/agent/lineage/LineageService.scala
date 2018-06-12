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
import akka.actor.{Actor, ActorRef, ActorSystem, AllForOneStrategy, Props}
import akka.event.slf4j.SLF4JLogging

import com.stratio.governance.commons.agent.actors.PostgresSender
import com.stratio.governance.commons.agent.actors.PostgresSender.PostgresEvent
import com.stratio.sparta.dg.agent.commons.LineageUtils
import com.stratio.sparta.serving.core.actor.StatusListenerActor._
import com.stratio.sparta.serving.core.actor.WorkflowListenerActor._
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.workflow.NodeGraph

class LineageService(statusListenerActor: ActorRef,
                     workflowListenerActor: ActorRef) extends Actor with SLF4JLogging {

  import LineageService._

  override val supervisorStrategy =
    AllForOneStrategy() {
      case t: Exception ⇒ {
        log.error(s"Exception on PostgresSender actor $sender : ${t.getMessage}", t)
        Restart
      }
    }

  implicit val system: ActorSystem = context.system

  val senderPostgres = context.actorOf(Props(new PostgresSender()))

  override def preStart(): Unit = {
    extractTenantMetadata()
    extractWorkflowChanges()
    extractStatusChanges()
  }

  override def receive: Receive = {
    case _ => log.debug("Unrecognized message in LineageService Actor")
  }

  private def extractTenantMetadata(): Unit = {

    log.debug(s"Sending tenant lineage")

    Try(LineageUtils.tenantMetadataLineage()) match {
      case Success(tenantMetadataList) =>
        senderPostgres ! PostgresEvent(tenantMetadataList)
        log.debug("Tenant metadata sent to Kafka")
      case Failure(ex) =>
        log.warn(s"The tenant event couldn't be sent to Postgres. Error was: ${ex.getMessage}")
    }
  }

  def extractWorkflowChanges(): Unit =
    workflowListenerActor ! OnWorkflowsChangesDo(WorkflowLineageKey) { workflow =>
      val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)
      Try(
        LineageUtils.workflowMetadataLineage(workflow) :::
          LineageUtils.inputMetadataLineage(workflow, graph) :::
          LineageUtils.transformationMetadataLineage(workflow, graph) :::
          LineageUtils.outputMetadataLineage(workflow, graph)) match {
        case Success(listSteps) =>
          senderPostgres ! PostgresEvent(listSteps)
          log.debug(s"Sending workflow lineage for workflow: ${workflow.id.get}")
        case Failure(exception) =>
          log.warn(s"Error while generating the metadata related to the workflow steps:${exception.getMessage}")
      }
    }

  def extractStatusChanges(): Unit =
    statusListenerActor ! OnWorkflowStatusesChangeDo(WorkflowStatusLineageKey) { workflowStatusStream =>
      Try(LineageUtils.statusMetadataLineage(workflowStatusStream)) match {
        case Success(maybeList) =>
          maybeList.fold() { listMetadata =>
            senderPostgres ! PostgresEvent(listMetadata)
            log.debug(s"Sending workflow status lineage for workflowStatus: " +
              s"${workflowStatusStream.workflow.get.name}")
          }
        case Failure(exception) =>
          log.warn(s"Error while generating the metadata related to a status event:${exception.getMessage}")
      }
    }

  def stopWorkflowChangesExtraction(): Unit =
    workflowListenerActor ! ForgetWorkflowActions(WorkflowLineageKey)

  def stopWorkflowStatusChangesExtraction(): Unit =
    statusListenerActor ! ForgetWorkflowStatusActions(WorkflowStatusLineageKey)
}

object LineageService {

  def props(statusListenerActor: ActorRef, workflowListenerActor: ActorRef): Props = Props(new LineageService(statusListenerActor, workflowListenerActor))

  val WorkflowLineageKey = "workflow-lineage"
  val WorkflowStatusLineageKey = "workflow-status-lineage"
}
