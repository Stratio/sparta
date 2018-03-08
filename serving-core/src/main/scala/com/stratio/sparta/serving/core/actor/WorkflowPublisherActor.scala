/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.Workflow
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.Try

class WorkflowPublisherActor(
                              curatorFramework: CuratorFramework,
                              override val serializerSystem: Option[ActorSystem] = None,
                              override val environmentStateActor: Option[ActorRef] = None
                            ) extends Actor with SpartaSerializer with SLF4JLogging {

  import WorkflowPublisherActor._

  private var pathCache: Option[PathChildrenCache] = None

  override def preStart(): Unit = {
    val workflowsPath = AppConstant.WorkflowsZkPath
    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[Workflow](new String(eventData.getData))
        } foreach { workflow =>
          event.getType match {
            case Type.CHILD_ADDED | Type.CHILD_UPDATED =>
              if(serializerSystem.isDefined && environmentStateActor.isDefined)
                self ! WorkflowChange(event.getData.getPath, workflow)
              else self ! WorkflowRawChange(event.getData.getPath, workflow)
            case Type.CHILD_REMOVED =>
              if(serializerSystem.isDefined && environmentStateActor.isDefined)
                self ! WorkflowRemove(event.getData.getPath, workflow)
              else self ! WorkflowRawRemove(event.getData.getPath, workflow)
            case _ => {}
          }
        }
      }
    }

    pathCache = Option(new PathChildrenCache(curatorFramework, workflowsPath, true))
    pathCache.foreach(_.getListenable.addListener(nodeListener, context.dispatcher))
    pathCache.foreach(_.start())
  }

  override def postStop(): Unit =
    pathCache.foreach(_.close())

  override def receive: Receive = {
    case cd: WorkflowChange =>
      context.system.eventStream.publish(cd)
    case cd: WorkflowRemove =>
      context.system.eventStream.publish(cd)
    case cd: WorkflowRawChange =>
      context.system.eventStream.publish(cd)
    case cd: WorkflowRawRemove =>
      context.system.eventStream.publish(cd)
    case _ =>
      log.debug("Unrecognized message in Workflow Publisher Actor")
  }

}

object WorkflowPublisherActor {

  trait Notification

  case class WorkflowChange(path: String, workflow: Workflow) extends Notification

  case class WorkflowRawChange(path: String, workflow: Workflow) extends Notification

  case class WorkflowRemove(path: String, workflow: Workflow) extends Notification

  case class WorkflowRawRemove(path: String, workflow: Workflow) extends Notification

}