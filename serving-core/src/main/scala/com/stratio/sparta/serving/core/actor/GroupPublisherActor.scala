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
import com.stratio.sparta.serving.core.models.workflow.Group

class GroupPublisherActor(curatorFramework: CuratorFramework) extends ListenerPublisher {

  import GroupPublisherActor._

  val relativePath = AppConstant.GroupZkPath

  override def initNodeListener(): Unit = {
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[Group](new String(eventData.getData))
        } foreach { group =>
          event.getType match {
            case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
              self ! GroupChange(event.getData.getPath, group)
            case Type.CHILD_REMOVED =>
              self ! GroupRemove(event.getData.getPath, group)
            case _ => {}
          }
        }
      }
    }
    pathCache = Option(new PathChildrenCache(curatorFramework, relativePath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def receive: Receive = groupReceive.orElse(listenerReceive)

  def groupReceive: Receive = {
    case cd: GroupChange =>
      context.system.eventStream.publish(cd)
    case cd: GroupRemove =>
      context.system.eventStream.publish(cd)
  }
}

object GroupPublisherActor {

  trait Notification

  case class GroupChange(path: String, group: Group) extends Notification

  case class GroupRemove(path: String, group: Group) extends Notification

}