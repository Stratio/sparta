/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, ActorRef}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.actor.RunWorkflowPublisherActor._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.{HeaderAuthUser, LoggedUser}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.WorkflowIdExecutionContext
import com.stratio.sparta.serving.core.utils.SpartaClusterUtils

import scala.concurrent.Future

class RunWorkflowListenerActor(launcherActor: ActorRef)
  extends Actor with SpartaClusterUtils with SpartaSerializer with SLF4JLogging {

  implicit val executionContext: scala.concurrent.ExecutionContext = context.dispatcher

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  lazy val executionPgService = PostgresDaoFactory.executionPgService

  override def preStart(): Unit = {
    mediator ! Subscribe(ClusterTopicRunWorkflow, self)
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(ClusterTopicRunWorkflow, self)
  }

  override def receive: Receive = {
    case RunWorkflowNotification(_, workflowIdExecutionContext) =>
      if (isThisNodeClusterLeader(cluster)) {
        if(workflowIdExecutionContext.executionSettings.forall(settings => settings.uniqueInstance.forall(unique => !unique))) {
          log.debug(s"Executing run workflow without unique instance from execution context: $workflowIdExecutionContext")
          launchWorkflowWithLauncher(workflowIdExecutionContext)
        } else {
          executionPgService.otherWorkflowInstanceRunning(
            workflowIdExecutionContext.workflowId,
            workflowIdExecutionContext.executionContext
          ).onSuccess{ case result =>
            if(!result) {
              log.debug(s"Executing run workflow preventing unique instance from execution context: $workflowIdExecutionContext")
              launchWorkflowWithLauncher(workflowIdExecutionContext)
            } else {
              log.info(s"There are other workflow instance running with the same " +
                s"id ${workflowIdExecutionContext.workflowId} and " +
                s"execution context ${workflowIdExecutionContext.executionContext}, aborting workflow run")
            }
          }
        }
      }
  }

  def launchWorkflowWithLauncher(workflowIdExecutionContext: WorkflowIdExecutionContext): Unit = {
    log.debug(s"Running workflow in workflow listener actor: $workflowIdExecutionContext")
    launcherActor ! Launch(
      workflowIdExecutionContext = workflowIdExecutionContext,
      user = workflowIdExecutionContext.executionSettings.flatMap(_.userId.map(user =>
        HeaderAuthUser(user, user).asInstanceOf[LoggedUser]
      ))
    )
  }

}

