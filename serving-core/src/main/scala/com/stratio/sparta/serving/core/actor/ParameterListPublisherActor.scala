/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.parameters.ParameterList
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.Try

class ParameterListPublisherActor(curatorFramework: CuratorFramework)
  extends ListenerPublisher {

  import ParameterListPublisherActor._

  val relativePath = AppConstant.ParameterListZkPath

  override def initNodeListener(): Unit = {
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[ParameterList](new String(eventData.getData))
        } foreach { parameterList =>
          event.getType match {
            case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
              self ! ParameterListChange(event.getData.getPath, parameterList)
            case Type.CHILD_REMOVED =>
              self ! ParameterListRemove(event.getData.getPath, parameterList)
            case _ => {}
          }
        }
      }
    }
    pathCache = Option(new PathChildrenCache(curatorFramework, relativePath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def receive: Receive = executionReceive.orElse(listenerReceive)

  def executionReceive: Receive = {
    case cd: ParameterListChange =>
      context.system.eventStream.publish(cd)
    case cd: ParameterListRemove =>
      context.system.eventStream.publish(cd)
  }
}

object ParameterListPublisherActor {

  trait Notification

  case class ParameterListChange(path: String, parameterList: ParameterList) extends Notification

  case class ParameterListRemove(path: String, parameterList: ParameterList) extends Notification

}

