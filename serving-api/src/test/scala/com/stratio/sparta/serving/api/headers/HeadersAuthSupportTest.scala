/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.headers

import akka.actor.ActorSystem
import com.stratio.sparta.serving.core.config.{AuthViaHeadersConfig, SpartaConfig}
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner
import spray.http.HttpHeaders.RawHeader
import spray.http.{HttpHeader, StatusCodes}
import spray.routing.{HttpService, Route}
import spray.testkit.ScalatestRouteTest


@RunWith(classOf[JUnitRunner])
class HeadersAuthSupportTest extends WordSpec with ScalatestRouteTest
  with HttpService with HeadersAuthSupport with Matchers {

  def actorRefFactory: ActorSystem = system
  val userId = "myuser"

  val bbvaHeaders: List[HttpHeader] = List(
    RawHeader("bbva.header.userid", userId),
    RawHeader("bbva.header.groupid", "mygroup")
  )

  override lazy val headersAuthConfig: Option[AuthViaHeadersConfig] = {
    import scala.collection.JavaConverters._
    val headersMap = Map(
      "sparta.authWithHeaders.enabled" -> true,
      "sparta.authWithHeaders.user" -> "bbva.header.userid",
      "sparta.authWithHeaders.group" -> "bbva.header.groupid"
    ).asJava

    val overridenProperties = ConfigFactory.parseMap(headersMap).withFallback(ConfigFactory.load())
    SpartaConfig.getAuthViaHeadersConfig(Some(overridenProperties), force = true)
  }

  def routeToTest: Route = authorizeHeaders{ user =>
    get {
      path("test"){
        complete(user.id)
      }
    }
  }

  "A service with HeadersAuthSupport" should {
    "response unauthorized without user and group header" in {
      Get("/test").withHeaders(Nil) ~> routeToTest ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "response 200 OK when using right user and group headers" in {
      Get("/test").withHeaders(bbvaHeaders) ~> routeToTest ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe userId
      }
    }
  }

  override def cleanUp(): Unit = { system.terminate() }

}
