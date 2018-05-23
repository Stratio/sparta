/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.workflow.DebugWorkflow
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Success, Try}

class DebugWorkflowPublisherActor(curatorFramework: CuratorFramework) extends ListenerPublisher {

  import DebugWorkflowPublisherActor._

  override val relativePath = AppConstant.DebugWorkflowZkPath

  override def initNodeListener(): Unit = {
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        Try {
          read[DebugWorkflow](new String(event.getData.getData))
        } match {
          case Success(debugWorkflow) =>
            event.getType match {
              case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
                self ! DebugWorkflowChange(event.getData.getPath, debugWorkflow)
              case Type.CHILD_REMOVED =>
                self ! DebugWorkflowRemove(event.getData.getPath, debugWorkflow)
              case _ =>
            }
          case Failure(_) =>
        }
      }
    }
    pathCache = Option(new PathChildrenCache(curatorFramework, relativePath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def receive: Receive = debugWorkflowReceive.orElse(listenerReceive)

  def debugWorkflowReceive: Receive = {
    case cd: DebugWorkflowChange =>
      context.system.eventStream.publish(cd)
    case cd: DebugWorkflowRemove =>
      context.system.eventStream.publish(cd)
  }
}

object DebugWorkflowPublisherActor {

  trait Notification

  case class DebugWorkflowChange(path: String, debugWorkflow: DebugWorkflow) extends Notification

  case class DebugWorkflowRemove(path: String, debugWorkflow: DebugWorkflow) extends Notification

}
