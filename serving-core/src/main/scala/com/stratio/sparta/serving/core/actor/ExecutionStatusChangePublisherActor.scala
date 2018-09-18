/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecutionStatusChange
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.Try

class ExecutionStatusChangePublisherActor() extends ListenerPublisher {

  import ExecutionStatusChangePublisherActor._

  val relativePath: String = AppConstant.BaseZkPath

  override def initNodeListener(): Unit = {
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[WorkflowExecutionStatusChange](new String(eventData.getData))
        } foreach { executionStatusChange =>
          event.getType match {
            case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
              self ! ExecutionStatusChange(event.getData.getPath, executionStatusChange)
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
    case executionStatusChange: ExecutionStatusChange =>
      context.system.eventStream.publish(executionStatusChange)
  }
}

object ExecutionStatusChangePublisherActor {

  trait Notification

  case class ExecutionStatusChange(path: String, executionChange: WorkflowExecutionStatusChange) extends Notification

}