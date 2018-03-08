/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowExecution}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.Try

class ExecutionPublisherActor(curatorFramework: CuratorFramework)
  extends Actor with SpartaSerializer with SLF4JLogging {

  import ExecutionPublisherActor._

  private var pathCache: Option[PathChildrenCache] = None

  override def preStart(): Unit = {
    val executionsPath = AppConstant.WorkflowExecutionsZkPath
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[WorkflowExecution](new String(eventData.getData))
        } foreach { execution =>
          event.getType match {
            case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
              self ! ExecutionChange(event.getData.getPath, execution)
            case Type.CHILD_REMOVED =>
              self ! ExecutionRemove(event.getData.getPath, execution)
            case _ => {}
          }
        }
      }
    }

    pathCache = Option(new PathChildrenCache(curatorFramework, executionsPath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def postStop(): Unit =
    pathCache.foreach(_.close())

  override def receive: Receive = {
    case cd: ExecutionChange =>
      context.system.eventStream.publish(cd)
    case cd: ExecutionRemove =>
      context.system.eventStream.publish(cd)
    case _ =>
      log.debug("Unrecognized message in Workflow Execution Publisher Actor")
  }

}

object ExecutionPublisherActor {

  trait Notification

  case class ExecutionChange(path: String, execution: WorkflowExecution) extends Notification

  case class ExecutionRemove(path: String, execution: WorkflowExecution) extends Notification

}