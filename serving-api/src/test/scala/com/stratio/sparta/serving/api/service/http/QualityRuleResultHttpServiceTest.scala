/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.TestActor
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.actor.QualityRuleResultActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.authorization.GosecUser
import com.stratio.sparta.serving.core.models.governance.QualityRuleResult
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}


@RunWith(classOf[JUnitRunner])
class QualityRuleResultHttpServiceTest  extends WordSpec
  with QualityRuleResultHttpService
  with HttpServiceBaseTest {

  val rootUser = Option(GosecUser("1234", "root", "dummyMail", "0", Seq.empty[String], Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map()

  override val supervisor: ActorRef = testProbe.ref

  "QualityRuleResultHttpService.findAll" should {
    "find all quality rule results" in {
      startAutopilot(Left(Success(Seq(getQualityRuleResultModel))))
      Get(s"/${HttpConstant.QualityRuleResultsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindAll(rootUser))
        responseAs[Seq[QualityRuleResult]] should equal(Seq(getQualityRuleResultModel))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException)))
      Get(s"/${HttpConstant.QualityRuleResultsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindAll(rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "QualityRuleResultHttpService.find" should {
    "find quality rule result by id" in {
      startAutopilot(Left(Success(getQualityRuleResultModel)))
      Get(s"/${HttpConstant.QualityRuleResultsPath}/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindById("id", rootUser))
        responseAs[QualityRuleResult] should equal(getQualityRuleResultModel)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException)))
      Get(s"/${HttpConstant.QualityRuleResultsPath}/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindById("id", rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "QualityRuleResultHttpService.findByExecutionId" should {
    "find quality rule result by its execution id" in {
      startAutopilot(Left(Success(getQualityRuleResultModel)))
      Get(s"/${HttpConstant.QualityRuleResultsPath}/executionId/exec1") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindByExecutionId("exec1", rootUser))
        responseAs[QualityRuleResult] should equal(getQualityRuleResultModel)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException)))
      Get(s"/${HttpConstant.QualityRuleResultsPath}/executionId/exec1") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindByExecutionId("exec1", rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "QualityRuleResultHttpService.findAllUnsent" should {
    "find all unsent quality rule results" in {
      startAutopilot(Left(Success(Seq(getQualityRuleResultModel))))
      Get(s"/${HttpConstant.QualityRuleResultsPath}/allUnsent") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindAllUnsent(rootUser))
        responseAs[Seq[QualityRuleResult]] should equal(Seq(getQualityRuleResultModel))
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException)))
      Get(s"/${HttpConstant.QualityRuleResultsPath}/allUnsent") ~> routes(rootUser) ~> check {
        testProbe.expectMsg(FindAllUnsent(rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }


  "QualityRuleResultHttpService.create" should {
    "create a quality rule result" in {
      val qualityRuleResultActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case CreateQualityRuleResult(result, user) =>
              sender ! Left(Try(result))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, testProbe, qualityRuleResultActorAutoPilot)
      Post(s"/${HttpConstant.QualityRuleResultsPath}", getQualityRuleResultModel) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateQualityRuleResult]
        responseAs[QualityRuleResult] should equal(getQualityRuleResultModel)
      }
    }
    "return a 500 if there was any error" in {
      val qualityRuleResultActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case CreateQualityRuleResult(result, user) =>
              sender ! Left(Try(throw new Exception))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, testProbe, qualityRuleResultActorAutoPilot)
      Post(s"/${HttpConstant.QualityRuleResultsPath}", getQualityRuleResultModel) ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[CreateQualityRuleResult]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }


  "QualityRuleResultHttpService.deleteAll" should {
    "return an OK if all the results were deleted" in {
      startAutopilot(Left(Success()))
      Delete(s"/${HttpConstant.QualityRuleResultsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteAll]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.QualityRuleResultsPath}") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteAll]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "QualityRuleResultHttpService.deleteById" should {
    "return an OK if the result was deleted" in {
      startAutopilot(Left(Success()))
      Delete(s"/${HttpConstant.QualityRuleResultsPath}/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteById]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.QualityRuleResultsPath}/id") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteById]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }


  "QualityRuleResultHttpService.deleteByExecutionId" should {
    "return an OK if all the related results were deleted" in {
      startAutopilot(Left(Success()))
      Delete(s"/${HttpConstant.QualityRuleResultsPath}/executionId/exec1") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteByExecutionId]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      startAutopilot(Left(Failure(new MockException())))
      Delete(s"/${HttpConstant.QualityRuleResultsPath}/executionId/exec1") ~> routes(rootUser) ~> check {
        testProbe.expectMsgType[DeleteByExecutionId]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}