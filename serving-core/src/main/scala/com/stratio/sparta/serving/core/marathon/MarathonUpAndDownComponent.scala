/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.marathon

import java.net.HttpCookie

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.utils.{HttpRequestUtils, MarathonAPIUtils}
import com.typesafe.config.Config
import org.json4s.jackson.Serialization._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

trait MarathonUpAndDownComponent extends HttpRequestUtils with SpartaSerializer {

  val configuration: Config

  lazy val uri: String = Try(configuration.getString("uri")).getOrElse {
    throw new Exception("The marathon uri was not set")
  }

  lazy val apiVersion: String = Try(configuration.getString("api.version")).getOrElse("v2")

  lazy val marathonAPIUtils = new MarathonAPIUtils(system, actorMaterializer)

  def upApplication(application: MarathonApplication, ssoToken: Option[HttpCookie]): Future[(String, String)] = {
    val marathonAppJson = write(application)

    log.debug(s"Submitting Marathon application: $marathonAppJson")

    for {
      resultHTTP <- doRequest(uri, s"$apiVersion/apps", POST, Option(marathonAppJson), ssoToken.map(List(_)).getOrElse(Seq.empty))
      responseAuth <- marathonAPIUtils.responseCheckedAuthorization(
        resultHTTP._2,
        Option(s"Correctly launched marathon application with id ${application.id}")
      )
    } yield (resultHTTP._1, responseAuth)
  }

  def downPath(appId: String): String = s"$apiVersion/apps/$appId"

  def downApplication(applicationId: String, ssoToken: Option[HttpCookie]): Future[(String, String)] = {

    log.info(s"Killing Marathon application: $applicationId")

    for {
      resultHTTP <- doRequest(uri, downPath(applicationId), DELETE, None, ssoToken.map(List(_)).getOrElse(Seq.empty))
      responseAuth <- marathonAPIUtils.responseCheckedAuthorization(
        resultHTTP._2,
        Option(s"Correctly deleted marathon application with id $applicationId")
      )
    } yield (resultHTTP._1, responseAuth)
  }
}

object MarathonUpAndDownComponent {

  def apply(_configuration: Config)(implicit _actorSystem: ActorSystem, _actorMaterializer: ActorMaterializer): MarathonUpAndDownComponent =
    new MarathonUpAndDownComponent {
      override val configuration: Config = _configuration
      override implicit val actorMaterializer: ActorMaterializer = _actorMaterializer
      override implicit val system: ActorSystem = _actorSystem
    }
}
