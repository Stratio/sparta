/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.cluster

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}


object SpartaAkkaCluster {

  def startClusterSingletons(props: Seq[(String, Props)])(implicit system: ActorSystem): Seq[ActorRef] =
    props.map { case(name, prop) =>
      system.actorOf(
        ClusterSingletonManager.props(
          singletonProps = prop,
          terminationMessage = PoisonPill,
          settings = ClusterSingletonManagerSettings(system)), name = name)
    }

  def proxyInstanceForName(name: String)(implicit system: ActorSystem): ActorRef =
    system.actorOf(
      ClusterSingletonProxy.props(
        singletonManagerPath = s"/user/$name",
        settings = ClusterSingletonProxySettings(system)
      ))
}
