/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.security.{SpartaSecurityManager, _}
import com.stratio.sparta.serving.api.actor.MlModelActor.FindAllModels
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.services.MlModelService
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.intelligence.IntelligenceModel
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import spray.httpx.Json4sJacksonSupport

import scala.util.Try

class MlModelActor()
  extends Actor with SLF4JLogging with Json4sJacksonSupport with SpartaSerializer with ActionUserAuthorize {

  val apiPath = HttpConstant.ConfigPath

  lazy val ResourceType = "MlModels"
  lazy val mlModelService = new MlModelService()

  def receiveApiActions(action: Any): Any = action match {
    case FindAllModels(user) => findAllModels(user)
    case _ => log.info("Unrecognized message in MlModelActor")
  }

  def findAllModels(user: Option[LoggedUser]): Unit = {
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      mlModelService.listModels()
    }
  }

}

object MlModelActor {

  case class FindAllModels(user: Option[LoggedUser])

  type ResponseListIntelligenceModels = Try[Seq[IntelligenceModel]]
}


