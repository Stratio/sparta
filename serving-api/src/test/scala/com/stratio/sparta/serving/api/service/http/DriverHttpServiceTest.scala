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

import java.util.UUID

import akka.actor.ActorRef
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.actor.DriverActor.{DeleteDriver, DeleteDrivers, ListDrivers, UploadDrivers}
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.files.SpartaFile
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class DriverHttpServiceTest extends WordSpec
  with DriverHttpService
  with HttpServiceBaseTest {
  override val supervisor: ActorRef = testProbe.ref

  val id = UUID.randomUUID.toString
  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)

  override implicit val actors: Map[String, ActorRef] = Map.empty[String, ActorRef]

  "PluginsHttpService.upload" should {
    "Upload a file" in {
      val response = Left(Success(Seq(SpartaFile("", "", ""))))
      startAutopilot(response)
      Put(s"/${HttpConstant.DriverPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UploadDrivers]
        status should be(StatusCodes.OK)
      }
    }
    "Fail when service is not available" in {
      val response = Left(Failure(new IllegalArgumentException("Error")))
      startAutopilot(response)
      Put(s"/${HttpConstant.DriverPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UploadDrivers]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DriverHttpService.findAll" should {
    "find all sparta files" in {
      startAutopilot(Left(Success(getSpartaFiles)))
      Get(s"/${HttpConstant.DriverPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[ListDrivers]
        responseAs[Seq[SpartaFile]] should equal(getSpartaFiles)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.DriverPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[ListDrivers]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DriverHttpService.remove" should {
    "return an OK because the sparta file was deleted" in {
      startAutopilot(Left(Success(getSpartaFiles)))
      Delete(s"/${HttpConstant.DriverPath}/file.jar") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteDriver]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.DriverPath}/file.jar") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteDriver]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "DriverHttpService.removeAll" should {
    "return an OK because the sparta files was deleted" in {
      startAutopilot(Left(Success(getSpartaFiles)))
      Delete(s"/${HttpConstant.DriverPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteDrivers]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.DriverPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeleteDrivers]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
