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
import com.stratio.tikitakka.common.exceptions.{ConfigurationException, ResponseException}
import com.stratio.tikitakka.common.util.ConfigComponent
import com.stratio.tikitakka.updown.marathon.MarathonComponent
import SpartaMarathonComponent._
import com.stratio.tikitakka.common.model.{ContainerId, CreateApp}
import com.stratio.tikitakka.common.model.marathon.MarathonApplication
import com.stratio.tikitakka.updown.marathon.MarathonComponent.upComponentMethod
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait SpartaMarathonComponent extends MarathonComponent {

  override lazy val uri = ConfigComponent.getString(SpartaMarathonComponent.uriField).getOrElse {
    throw ConfigurationException("The marathon uri was not set")
  }

  override lazy val apiVersion = ConfigComponent.getString(versionField, defaultApiVersion)

  override def upApplication(application: CreateApp, ssoToken: Option[HttpCookie]): Future[ContainerId] = {
    val marathonApp = MarathonApplication(application)
    doRequest[String](uri, upPath, upComponentMethod, Option(Json.toJson(marathonApp)), ssoToken.map(List(_)).getOrElse(Seq.empty))
      .recover { case e =>
        throw ResponseException(s"Error when up an application with error: ${e.getLocalizedMessage}", e)
      }
      .map { case _ =>
        ContainerId(application.id)
      }
  }
}

object SpartaMarathonComponent {

  // Property field constants
  val uriField = "sparta.marathon.tikitakka.marathon.uri"
  val versionField = "sparta.marathon.tikitakka.marathon.api.version"

  // Default property constants
  val defaultApiVersion = "v2"

  val upComponentMethod = POST
  val downComponentMethod = DELETE

  def apply(implicit _system: ActorSystem, _materializer: ActorMaterializer): SpartaMarathonComponent =
    new SpartaMarathonComponent {
      implicit val actorMaterializer: ActorMaterializer = _materializer
      implicit val system: ActorSystem = _system
    }
}
