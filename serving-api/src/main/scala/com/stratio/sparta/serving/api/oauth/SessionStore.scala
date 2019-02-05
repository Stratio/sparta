/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.oauth

import java.util.UUID

import scala.Predef._

object SessionStore {

  var sessionStore: Map[String, (String, Long)] = Map.empty[String, (String, Long)]

  def addSession(sessionId: String, identity: String, expires: Long): Unit = {
    synchronized {
      sessionStore += sessionId -> (identity, now + expires)
    }
  }

  private def validateSession(identity: String, expires: Long, sessionId: String) = {
    if (expires < now) {
      removeSession(sessionId)
      None
    } else Option(identity)
  }

  def getSession(sessionId: String): Option[String] = {
    synchronized {
      sessionStore.get(sessionId) match {
        case Some((identity, expires)) => validateSession(identity, expires, sessionId)
        case _ => None
      }
    }
  }

  def removeSession(sessionId: String): Unit = {
    synchronized {
      sessionStore -= sessionId
    }
  }

  def clean: Iterable[Option[String]] = {
    sessionStore.map{case (id,(identity, expires)) => validateSession(identity,expires,id)}
  }

  def now: Long = System.currentTimeMillis

  def getRandomSessionId: String = UUID.randomUUID().toString
}

