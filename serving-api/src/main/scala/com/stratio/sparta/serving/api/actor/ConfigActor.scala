/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import scala.util.Try

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.Config
import spray.httpx.Json4sJacksonSupport

import com.stratio.sparta.security.{SpartaSecurityManager, _}
import com.stratio.sparta.serving.api.actor.ConfigActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.LinkHelper
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.frontend.FrontendConfiguration
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

class ConfigActor(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with SLF4JLogging with Json4sJacksonSupport with SpartaSerializer with ActionUserAuthorize {

  val apiPath = HttpConstant.ConfigPath

  val ResourceType = "Configuration"
  val oauthConfig: Option[Config] = SpartaConfig.getOauth2Config
  val enabledSecurity: Boolean = Try(oauthConfig.get.getString("enable").toBoolean).getOrElse(false)
  val emptyField = ""

  override def receive: Receive = {
    case FindAll(user) => findFrontendConfig(user)
    case _ => log.info("Unrecognized message in ConfigActor")
  }

  def findFrontendConfig(user: Option[LoggedUser]): Unit = {
    authorizeActions[Try[FrontendConfiguration]](user, Map(ResourceType -> View)) {
      retrieveStringConfig(user)
    }
  }

  def retrieveStringConfig(user: Option[LoggedUser]): Try[FrontendConfiguration] = {
    Try {
      val timeout = Try(SpartaConfig.getDetailConfig.get.getInt("timeout"))
        .getOrElse(AppConstant.DefaultApiTimeout) + 1
      if (enabledSecurity)
        FrontendConfiguration(timeout, retrieveNameUser(user), LinkHelper.getClusterLocalLink)
      else FrontendConfiguration(timeout, emptyField, LinkHelper.getClusterLocalLink)
    }

  }

  private def retrieveNameUser(user: Option[LoggedUser]): String = user match {
    case Some(currentUser) if currentUser.name.nonEmpty => currentUser.name
    case Some(currentUser) if currentUser.name.isEmpty => "Anonymous"
    case None => emptyField
  }
}

object ConfigActor {

  case class FindAll(user: Option[LoggedUser])

}
