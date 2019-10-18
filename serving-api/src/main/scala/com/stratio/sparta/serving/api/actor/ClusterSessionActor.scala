/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef}
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import akka.cluster.ddata.Replicator._
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.ClusterSessionActor._

import scala.concurrent.duration._

class ClusterSessionActor extends Actor with SLF4JLogging {

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  implicit val cluster = Cluster(context.system)

  val replicator = DistributedData(context.system).replicator

  override def receive: Receive = {
    case GetSession(sessionId) =>
      replicator ! Get(dataKey(sessionId), ReadLocal, Some(SessionContext(sessionId, sender())))

    case session@GetSuccess(_, Some(SessionContext(sessionId, replyTo))) =>
      val maybeInfo: Option[SessionInfo] = session.dataValue match {
        case sessionsMap: LWWMap[String, SessionInfo] => sessionsMap.get(sessionId)
        case _ => None
      }
      replyTo ! maybeInfo

    case GetFailure(_, Some(SessionContext(_, replyTo))) =>
      log.debug("Failed to get session ID")
      replyTo ! None

    case NotFound(_, Some(SessionContext(_, replyTo))) =>
      log.trace("Session ID not found")
      replyTo ! None

    case UpdateSuccess(_, Some(SessionContext(sessionId, replyTo))) =>
      replyTo ! NewSessionCreated(sessionId)

    case UpdateTimeout(_, Some(SessionContext(_, replyTo))) =>
      log.debug("Update session ID timeout")
      replyTo ! None

    case NewSession(sessionId, identity, expires) =>
      replicator ! Update(dataKey(sessionId), LWWMap(), ClusterSessionActor.writeLocal, Some(SessionContext(sessionId, sender()))) {
        _ + (sessionId -> SessionInfo(identity, expires))
      }

    case RefreshSession(sessionId, identity, expires) =>
      replicator ! Update(dataKey(sessionId), LWWMap(), ClusterSessionActor.writeLocal) {
        _ + (sessionId -> SessionInfo(identity, expires))
      }

    case RemoveSession(sessionId) =>
      replicator ! Delete(dataKey(sessionId), ClusterSessionActor.writeLocal)

  }

  def dataKey(key: String): LWWMapKey[String, SessionInfo] = LWWMapKey(key)

}

object ClusterSessionActor {

  import java.util.UUID

  import com.stratio.sparta.serving.core.config.SpartaConfig
  import com.stratio.sparta.serving.core.constants.AppConstant

  import scala.util.Try

  private val timeout = Try(SpartaConfig.getDetailConfig().get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1

  private val writeAll = WriteAll(timeout.seconds)

  private val writeLocal = WriteLocal

  case class NewSession(sessionId: String, identity: String, expires: Long)

  case class NewSessionCreated(sessionId: String)

  case class RefreshSession(sessionId: String, identity: String, expirationTime: Long)

  case class RemoveSession(sessionId: String)

  case class GetSession(sessionId: String)

  case class SessionInfo(identity: String, expiration: Long)

  private final case class SessionContext(key: String, replyTo: ActorRef)

  def getRandomSessionId: String = UUID.randomUUID().toString

}
