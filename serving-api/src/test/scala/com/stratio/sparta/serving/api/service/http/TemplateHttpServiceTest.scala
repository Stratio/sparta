/**
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
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import com.stratio.sparkta.serving.api.service.http.TemplateHttpService
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.actor.TemplateActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.TemplateModel
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class TemplateHttpServiceTest extends WordSpec
with TemplateHttpService
with HttpServiceBaseTest {

  override implicit val actors: Map[String, ActorRef] = Map()
  override val supervisor: ActorRef = testProbe.ref

  "TemplateHttpService" should {
    "find all templates depending of its type" in {
      startAutopilot(ResponseTemplates(Success(Seq(getTemplateModel()))))
      Get(s"/${HttpConstant.TemplatePath}/input") ~> routes ~> check {
        testProbe.expectMsgType[FindByType]
        responseAs[Seq[TemplateModel]] should equal(Seq(getTemplateModel()))
      }
    }

    "return a 500 if there was any error when tries to find all templates depending of its type" in {
      startAutopilot(ResponseTemplates(Failure(new MockException())))
      Get(s"/${HttpConstant.TemplatePath}/input") ~> routes ~> check {
        testProbe.expectMsgType[FindByType]
        status should be (StatusCodes.InternalServerError)
      }
    }

    "find all templates depending of its type and its name" in {
      startAutopilot(ResponseTemplate(Success(getTemplateModel())))
      Get(s"/${HttpConstant.TemplatePath}/input/template") ~> routes ~> check {
        testProbe.expectMsgType[FindByTypeAndName]
        responseAs[TemplateModel] should equal(getTemplateModel())
      }
    }

    "return a 500 if there was any error when tries to find a template depending of its type and its name" in {
      startAutopilot(ResponseTemplate(Failure(new MockException())))
      Get(s"/${HttpConstant.TemplatePath}/input/template") ~> routes ~> check {
        testProbe.expectMsgType[FindByTypeAndName]
        status should be (StatusCodes.InternalServerError)
      }
    }
  }
}


