/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}
import com.stratio.sparta.serving.core.actor.ListenerPublisher.ClusterTopicNodeListener
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.workflow.WorkflowIdExecutionContext
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.Try

class RunWorkflowPublisherActor() extends ListenerPublisher {

  import RunWorkflowPublisherActor._

  val relativePath: String = AppConstant.BaseZkPath

  override def preStart(): Unit = {
    if (!CuratorFactoryHolder.existsPath(AppConstant.RunWorkflowZkPath))
      CuratorFactoryHolder.getInstance().createContainers(AppConstant.RunWorkflowZkPath)

    mediator ! Subscribe(ClusterTopicNodeListener, self)

    initNodeListener()
  }

  override def postStop(): Unit = {
    log.warn(s"Stopped ExecutionStatusChangePublisherActor at time ${System.currentTimeMillis()}")
  }

  override def initNodeListener(): Unit = {
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[WorkflowIdExecutionContext](new String(eventData.getData))
        } foreach { workflowIdExecutionContext =>
          event.getType match {
            case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
              self ! RunWorkflowNotification(event.getData.getPath, workflowIdExecutionContext)
            case _ => {}
          }
        }
      }
    }
    pathCache = Option(new PathChildrenCache(CuratorFactoryHolder.getInstance(), relativePath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def receive: Receive = executionStatusChangeReceive.orElse(listenerReceive)

  def executionStatusChangeReceive: Receive = {
    case runWorkflowNotification: RunWorkflowNotification =>
      mediator ! Publish(ClusterTopicRunWorkflow, runWorkflowNotification)
  }
}

object RunWorkflowPublisherActor {

  val ClusterTopicRunWorkflow = "RunWorkflow"

  trait Notification

  case class RunWorkflowNotification(path: String, workflowIdExecutionContext: WorkflowIdExecutionContext) extends Notification

}

