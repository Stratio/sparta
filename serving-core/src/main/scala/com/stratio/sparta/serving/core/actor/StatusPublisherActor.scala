/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import scala.util.Try

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus

class StatusPublisherActor(curatorFramework: CuratorFramework) extends ListenerPublisher {

  import StatusPublisherActor._

  override val relativePath = AppConstant.WorkflowStatusesZkPath

  override def initNodeListener(): Unit = {
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        Try {
          read[WorkflowStatus](new String(event.getData.getData))
        } foreach { status =>
          event.getType match {
            case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
              self ! StatusChange(event.getData.getPath, status)
            case Type.CHILD_REMOVED =>
              self ! StatusRemove(event.getData.getPath, status)
            case _ => {}
          }
        }
      }
    }
    pathCache = Option(new PathChildrenCache(curatorFramework, relativePath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def receive: Receive = statusReceive.orElse(listenerReceive)

  def statusReceive: Receive = {
    case cd: StatusChange =>
      context.system.eventStream.publish(cd)
    case cd: StatusRemove =>
      context.system.eventStream.publish(cd)
  }

}


object StatusPublisherActor {

  trait Notification

  case class StatusChange(path: String, workflowStatus: WorkflowStatus) extends Notification

  case class StatusRemove(path: String, workflowStatus: WorkflowStatus) extends Notification

}