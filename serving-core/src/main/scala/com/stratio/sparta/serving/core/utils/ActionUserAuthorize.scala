/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.utils

import com.stratio.sparta.security.{Action, SpartaSecurityManager}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.duration._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant

import scala.util.Try

trait ActionUserAuthorize extends Actor with SLF4JLogging {

  private val apiTimeout = Try(SpartaConfig.getDetailConfig.get.getInt("timeout"))
    .getOrElse(AppConstant.DefaultApiTimeout) - 1

  implicit val timeout: Timeout = Timeout(apiTimeout.seconds)

  import context.dispatcher

  def securityActionAuthorizer[T](
                                   user: Option[LoggedUser],
                                   actions: Map[String, Action],
                                   pipeToActor: Option[ActorRef] = None
                                 )(actionFunction: => T)(
                                   implicit secManagerOpt: Option[SpartaSecurityManager]
                                 ): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        val rejectedActions = actions filterNot {
          case (resource, action) => secManager.authorize(userLogged.id, resource, action)
        }

        if (rejectedActions.nonEmpty) {
          // There are rejected actions.
          log.debug(s"Not authorized: Actions: $actions \t Rejected: ${rejectedActions.head}")
          sender ! Right(errorResponseAuthorization(userLogged.id, actions.head._1))
        } else {
          // All actions've been accepted.
          log.debug(s"Authorized! Actions: $actions")
          if (pipeToActor.isDefined) {
            val result = for {
              response <- pipeToActor.get ? actionFunction
            } yield Left(response)
            pipe(result) to sender
          } else sender ! Left(actionFunction)
        }
      case (Some(_), None) =>
        sender ! Right(errorNoUserFound(actions.values.toSeq))
      case (None, _) =>
        if (pipeToActor.isDefined) {
          val result = for {
            response <- pipeToActor.get ? actionFunction
          } yield Left(response)
          pipe(result) to sender
        } else sender ! Left(actionFunction)
    }

}
