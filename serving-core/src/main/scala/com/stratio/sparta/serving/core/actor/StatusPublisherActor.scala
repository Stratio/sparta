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
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.Try

class StatusPublisherActor(curatorFramework: CuratorFramework) extends Actor with SpartaSerializer with SLF4JLogging {

  import StatusPublisherActor._

  private var pathCache: Option[PathChildrenCache] = None

  override def preStart(): Unit = {
    val statusesPath = AppConstant.WorkflowStatusesZkPath
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

    pathCache = Option(new PathChildrenCache(curatorFramework, statusesPath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def postStop(): Unit =
    pathCache.foreach(_.close())

  override def receive: Receive = {
    case cd: StatusChange =>
      context.system.eventStream.publish(cd)
    case cd: StatusRemove =>
      context.system.eventStream.publish(cd)
    case _ =>
      log.debug("Unrecognized message in Workflow Status Publisher Actor")
  }

}


object StatusPublisherActor {

  trait Notification

  case class StatusChange(path: String, workflowStatus: WorkflowStatus) extends Notification

  case class StatusRemove(path: String, workflowStatus: WorkflowStatus) extends Notification

}