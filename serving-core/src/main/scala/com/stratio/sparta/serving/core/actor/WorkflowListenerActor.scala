/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor.{WorkflowChange, WorkflowRemove}
import com.stratio.sparta.serving.core.models.workflow.Workflow

import scala.concurrent.{Future, blocking}

class WorkflowListenerActor extends Actor with SLF4JLogging {

  import WorkflowListenerActor._

  private val workflowActions = scala.collection.mutable.Map[String, List[WorkflowChangeAction]]()
  private val genericActions = scala.collection.mutable.Map[String, List[WorkflowChangeAction]]()

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[WorkflowChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[WorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRemove])
  }

  override def receive: Receive = {
    case request@OnWorkflowChangeDo(id) =>
      workflowActions += ((id, request.action :: workflowActions.getOrElse(id, Nil)))
    case request@OnWorkflowsChangesDo(key) =>
      genericActions += ((key, request.action :: workflowActions.getOrElse(key, Nil)))
    case ForgetWorkflowActions(id) =>
      workflowActions -= id
      genericActions -= id
    case WorkflowChange(_, workflow) =>
      workflowActions.getOrElse(workflow.id.getOrElse(""), Nil) foreach { callback =>
        Future {
          try {
            blocking(callback(workflow))
          } catch {
            case e: Exception =>
              log.error(s"Error executing action for workflow ${workflow.name}." +
                s" With exception: ${e.getLocalizedMessage}")
          }
        }(context.dispatcher)
      }
      genericActions.foreach { case (_, actions) =>
        actions.foreach { callback =>
          Future {
            try {
              blocking(callback(workflow))
            } catch {
              case e: Exception => log.error(s"Error executing action for workflow ${workflow.name}." +
                s" With exception: ${e.getLocalizedMessage}")
            }
          }(context.dispatcher)
        }
      }
    case WorkflowRemove(_, workflow) =>
      workflow.id.foreach(id => workflowActions -= id)
    case _ => log.debug("Unrecognized message in Workflow Listener Actor")
  }

}

object WorkflowListenerActor {

  type WorkflowChangeAction = Workflow => Unit

  case class OnWorkflowChangeDo(workflowId: String)(val action: WorkflowChangeAction)

  case class OnWorkflowsChangesDo(key: String)(val action: WorkflowChangeAction)

  case class ForgetWorkflowActions(id: String)

}