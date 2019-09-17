/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, AllForOneStrategy, Props, SupervisorStrategy}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.helpers.WorkflowHelper

// TODO ALVARO Delete this class when possible
class TopLevelSupervisorActor extends Actor with SLF4JLogging {

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  override def receive: Receive = {
    case _ => log.debug("Unsupported message received in TopLevelSupervisorActor")
  }

  override def supervisorStrategy: SupervisorStrategy = AllForOneStrategy() {
    case _ => Restart
  }

  override def postStop(): Unit = {
    log.warn(s"Stopped TopLevelSupervisorActor at time ${System.currentTimeMillis()}")
  }

  override def preStart(): Unit = {
    //Initialize Nginx actor
    if (WorkflowHelper.isMarathonLBConfigured) {
      log.info("Initializing Nginx service")
      Option(context.actorOf(Props(new NginxActor())))
    }

  }
}