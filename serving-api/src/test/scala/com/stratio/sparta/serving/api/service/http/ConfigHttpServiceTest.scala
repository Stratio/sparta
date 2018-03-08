/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.stratio.sparta.sdk.exception.MockException
import com.stratio.sparta.serving.api.actor.ConfigActor
import com.stratio.sparta.serving.api.actor.ConfigActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.{SpartaConfig, SpartaConfigFactory}
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.models.dto.LoggedUserConstant
import com.stratio.sparta.serving.core.models.frontend.FrontendConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class ConfigHttpServiceTest extends WordSpec
  with ConfigHttpService
  with HttpServiceBaseTest{

  val configActorTestProbe = TestProbe()

  val dummyUser = Some(LoggedUserConstant.AnonymousUser)

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.ConfigActorName -> configActorTestProbe.ref
  )

  override val supervisor: ActorRef = testProbe.ref

  override def beforeEach(): Unit = {
    SpartaConfig.initMainConfig(Option(localConfig), SpartaConfigFactory(localConfig))
  }

  protected def retrieveStringConfig(): FrontendConfiguration =
    FrontendConfiguration(AppConstant.DefaultApiTimeout, dummyUser.get.name)

  "ConfigHttpService.FindAll" should {
    "retrieve a FrontendConfiguration item" in {
      startAutopilot(Left(Success(retrieveStringConfig())))
      Get(s"/${HttpConstant.ConfigPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        responseAs[FrontendConfiguration] should equal(retrieveStringConfig())
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Get(s"/${HttpConstant.ConfigPath}") ~> routes(dummyUser) ~> check {
        testProbe.expectMsgType[FindAll]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

}
