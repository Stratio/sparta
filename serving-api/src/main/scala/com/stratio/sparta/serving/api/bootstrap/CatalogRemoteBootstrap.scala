/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.bootstrap

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor._
import com.stratio.sparta.serving.api.actor.remote.CatalogWorkerActor
import com.stratio.sparta.serving.core.constants.AkkaConstant._

case class CatalogRemoteBootstrap(title: String)(implicit system: ActorSystem) extends Bootstrap
  with SLF4JLogging {

  def start: Unit = {
    log.info(s"# Bootstraping $title #")
    initAkkaCatalog()
  }

  protected[bootstrap] def initAkkaCatalog(): Unit = {
    system.actorOf(Props[SpartaClusterNodeActor], "clusterNode")
    Cluster(system) registerOnMemberUp {
      system.actorOf(CatalogWorkerActor.props, CatalogWorkerActorName)
    }

  }
}
