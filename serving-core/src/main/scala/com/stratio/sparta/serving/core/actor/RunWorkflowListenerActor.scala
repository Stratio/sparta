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
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.utils.SpartaClusterUtils

class RunWorkflowListenerActor(launcherActor: ActorRef)
  extends Actor with SpartaClusterUtils with SpartaSerializer with SLF4JLogging {

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(ClusterTopicRunWorkflow, self)
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(ClusterTopicRunWorkflow, self)
  }

  override def receive: Receive = {
    case RunWorkflowNotification(_, workflowIdExecutionContext) =>
      if (isThisNodeClusterLeader(cluster)) {
        log.debug(s"Running workflow in workflow listener actor: $workflowIdExecutionContext")
        launcherActor ! Launch(workflowIdExecutionContext, None)
      }
  }

}

