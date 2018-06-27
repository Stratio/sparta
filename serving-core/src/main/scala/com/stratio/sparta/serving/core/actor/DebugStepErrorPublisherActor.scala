/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.core.models.{ResultStep, WorkflowError}
import com.stratio.sparta.serving.core.constants.AppConstant
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Success, Try}

class DebugStepErrorPublisherActor(curatorFramework: CuratorFramework) extends ListenerPublisher {

  import DebugStepErrorPublisherActor._

  override val relativePath = AppConstant.DebugStepErrorZkPath

  override def initNodeListener(): Unit = {
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[WorkflowError](new String(eventData.getData))
        } match {
          case Success(debugStepError) =>
            event.getType match {
              case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
                self ! DebugStepErrorChange(event.getData.getPath, debugStepError)
              case Type.CHILD_REMOVED =>
                self ! DebugStepErrorRemove(event.getData.getPath, debugStepError)
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
    case cd: DebugStepErrorChange =>
      context.system.eventStream.publish(cd)
    case cd: DebugStepErrorRemove =>
      context.system.eventStream.publish(cd)
  }
}

object DebugStepErrorPublisherActor {

  trait Notification

  case class DebugStepErrorChange(path: String, debugStepError: WorkflowError) extends Notification

  case class DebugStepErrorRemove(path: String, debugStepError: WorkflowError) extends Notification

}



