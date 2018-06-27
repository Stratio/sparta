/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.actor.EnvironmentActor._
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.env.{Environment, EnvironmentData, EnvironmentVariable}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class EnvironmentHttpServiceTest extends WordSpec
  with EnvironmentHttpService
  with HttpServiceBaseTest {

  val workflowTestProbe = TestProbe()
  val environmentTestProbe = TestProbe()
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)
  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.WorkflowActorName -> workflowTestProbe.ref,
    AkkaConstant.EnvironmentActorName -> environmentTestProbe.ref
  )
  override val supervisor: ActorRef = testProbe.ref

  override def beforeEach(): Unit = {
    SpartaConfig.initMainConfig(Option(localConfig), SpartaConfigFactory(localConfig))
  }

  "EnvironmentHttpService.find" should {
    "find a environment" in {
      startAutopilot(Left(Success(getEnvironmentModel())))
      Get(s"/${HttpConstant.EnvironmentPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindEnvironment]
        responseAs[Environment] should equal(getEnvironmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.EnvironmentPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindEnvironment]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "EnvironmentHttpService.findVariable" should {
    "find a environment variable" in {
      startAutopilot(Left(Success(getEnvironmentModel())))
      Get(s"/${HttpConstant.EnvironmentPath}/variable/foo") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindEnvironmentVariable]
        responseAs[Environment] should equal(getEnvironmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.EnvironmentPath}/variable/foo") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindEnvironmentVariable]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "EnvironmentHttpService.delete" should {
    "delete a environment" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.EnvironmentPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteEnvironment]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.EnvironmentPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteEnvironment]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "EnvironmentHttpService.deleteVariable" should {
    "delete a environment" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.EnvironmentPath}/variable/foo") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteEnvironmentVariable]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.EnvironmentPath}/variable/foo") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteEnvironmentVariable]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "EnvironmentHttpService.update" should {
    "update a environment" in {
      startAutopilot(Left(Success(getEnvironmentModel())))
      Put(s"/${HttpConstant.EnvironmentPath}", getEnvironmentModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UpdateEnvironment]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.EnvironmentPath}", getEnvironmentModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UpdateEnvironment]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "EnvironmentHttpService.create" should {
    "create a environment" in {
      startAutopilot(Left(Success(getEnvironmentModel())))
      Post(s"/${HttpConstant.EnvironmentPath}", getEnvironmentModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateEnvironment]
        responseAs[Environment] should equal(getEnvironmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.EnvironmentPath}", getEnvironmentModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateEnvironment]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "EnvironmentHttpService.createVariable" should {
    "create a environment variable" in {
      startAutopilot(Left(Success(getEnvironmentVariableModel())))
      Post(s"/${HttpConstant.EnvironmentPath}/variable", getEnvironmentVariableModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateEnvironmentVariable]
        responseAs[EnvironmentVariable] should equal(getEnvironmentVariableModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.EnvironmentPath}/variable", getEnvironmentVariableModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateEnvironmentVariable]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "EnvironmentHttpService.importData" should {
    "import a environment data" in {
      startAutopilot(Left(Success(getEnvironmentData())))
      Put(s"/${HttpConstant.EnvironmentPath}/import", getEnvironmentData()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[ImportData]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.EnvironmentPath}/import", getEnvironmentData()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[ImportData]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
  
}
