/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.utils

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}

object AkkaClusterUtils {

  case class ActorSingletonInfo private(name: String, props: Props, role: Option[String])

  object ActorSingletonInfo{
    def apply(name: String, props: Props, role: String): ActorSingletonInfo = ActorSingletonInfo(name, props, Option(role))
    def apply(name: String, props: Props): ActorSingletonInfo = ActorSingletonInfo(name, props, None)
  }

  def startClusterSingletons(props: Seq[ActorSingletonInfo])(implicit system: ActorSystem): Seq[(String, ActorRef)] =
    props.map { actorSingletonInfo =>
      (
        actorSingletonInfo.name,
        system.actorOf(
          ClusterSingletonManager.props(
            singletonProps = actorSingletonInfo.props,
            terminationMessage = PoisonPill,
            settings = ClusterSingletonManagerSettings(system).withRole(actorSingletonInfo.role)), name = actorSingletonInfo.name)
      )
    }

  def proxyInstanceForName(name: String)(implicit system: ActorSystem): ActorRef =
    proxyInstanceForName(name, None)

  def proxyInstanceForName(name: String, role: String)(implicit system: ActorSystem): ActorRef =
    proxyInstanceForName(name, Option(role))

  def proxyInstanceForName(name: String, role: Option[String])(implicit system: ActorSystem): ActorRef =
    system.actorOf(
      ClusterSingletonProxy.props(
        singletonManagerPath = s"/user/$name",
        settings = ClusterSingletonProxySettings(system).withRole(role)
      ))
}
