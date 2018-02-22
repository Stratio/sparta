/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.dg.agent.lineage

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import com.stratio.governance.commons.agent.actors.KafkaSender
import com.stratio.governance.commons.agent.actors.KafkaSender.KafkaEvent
import com.stratio.sparta.dg.agent.commons.LineageUtils
import com.stratio.sparta.serving.core.actor.WorkflowListenerActor._
import com.stratio.sparta.serving.core.actor.WorkflowStatusListenerActor._
import com.stratio.sparta.serving.core.actor.{WorkflowListenerActor, WorkflowStatusListenerActor}
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.workflow.NodeGraph
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}
import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge

class LineageService(implicit val actorSystem: ActorSystem) extends SLF4JLogging {

  private val WorkflowLineageKey = "workflow-lineage"
  private val WorkflowStatusLineageKey = "workflow-status-lineage"
  private val statusListenerActor = actorSystem.actorOf(Props(new WorkflowStatusListenerActor()))
  private val workflowListenerActor = actorSystem.actorOf(Props(new WorkflowListenerActor()))
  private val senderKafka = actorSystem.actorOf(Props(new KafkaSender()))
  private val config = ConfigFactory.load
  private val topicKafka = Try(config.getString("sender.topic")).getOrElse("dg-metadata")

  def extractTenantMetadata(): Unit = {

    log.debug(s"Sending tenant lineage")

    Try(LineageUtils.tenantMetadataLineage()) match {
      case Success(tenantMetadataList) =>
        senderKafka ! KafkaEvent(tenantMetadataList, topicKafka)
        log.debug("Tenant metadata sent to Kafka")
      case Failure(ex) =>
        log.warn(s"The tenant event couldn't be sent to Kafka. Error was: ${ex.getMessage}")
    }
  }

  def extractWorkflowChanges(): Unit =
    workflowListenerActor ! OnWorkflowsChangesDo(WorkflowLineageKey) { workflow =>
      val graph: Graph[NodeGraph, DiEdge] = GraphHelper.createGraph(workflow)
      Try(
        LineageUtils.workflowMetadataLineage(workflow) :::
        LineageUtils.inputMetadataLineage(workflow, graph) :::
        LineageUtils.transformationMetadataLineage(workflow, graph) :::
        LineageUtils.outputMetadataLineage(workflow, graph)) match {
        case Success(listSteps) =>
          senderKafka ! KafkaEvent(listSteps, topicKafka)
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
            senderKafka ! KafkaEvent(listMetadata, topicKafka)
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