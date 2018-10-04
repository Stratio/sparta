/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.handler

import akka.actor.ActorSystem
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
import spray.http.StatusCodes
import spray.httpx.Json4sJacksonSupport
import spray.routing.{Directives, HttpService, StandardRoute}
import spray.testkit.ScalatestRouteTest
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.service.handler.CustomExceptionHandler._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}

@RunWith(classOf[JUnitRunner])
class CustomExceptionHandlerTest extends WordSpec
with Directives with ScalatestRouteTest with Matchers
with Json4sJacksonSupport with HttpService with SpartaSerializer {

  def actorRefFactory: ActorSystem = system

  override def cleanUp(): Unit = { system.terminate() }
  trait MyTestRoute {

    val exception: Throwable
    val route: StandardRoute = complete(throw exception)
  }

  def route(throwable: Throwable): StandardRoute = complete(throw throwable)

  "CustomExceptionHandler" should {
    "encapsulate a unknow error in an error model and response with a 550 code" in new MyTestRoute {
      val exception = new MockException
      Get() ~> sealRoute(route) ~> check {
        status should be(StatusCodes.InternalServerError)
        val result = ErrorModel.toString(
          new ErrorModel(500, "560", "Unknown error", None, Option("com.stratio.sparta.core.exception.MockException")))
        response.entity.asString should be(result)
      }
    }
    "encapsulate a serving api error in an error model and response with a 333 code" in new MyTestRoute {
      val exception = ServerException.create(ErrorModel.toString(new ErrorModel(500, "333", "testing exception")))
      Get() ~> sealRoute(route) ~> check {
        status should be(StatusCodes.InternalServerError)
        response.entity.asString should be(ErrorModel.toString(new ErrorModel(500, "333", "testing exception")))
      }
    }
  }
}
