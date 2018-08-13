/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.core.actor.ParameterListActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.{LoggedUser, LoggedUserConstant}
import com.stratio.sparta.serving.core.models.parameters.ParameterList
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class ParameterListHttpServiceTest extends WordSpec
  with ParameterListHttpService
  with HttpServiceBaseTest {
  
  val parameterTestProbe = TestProbe()
  val dummyUser = Some(LoggedUserConstant.AnonymousUser)
  val rootUser = Some(LoggedUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.ParameterListActorName -> parameterTestProbe.ref
  )
  override val supervisor: ActorRef = testProbe.ref

  override def beforeEach(): Unit = {
    SpartaConfig.initMainConfig(Option(localConfig), SpartaConfigFactory(localConfig))
  }

  "ParameterListHttpService.findAll" should {
    "findAll parameter lists" in {
      startAutopilot(Left(Success(Seq(getParameterListModel()))))
      Get(s"/${HttpConstant.ParameterListPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindAllParameterList]
        responseAs[Seq[ParameterList]] should equal(Seq(getParameterListModel()))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.ParameterListPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindAllParameterList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ParameterListHttpService.findByID" should {
    "find a parameter list" in {
      startAutopilot(Left(Success(Seq(getParameterListModel()))))
      Get(s"/${HttpConstant.ParameterListPath}/id/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByIdParameterList]
        responseAs[ParameterList] should equal(getParameterListModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.ParameterListPath}/id/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByIdParameterList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ParameterListHttpService.findByName" should {
    "find a parameter list" in {
      startAutopilot(Left(Success(Seq(getParameterListModel()))))
      Get(s"/${HttpConstant.ParameterListPath}/name/plist") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByNameParameterList]
        responseAs[ParameterList] should equal(getParameterListModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.ParameterListPath}/name/plist") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[FindByNameParameterList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ParameterListHttpService.deleteAll" should {
    "delete all parameter lists" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.ParameterListPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteAllParameterList]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.ParameterListPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteAllParameterList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
  
  "ParameterListHttpService.deleteByName" should {
    "delete a parameter list" in {
      startAutopilot(Left(Success(None)))
      Delete(s"/${HttpConstant.ParameterListPath}/name/plist") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteByNameParameterList]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.ParameterListPath}/name/plist") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteByNameParameterList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ParameterListHttpService.update" should {
    "update a parameter list" in {
      startAutopilot(Left(Success(getParameterListModel())))
      Put(s"/${HttpConstant.ParameterListPath}", getParameterListModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UpdateParameterList]
        status should equal(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Put(s"/${HttpConstant.ParameterListPath}", getParameterListModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[UpdateParameterList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "ParameterListHttpService.create" should {
    "create a parameter list" in {
      startAutopilot(Left(Success(getParameterListModel())))
      Post(s"/${HttpConstant.ParameterListPath}", getParameterListModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateParameterList]
        responseAs[ParameterList] should equal(getParameterListModel())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Post(s"/${HttpConstant.ParameterListPath}", getParameterListModel()) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateParameterList]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
  
}
