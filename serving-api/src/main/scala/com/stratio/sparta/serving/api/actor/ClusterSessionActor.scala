/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import com.stratio.sparta.serving.api.actor.ClusterSessionActor._
import com.stratio.sparta.serving.api.oauth.SessionStore

class ClusterSessionActor extends Actor {

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    mediator ! Subscribe(ClusterTopicSession, self)
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(ClusterTopicSession, self)
  }

  override def receive: Receive = {
    case newSession: NewSession =>
      SessionStore.addSession(newSession)
    case removeSession: RemoveSession =>
      SessionStore.removeSession(removeSession)
    case PublishSessionInCluster(newSession) =>
      mediator ! Publish(ClusterTopicSession, newSession)
    case PublishRemoveSessionInCluster(sessionToRemove) =>
      mediator ! Publish(ClusterTopicSession, sessionToRemove)
  }

}

object ClusterSessionActor {

  val ClusterTopicSession = "Session"

  trait Notification

  case class NewSession(sessionId: String, identity: String, expires: Long) extends Notification

  case class RemoveSession(sessionId: String) extends Notification

  case class PublishSessionInCluster(newSession: NewSession)

  case class PublishRemoveSessionInCluster(sessionToRemove: RemoveSession)

}
