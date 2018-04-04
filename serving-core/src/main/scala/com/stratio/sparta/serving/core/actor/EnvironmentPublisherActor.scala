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
import com.stratio.sparta.serving.core.models.env.Environment

class EnvironmentPublisherActor(curatorFramework: CuratorFramework)
  extends ListenerPublisher {

  import EnvironmentPublisherActor._

  val relativePath = AppConstant.BaseZkPath

  override def initNodeListener(): Unit = {
    val nodeListener1 = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[Environment](new String(eventData.getData))
        } foreach { environment =>
          event.getType match {
            case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
              self ! EnvironmentChange(event.getData.getPath, environment)
            case Type.CHILD_REMOVED =>
              self ! EnvironmentRemove(event.getData.getPath, environment)
            case _ => {}
          }
        }
      }
    }
    pathCache = Option(new PathChildrenCache(curatorFramework, relativePath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener1, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def receive: Receive = environmentReceive.orElse(listenerReceive)

  def environmentReceive: Receive = {
    case cd: EnvironmentChange =>
      context.system.eventStream.publish(cd)
    case cd: EnvironmentRemove =>
      context.system.eventStream.publish(cd)
  }
}

object EnvironmentPublisherActor {

  trait Notification

  case class EnvironmentChange(path: String, environment: Environment) extends Notification

  case class EnvironmentRemove(path: String, environment: Environment) extends Notification

}