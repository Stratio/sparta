/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import org.apache.curator.framework.recipes.cache.PathChildrenCache

import com.stratio.sparta.serving.core.actor.BusNotification.InitNodeListener
import com.stratio.sparta.serving.core.models.SpartaSerializer

trait ListenerPublisher extends Actor with SpartaSerializer with SLF4JLogging {

  protected var pathCache: Option[PathChildrenCache] = None

  val relativePath : String

  def listenerReceive: Receive = {
    case cd: InitNodeListener => {
      log.info(s"Restarting zkNode Listeners for ${self.toString()}")
      initNodeListener()
    }
    case _ =>
      log.debug(s"Unrecognized message in ${self.toString()}")
  }

  def initNodeListener(): Unit

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[InitNodeListener])
    initNodeListener()
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[InitNodeListener])
    pathCache.foreach(_.close())
  }
}