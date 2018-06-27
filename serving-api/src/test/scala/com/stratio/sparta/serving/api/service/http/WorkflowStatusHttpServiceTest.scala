/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.testkit.{TestActor, TestProbe}
import com.stratio.sparta.core.exception.MockException
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.StatusActor.{FindAll, FindById, Update}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class WorkflowStatusHttpServiceTest extends WordSpec
  with WorkflowStatusHttpService
  with HttpServiceBaseTest {

  val statusActorTestProbe = TestProbe()
  val rootUser = Some(LoggedUser("1234","root", "dummyMail","0",Seq.empty[String],Seq.empty[String]))

  override implicit val actors: Map[String, ActorRef] = Map(
    AkkaConstant.StatusActorName -> statusActorTestProbe.ref
  )

  override val supervisor: ActorRef = testProbe.ref

  "WorkflowStatusHttpService.findAll" should {
    "find all policy contexts" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll(user) =>
              sender ! Left(Success(Seq(getWorkflowStatusModel())))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.WorkflowStatusesPath}") ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsg(FindAll(rootUser))
        responseAs[Seq[WorkflowStatus]] should equal(Seq(getWorkflowStatusModel()))
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindAll(user) =>
              sender ! Left(Failure(new MockException))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.WorkflowStatusesPath}") ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsg(FindAll(rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowStatusHttpService.find" should {
    "find policy contexts by id" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindById(id, user) =>
              sender ! Left(Success(getWorkflowStatusModel()))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.WorkflowStatusesPath}/id") ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsg(FindById("id", rootUser))
        responseAs[WorkflowStatus] should equal(getWorkflowStatusModel())
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case FindById(id, user) =>
              sender ! Left(Failure(new MockException))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Get(s"/${HttpConstant.WorkflowStatusesPath}/id") ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsg(FindById("id", rootUser))
        status should be(StatusCodes.InternalServerError)
      }
    }
  }

  "WorkflowStatusHttpService.update" should {
    "update a policy context when the id of the contexts exists" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(policyStatus, user) =>
              sender ! Left(Try(policyStatus))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Put(s"/${HttpConstant.WorkflowStatusesPath}", getWorkflowStatusModel()) ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.OK)
      }
    }
    "return a 500 if there was any error" in {
      val statusActorAutoPilot = Option(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot =
          msg match {
            case Update(policyStatus, user) =>
              sender ! Left(Try(throw new Exception))
              TestActor.NoAutoPilot
          }
      })
      startAutopilot(None, statusActorTestProbe, statusActorAutoPilot)
      Put(s"/${HttpConstant.WorkflowStatusesPath}", getWorkflowStatusModel()) ~> routes(rootUser) ~> check {
        statusActorTestProbe.expectMsgType[Update]
        status should be(StatusCodes.InternalServerError)
      }
    }
  }
}
