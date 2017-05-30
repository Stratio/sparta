/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.serving.core.utils

import com.stratio.sparta.security.{Action, SpartaSecurityManager}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._


trait ActionUserAuthorize extends Actor with SLF4JLogging{

  def securityActionAuthorizer[T](secManagerOpt: Option[SpartaSecurityManager],
                                  user: Option[LoggedUser],
                                  actions : Map[String, Action],
                                  actionFunction: () => T) : Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        val rejectedActions = actions.flatMap {case (resource, action) =>
          if(!secManager.authorize(userLogged.id, resource, action))
            Option(action)
          else None
        }
        if (rejectedActions.isEmpty){
          log.debug(s"Authorized! Actions: $actions")
          sender ! Left(actionFunction())
        } else{
          log.debug(s"Not authorized: Actions: $actions \t Rejected: ${rejectedActions.head}")
          sender ! Right(errorResponseAuthorization(userLogged.id, actions.head._1))
        }

      case (Some(secManager), None) => sender ! Right(errorNoUserFound(actions.values.toSeq))
      case (None, _) => sender ! Left(actionFunction())
    }

}
