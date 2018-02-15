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
import com.stratio.sparta.serving.core.actor.WorkflowListenerActor._
import com.stratio.sparta.serving.core.actor.WorkflowStatusListenerActor._
import com.stratio.sparta.serving.core.actor.{WorkflowListenerActor, WorkflowStatusListenerActor}

class LineageService(actorSystem: ActorSystem) extends SLF4JLogging {

  private val WorkflowLineageKey = "workflow-lineage"
  private val WorkflowStatusLineageKey = "workflow-status-lineage"
  private val statusListenerActor = actorSystem.actorOf(Props(new WorkflowStatusListenerActor()))
  private val workflowListenerActor = actorSystem.actorOf(Props(new WorkflowListenerActor()))

  def extractTenantMetadata(): Unit = {

    log.info(s"Sending tenant lineage")
  }

  def extractWorkflowChanges(): Unit = {

    workflowListenerActor ! OnWorkflowsChangesDo(WorkflowLineageKey) { workflow =>

      log.info(s"Sending workflow lineage for workflow: ${workflow.id.get}")
    }
  }

  def extractStatusChanges(): Unit = {

    statusListenerActor ! OnWorkflowStatusesChangeDo(WorkflowStatusLineageKey) { workflowStatus =>

      log.info(s"Sending workflow status lineage for workflowStatus: ${workflowStatus.id}")
    }

  }

  def stopWorkflowChangesExtraction(): Unit =
    workflowListenerActor ! ForgetWorkflowActions(WorkflowLineageKey)

  def stopWorkflowStatusChangesExtraction(): Unit =
    statusListenerActor ! ForgetWorkflowStatusActions(WorkflowStatusLineageKey)



}
