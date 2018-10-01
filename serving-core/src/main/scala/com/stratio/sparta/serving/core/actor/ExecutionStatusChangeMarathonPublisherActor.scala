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

import com.stratio.sparta.serving.core.actor.BusNotification.InitNodeListener
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecutionStatusChange

class ExecutionStatusChangeMarathonPublisherActor extends MarathonListenerPublisher {

  import ExecutionStatusChangePublisherActor._

  val relativePath: String = AppConstant.BaseZkPath

  override def preStart(): Unit = {
    if(!CuratorFactoryHolder.existsPath(AppConstant.ExecutionsStatusChangesZkPath))
      CuratorFactoryHolder.getInstance().createContainers(AppConstant.ExecutionsStatusChangesZkPath)

    context.system.eventStream.subscribe(self, classOf[InitNodeListener])

    initNodeListener()
  }

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