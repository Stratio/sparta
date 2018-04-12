/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.utils

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.marathon.OauthTokenUtils.expireToken
import com.stratio.sparta.serving.core.utils.NginxUtils.Error.{UnExpectedError, Unauthorized}
import com.stratio.tikitakka.common.util.HttpRequestUtils
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap.option2NotBlankOption
import com.stratio.sparta.serving.core.constants.AppConstant
import scala.util.Properties

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MarathonAPIUtils(system: ActorSystem, materializer: ActorMaterializer) extends SLF4JLogging{ outer =>

  private[core] lazy val oauthUtils = new HttpRequestUtils {
    override implicit val system: ActorSystem = outer.system
    override implicit val actorMaterializer: ActorMaterializer = outer.materializer
  }

  private[core] val UnauthorizedKey = "<title>Unauthorized</title>"

  private[core] val instanceName = AppConstant.instanceName.fold("sparta-server") {x => x}

  private[core] val marathonApiUri = Properties.envOrNone("MARATHON_TIKI_TAKKA_MARATHON_URI").notBlank


  private[core] def responseUnauthorized(): Future[Nothing] = {
    expireToken()
    val problem = Unauthorized
    log.error(problem.getMessage)
    Future.failed(problem)
  }

  private[core] def responseUnExpectedError(exception: Exception): Future[Nothing] = {
    val problem = UnExpectedError(exception)
    log.error(problem.getMessage)
    Future.failed(problem)
  }


  private[core] def responseCheckedAuthorization(response: String, successfulLog: Option[String]): Future[String] = {
    if(response.contains(UnauthorizedKey))
      responseUnauthorized()
    else {
      successfulLog.foreach(log.debug(_))
      Future(response)
    }
  }

}
