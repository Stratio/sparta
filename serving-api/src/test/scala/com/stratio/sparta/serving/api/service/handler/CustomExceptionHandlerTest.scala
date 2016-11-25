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
package com.stratio.sparta.serving.api.service.handler

import akka.actor.ActorSystem
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}
import spray.http.StatusCodes
import spray.httpx.Json4sJacksonSupport
import spray.routing.{Directives, HttpService, StandardRoute}
import spray.testkit.ScalatestRouteTest
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.service.handler.CustomExceptionHandler._
import com.stratio.sparta.serving.core.SpartaSerializer
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.ErrorModel

@RunWith(classOf[JUnitRunner])
class CustomExceptionHandlerTest extends WordSpec
with Directives with ScalatestRouteTest with Matchers
with Json4sJacksonSupport with HttpService with SpartaSerializer {

  def actorRefFactory: ActorSystem = system

  trait MyTestRoute {

    val exception: Throwable
    val route: StandardRoute = complete(throw exception)
  }

  def route(throwable: Throwable): StandardRoute = complete(throw throwable)

  "CustomExceptionHandler" should {
    "encapsulate a unknow error in an error model and response with a 500 code" in new MyTestRoute {
      val exception = new MockException
      Get() ~> sealRoute(route) ~> check {
        status should be(StatusCodes.InternalServerError)
        response.entity.asString should be(ErrorModel.toString(new ErrorModel("666", "unknown")))
      }
    }
    "encapsulate a serving api error in an error model and response with a 400 code" in new MyTestRoute {
      val exception = ServingCoreException.create(ErrorModel.toString(new ErrorModel("333", "testing exception")))
      Get() ~> sealRoute(route) ~> check {
        status should be(StatusCodes.NotFound)
        response.entity.asString should be(ErrorModel.toString(new ErrorModel("333", "testing exception")))
      }
    }
  }
}
