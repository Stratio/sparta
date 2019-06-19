/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.oauth

import java.util.UUID
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.ClusterSessionActor.{NewSession, RefreshSession, RemoveSession}

object SessionStore {

  var sessionStore: Map[String, (String, Long)] = Map.empty[String, (String, Long)]

  def addSession(newSession: NewSession): Unit = {
    synchronized {
      import newSession._
      sessionStore += sessionId -> (identity, now + expires)
    }
  }

  def refreshSession(refreshSession: RefreshSession): Unit = {
    synchronized {
      sessionStore.get(refreshSession.sessionId).foreach{
        case (identity: String, _) =>
          sessionStore += refreshSession.sessionId -> (identity, refreshSession.expirationTime)
      }
    }
  }

  def validateSession(sessionId: String): Boolean = {
    sessionStore.get(sessionId) match {
      case Some((_, expires)) =>
        expires >= now
      case _ => false
    }
  }

  def getSession(sessionId: String): Option[String] = {
    synchronized {
      sessionStore.get(sessionId) match {
        case Some((identity, _)) =>
          if (validateSession(sessionId))
            Option(identity)
          else None
        case _ => None
      }
    }
  }

  def removeSession(removeSession: RemoveSession): Unit = {
    synchronized {
      sessionStore -= removeSession.sessionId
    }
  }

  def clean: Iterable[Option[String]] = {
    sessionStore.map { case (id, (identity, _)) =>
      if (!validateSession(id)) {
        removeSession(RemoveSession(id))
        None
      } else Option(identity)
    }
  }

  def now: Long = System.currentTimeMillis

  def getRandomSessionId: String = UUID.randomUUID().toString
}

