/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.actor.PluginActor.{DeletePlugin, DeletePlugins, ListPlugins, UploadPlugins}
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.files.SpartaFile
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

  val dummyUser = Some(LoggedUserConstant.AnonymousUser)

  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map.empty

  override def beforeEach(): Unit = {
    SpartaConfig.initMainConfig(Option(localConfig), SpartaConfigFactory(localConfig))
  }

  "PluginsHttpService.upload" should {
    "Upload a file" in {
      val response = Left(Success(Seq(SpartaFile("", "", ""))))
      startAutopilot(response)
      Put(s"/${HttpConstant.PluginsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UploadPlugins]
        status should be(StatusCodes.OK)
      }
    }
    "Fail when service is not available" in {
      val response = Left(Failure(new IllegalArgumentException("Error")))
      startAutopilot(response)
      Put(s"/${HttpConstant.PluginsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UploadPlugins]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PluginsHttpService.findAll" should {
    "find all sparta files" in {
      startAutopilot(Left(Success(getSpartaFiles)))
      Get(s"/${HttpConstant.PluginsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[ListPlugins]
        responseAs[Seq[SpartaFile]] should equal(getSpartaFiles)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.PluginsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[ListPlugins]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PluginsHttpService.remove" should {
    "return an OK because the sparta file was deleted" in {
      startAutopilot(Left(Success()))
      Delete(s"/${HttpConstant.PluginsPath}/file.jar") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeletePlugin]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.PluginsPath}/file.jar") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeletePlugin]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "PluginsHttpService.removeAll" should {
    "return an OK because the sparta files was deleted" in {
      startAutopilot(Left(Success(())))
      Delete(s"/${HttpConstant.PluginsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeletePlugins]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.PluginsPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[DeletePlugins]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
