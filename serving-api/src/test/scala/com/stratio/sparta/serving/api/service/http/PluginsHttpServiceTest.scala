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

package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.stratio.sparta.serving.api.actor.PluginActor.{PluginResponse, UploadPlugins}
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.models.policy.files.{JarFile, JarFilesResponse}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http._

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class PluginsHttpServiceTest extends WordSpec
  with PluginsHttpService
  with HttpServiceBaseTest {

  override val supervisor: ActorRef = testProbe.ref

  val pluginTestProbe = TestProbe()

  override implicit val actors: Map[String, ActorRef] = Map.empty

  override def beforeEach(): Unit = {
    SpartaConfig.initMainConfig(Option(localConfig), SpartaConfigFactory(localConfig))
  }

  "PluginsHttpService.upload" should {
    "Upload a file" in {
      val response = JarFilesResponse(Success(Seq(JarFile("", "", "", ""))))
      startAutopilot(response)
      Put(s"/${HttpConstant.PluginsPath}") ~> routes ~> check {
        testProbe.expectMsgType[UploadPlugins]
        status should be(StatusCodes.OK)
      }
    }
    "Fail when service is not available" in {
      val response = JarFilesResponse(Failure(new IllegalArgumentException("Error")))
      startAutopilot(response)
      Put(s"/${HttpConstant.PluginsPath}") ~> routes ~> check {
        testProbe.expectMsgType[UploadPlugins]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
