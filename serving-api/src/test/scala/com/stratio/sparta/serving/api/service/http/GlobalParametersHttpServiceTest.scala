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
import com.stratio.sparta.serving.api.actor.GlobalParametersActor._
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.parameters.{GlobalParameters, ParameterVariable}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class GlobalParametersHttpServiceTest extends WordSpec
  with GlobalParametersHttpService
  with HttpServiceBaseTest {

  val workflowTestProbe = TestProbe()
  val environmentTestProbe = TestProbe()
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)
  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.WorkflowActorName -> workflowTestProbe.ref,
    AkkaConstant.GlobalParametersActorName -> environmentTestProbe.ref
  )
  override val supervisor: ActorRef = testProbe.ref

  override def beforeEach(): Unit = {
    SpartaConfig.getSpartaConfig(Option(localConfig))
    SpartaConfig.getZookeeperConfig(Option(localConfig))
    SpartaConfig.getDetailConfig(Option(localConfig))
  }

  "GlobalParametersHttpService.find" should {
    "find a global parameters" in {
      startAutopilot(Left(Success(getEnvironmentModel())))
      Get(s"/${HttpConstant.GlobalParametersPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindGlobalParameters]
        responseAs[GlobalParameters] should equal(getEnvironmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.GlobalParametersPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindGlobalParameters]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GlobalParametersHttpService.findVariable" should {
    "find a global parameters variable" in {
      startAutopilot(Left(Success(getEnvironmentModel())))
      Get(s"/${HttpConstant.GlobalParametersPath}/variable/foo") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindGlobalParametersVariable]
        responseAs[GlobalParameters] should equal(getEnvironmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.GlobalParametersPath}/variable/foo") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindGlobalParametersVariable]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GlobalParametersHttpService.delete" should {
    "delete a global parameters" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.GlobalParametersPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteGlobalParameters]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.GlobalParametersPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteGlobalParameters]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GlobalParametersHttpService.deleteVariable" should {
    "delete a global parameters" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.GlobalParametersPath}/variable/foo") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteGlobalParametersVariable]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.GlobalParametersPath}/variable/foo") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteGlobalParametersVariable]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GlobalParametersHttpService.update" should {
    "update a global parameters" in {
      startAutopilot(Left(Success(getEnvironmentModel())))
      Put(s"/${HttpConstant.GlobalParametersPath}", getEnvironmentModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UpdateGlobalParameters]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.GlobalParametersPath}", getEnvironmentModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UpdateGlobalParameters]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GlobalParametersHttpService.create" should {
    "create a global parameters" in {
      startAutopilot(Left(Success(getEnvironmentModel())))
      Post(s"/${HttpConstant.GlobalParametersPath}", getEnvironmentModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateGlobalParameters]
        responseAs[GlobalParameters] should equal(getEnvironmentModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.GlobalParametersPath}", getEnvironmentModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateGlobalParameters]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "GlobalParametersHttpService.createVariable" should {
    "create a global parameters variable" in {
      startAutopilot(Left(Success(getEnvironmentVariableModel())))
      Post(s"/${HttpConstant.GlobalParametersPath}/variable", getEnvironmentVariableModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateGlobalParametersVariable]
        responseAs[ParameterVariable] should equal(getEnvironmentVariableModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.GlobalParametersPath}/variable", getEnvironmentVariableModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateGlobalParametersVariable]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
  
}
