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
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._


trait ActionUserAuthorize extends Actor{

  def securityActionAuthorizer[T](secManagerOpt: Option[SpartaSecurityManager], user: Option[LoggedUser],
                                  resource : String, action:Action,  actionFunction: T) : Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (secManager.authorize(userLogged.id, resource, action))
          sender ! actionFunction
        else
          sender ! errorResponseAuthorization(userLogged.id, action)
      case (Some(secManager), None) => sender ! errorNoUserFound(action)
      case (None, _) => sender ! actionFunction
    }

}
